/*
 *    Copyright 2022 Whilein
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package w.eventbus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import w.asm.Asm;
import w.asm.MagicAccessorBridge;
import w.util.ClassLoaderUtils;
import w.util.TypeUtils;
import w.util.mutable.Mutables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static w.asm.Asm.OBJECT_TYPE;
import static w.asm.Asm.methodDescriptor;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleEventBus implements EventBus {

    private static final Object DEFAULT_NAMESPACE = new Object[0];

    private static final String GEN_DISPATCHER_NAME = "w/eventbus/GeneratedEventDispatcher";

    @Getter
    Logger logger;

    Object mutex;

    List<RegisteredSubscription> subscriptions;
    Map<Class<?>, List<RegisteredSubscription>> byEventType;

    Map<Class<?>, Set<Class<?>>> typeCache;

    @NonFinal
    Map<Class<?>, EventDispatcher> dispatchers;

    NamespaceValidator namespaceValidator;

    /**
     * Создать новый {@link EventBus} с определённым логгером
     *
     * @param logger             Логгер, в котором будет выводиться ошибки слушателей и отладка
     * @param namespaceValidator Проверка на валидность namespace
     * @return Новый {@link EventBus}
     */
    public static @NotNull EventBus create(
            final @NotNull Logger logger,
            final @NotNull NamespaceValidator namespaceValidator
    ) {
        return new SimpleEventBus(
                logger,
                new Object[0],
                new ArrayList<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                namespaceValidator
        );
    }

    /**
     * Создать новый {@link EventBus}
     *
     * @param namespaceValidator Проверка на валидность namespace
     * @return Новый {@link EventBus}
     */
    public static @NotNull EventBus create(
            final @NotNull NamespaceValidator namespaceValidator
    ) {
        return create(LoggerFactory.getLogger(EventBus.class), namespaceValidator);
    }

    private void register(
            final Object namespace,
            final Class<?> subscriptionType,
            final Object subscription
    ) {
        ensureValid(namespace);

        if (subscriptionType.isInterface()) {
            throw new IllegalStateException("Cannot register interface as subscription");
        }

        val map = new HashMap<Class<?>, List<RegisteredSubscription>>();

        for (val type : findTypes(subscriptionType)) {
            for (val method : type.getDeclaredMethods()) {
                if (method.isBridge() || method.isSynthetic()) {
                    continue;
                }

                val subscribe = method.getDeclaredAnnotation(Subscribe.class);

                if (subscribe == null) {
                    continue;
                }

                val parameters = method.getParameterTypes();

                if (parameters.length != 1) {
                    logger.error("Illegal count of parameters for event subscription: " + parameters.length);
                    continue;
                }

                val eventType = parameters[0];

                if (!Event.class.isAssignableFrom(eventType)) {
                    logger.error("Cannot subscribe to {}, because {} is not assignable from it",
                            eventType.getName(), Event.class);

                    continue;
                }

                final Set<Class<? extends Event>> eventTypes = new HashSet<>();
                eventTypes.add(eventType.asSubclass(Event.class));

                for (val childEventType : subscribe.types()) {
                    if (Event.class.isAssignableFrom(childEventType)) {
                        eventTypes.add(childEventType.asSubclass(Event.class));
                    }
                }

                map.putAll(register(
                        ImmutableRegisteredEventSubscription.create(
                                AsmDispatchWriters.fromMethod(subscription, method),
                                subscription,
                                type,
                                subscribe.order(),
                                subscribe.ignoreCancelled()
                                && Cancellable.class.isAssignableFrom(eventType),
                                namespace,
                                Collections.unmodifiableSet(eventTypes)
                        )
                ));
            }
        }

        synchronized (mutex) {
            bakeAll(map);
        }
    }

    private void bakeAll(final Map<Class<?>, List<RegisteredSubscription>> modifiedDispatchers) {
        val dispatchers = new HashMap<>(this.dispatchers);

        for (val entry : modifiedDispatchers.entrySet()) {
            bake(entry.getKey(), entry.getValue(), dispatchers);
        }

        this.dispatchers = dispatchers;
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Field {
        Type type;
        String name;
        int local;
    }

    @SneakyThrows
    private void bake(
            final Class<?> type,
            final List<RegisteredSubscription> subscriptions,
            final Map<Class<?>, EventDispatcher> dispatchers
    ) {
        if (subscriptions.isEmpty()) {
            dispatchers.remove(type);
            return;
        }

        Collections.sort(subscriptions);

        val fields = new HashMap<Object, Field>();
        val classLoaders = new HashSet<ClassLoader>();

        val parameterTypes = new ArrayList<Class<?>>(subscriptions.size() + 1);
        parameterTypes.add(Logger.class);

        val parameters = new ArrayList<>(subscriptions.size() + 1);
        parameters.add(logger);

        int i, j = subscriptions.size();

        val cw = new ClassWriter(0);

        val magicAccessor = MagicAccessorBridge.getInstance();

        cw.visit(
                Opcodes.V1_1, ACC_PUBLIC | ACC_FINAL, GEN_DISPATCHER_NAME, null,
                magicAccessor.isAvailable() ? magicAccessor.getInternalName() : OBJECT_TYPE,
                new String[]{Type.getInternalName(EventDispatcher.class)}
        );

        // region <init>
        {
            int stackSize = 1;
            int localSize = 2;

            val descriptor = new StringBuilder();
            descriptor.append('(').append(Type.getDescriptor(Logger.class));

            val fieldCounter = Mutables.newInt();

            for (i = 0; i < j; i++) {
                val subscription = subscriptions.get(i);

                classLoaders.add(subscription.getOwnerType().getClassLoader());

                for (val event : subscription.getEvents()) {
                    classLoaders.add(event.getClassLoader());
                }

                val owner = subscription.getOwner();

                if (owner != null) {
                    val writer = subscription.getDispatchWriter();
                    val handleType = writer.getOwnerType();

                    val size = handleType.getSize();

                    final int local = localSize;

                    fields.computeIfAbsent(owner, __ -> new Field(
                            handleType,
                            "_" + fieldCounter.getAndIncrement(),
                            local
                    ));

                    stackSize = Math.max(stackSize, size + 1);
                    localSize += size;

                    descriptor.append(handleType.getDescriptor());

                    parameterTypes.add(subscription.getOwnerType());
                    parameters.add(owner);
                }
            }

            descriptor.append(")V");

            val constructor = cw.visitMethod(ACC_PRIVATE, "<init>",
                    descriptor.toString(), null, null);

            constructor.visitVarInsn(ALOAD, 0);

            constructor.visitMethodInsn(INVOKESPECIAL, OBJECT_TYPE,
                    "<init>", methodDescriptor(void.class), false);

            int local = 1;

            // region <init> logger
            {
                cw.visitField(ACC_PRIVATE | ACC_FINAL, "log", "Lorg/slf4j/Logger;",
                        null, null).visitEnd();

                constructor.visitVarInsn(ALOAD, 0);
                constructor.visitVarInsn(ALOAD, local++);
                constructor.visitFieldInsn(PUTFIELD, GEN_DISPATCHER_NAME, "log", "Lorg/slf4j/Logger;");
            }
            // endregion

            for (val field : fields.values()) {
                val fieldName = field.name;
                val fieldType = field.type;
                val fieldDescriptor = fieldType.getDescriptor();

                cw.visitField(ACC_PRIVATE | ACC_FINAL, fieldName, fieldDescriptor,
                        null, null).visitEnd();

                constructor.visitVarInsn(ALOAD, 0);
                constructor.visitVarInsn(ALOAD, field.local);
                constructor.visitFieldInsn(PUTFIELD, GEN_DISPATCHER_NAME, fieldName, fieldDescriptor);

                local += fieldType.getSize();
            }

            constructor.visitInsn(RETURN);
            constructor.visitMaxs(stackSize, localSize);
            constructor.visitEnd();
        }
        // endregion
        // region dispatch
        {
            val mv = cw.visitMethod(ACC_PUBLIC, "dispatch",
                    methodDescriptor(void.class, Event.class), null, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(type));
            mv.visitVarInsn(ASTORE, 1);

            boolean hasCastToCancellable = false;
            Label nextSubscriptionStart = null;

            val anyCancellable = subscriptions.stream().anyMatch(RegisteredSubscription::isIgnoreCancelled);

            for (val subscription : subscriptions) {
                val writer = subscription.getDispatchWriter();

                if (nextSubscriptionStart != null) {
                    mv.visitLabel(nextSubscriptionStart);
                    nextSubscriptionStart = null;
                }

                if (subscription.isIgnoreCancelled()) {
                    if (!hasCastToCancellable) {
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(Cancellable.class));
                        mv.visitVarInsn(ASTORE, 2);
                        hasCastToCancellable = true;
                    }

                    mv.visitVarInsn(ALOAD, 2);

                    mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Cancellable.class),
                            "isCancelled", methodDescriptor(boolean.class), true);

                    mv.visitJumpInsn(IFNE, nextSubscriptionStart = new Label());
                }

                val start = new Label();
                val end = new Label();
                val handler = new Label();
                val next = new Label();

                mv.visitLabel(start);

                val owner = subscription.getOwner();

                if (owner != null) {
                    val fieldName = fields.get(owner).name;

                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, GEN_DISPATCHER_NAME, fieldName, writer.getOwnerType().getDescriptor());
                }

                writer.write(mv);
                mv.visitJumpInsn(GOTO, next);
                mv.visitLabel(end);
                mv.visitLabel(handler);
                mv.visitVarInsn(ASTORE, anyCancellable ? 3 : 2); // exception
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, GEN_DISPATCHER_NAME, "log", Type.getDescriptor(Logger.class));
                mv.visitLdcInsn("Error occurred whilst dispatching " + type.getName()
                                + " to " + writer.getName());
                mv.visitVarInsn(ALOAD, anyCancellable ? 3 : 2);
                mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Logger.class), "error",
                        methodDescriptor(void.class, String.class, Throwable.class), true);
                mv.visitLabel(next);
                mv.visitTryCatchBlock(start, end, handler, Asm.EXCEPTION_TYPE);
            }

            if (nextSubscriptionStart != null) {
                mv.visitLabel(nextSubscriptionStart);
            }

            mv.visitInsn(RETURN);
            mv.visitMaxs(3, anyCancellable ? 4 : 3);
            mv.visitEnd();
        }
        // endregion

        val result = cw.toByteArray();

        classLoaders.removeIf(Objects::isNull);

        val genDispatcherName = GEN_DISPATCHER_NAME.replace('/', '.');
        val currentClassLoader = EventBus.class.getClassLoader();

        final ClassLoader firstClassLoader;

        val generatedType = classLoaders.size() == 1 && (firstClassLoader = classLoaders.iterator().next()) == currentClassLoader
                ? ClassLoaderUtils.defineClass(firstClassLoader, genDispatcherName, result)
                : ClassLoaderUtils.defineSharedClass(currentClassLoader, classLoaders, genDispatcherName, result);

        val constructor = generatedType.asSubclass(EventDispatcher.class)
                .getDeclaredConstructor(parameterTypes.toArray(new Class[0]));

        constructor.setAccessible(true);

        dispatchers.put(type, constructor.newInstance(parameters.toArray()));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E extends AsyncEvent> @NotNull CompletableFuture<E> dispatchAsync(final @NotNull E event) {
        dispatch(event);

        return (CompletableFuture) event.getDoneFuture();
    }

    @Override
    public void dispatch(final @NotNull Event event) {
        val dispatcher = dispatchers.get(event.getClass());

        if (dispatcher != null) {
            dispatcher.dispatch(event);
        }

        postDispatch(event);
    }

    private void postDispatch(final Event event) {
        try {
            event.postDispatch();
        } catch (final Throwable t) {
            logger.error("Error occurred whilst executing Event#postDispatch", t);
        }
    }

    private Map<Class<?>, List<RegisteredSubscription>> removeFromIndex(
            final RegisteredSubscription subscription
    ) {
        val result = new HashMap<Class<?>, List<RegisteredSubscription>>();

        for (val event : subscription.getEvents()) {
            val subscriptions = byEventType.get(event);
            subscriptions.remove(subscription);

            if (subscriptions.isEmpty()) {
                byEventType.remove(event);
            }

            result.put(event, subscriptions);
        }

        return result;
    }

    private void unregisterAll(final Predicate<RegisteredSubscription> predicate) {
        final Map<Class<?>, List<RegisteredSubscription>> modified = new HashMap<>();

        synchronized (mutex) {
            if (subscriptions.removeIf(subscription -> {
                final boolean result;

                if ((result = predicate.test(subscription))) {
                    modified.putAll(removeFromIndex(subscription));
                }

                return result;
            })) {
                bakeAll(modified);
            }
        }
    }

    @Override
    public void unregister(final @NotNull RegisteredSubscription subscription) {
        synchronized (mutex) {
            if (subscriptions.remove(subscription)) {
                bakeAll(removeFromIndex(subscription));
            }
        }
    }

    private Set<Class<?>> findTypes(final Class<?> type) {
        synchronized (mutex) {
            return typeCache.computeIfAbsent(type, TypeUtils::findTypes);
        }
    }

    private Map<Class<?>, List<RegisteredSubscription>> register(
            final RegisteredSubscription subscription
    ) {
        synchronized (mutex) {
            subscriptions.add(subscription);

            val result = new HashMap<Class<?>, List<RegisteredSubscription>>();

            for (val eventType : subscription.getEvents()) {
                val subscriptions = byEventType.computeIfAbsent(eventType,
                        __ -> new ArrayList<>());
                subscriptions.add(subscription);

                result.put(eventType, subscriptions);
            }

            return result;
        }
    }

    @Override
    public void unregisterAll(final @NotNull Object owner) {
        unregisterAll(subscription -> subscription.getOwner() == owner);
    }

    @Override
    public void unregisterAll(final @NotNull Class<?> ownerType) {
        unregisterAll(subscription -> subscription.getOwnerType() == ownerType);
    }

    @Override
    public void unregisterAllByNamespace(final @NotNull Object namespace) {
        unregisterAll(subscription -> subscription.getNamespace() == namespace);
    }

    @Override
    public void unregisterAll() {
        synchronized (mutex) {
            dispatchers = new HashMap<>();
        }
    }

    @Override
    public void register(final @NotNull Object namespace, final @NotNull Object subscription) {
        register(namespace, subscription.getClass(), subscription);
    }

    @Override
    public void register(final @NotNull Object namespace, final @NotNull Class<?> subscriptionType) {
        register(namespace, subscriptionType, null);
    }

    @Override
    public void register(final @NotNull Object subscription) {
        register(DEFAULT_NAMESPACE, subscription);
    }

    @Override
    public void register(final @NotNull Class<?> subscriptionType) {
        register(DEFAULT_NAMESPACE, subscriptionType);
    }

    @Override
    public @NotNull <E extends Event> RegisteredSubscription register(
            final @NotNull Object namespace,
            final @NotNull Class<E> type,
            final @NotNull Consumer<@NotNull E> subscription
    ) {

        return register(namespace, type, PostOrder.NORMAL, subscription);
    }

    @Override
    public @NotNull <E extends Event> RegisteredSubscription register(
            final @NotNull Class<E> type,
            final @NotNull Consumer<@NotNull E> subscription
    ) {
        return register(DEFAULT_NAMESPACE, type, subscription);
    }

    @Override
    public @NotNull <E extends Event> RegisteredSubscription register(
            final @NotNull Object namespace,
            final @NotNull Class<E> type,
            final @NotNull PostOrder order,
            final @NotNull Consumer<@NotNull E> subscription
    ) {
        ensureValid(namespace);

        val registeredSubscription = ImmutableRegisteredEventSubscription.create(
                AsmDispatchWriters.fromConsumer(subscription),
                subscription,
                Consumer.class,
                order,
                false,
                namespace,
                Set.of(type)
        );

        synchronized (mutex) {
            bakeAll(register(registeredSubscription));
        }

        return registeredSubscription;
    }

    @Override
    public @NotNull <E extends Event> RegisteredSubscription register(
            final @NotNull Class<E> type,
            final @NotNull PostOrder order,
            final @NotNull Consumer<@NotNull E> subscription
    ) {
        return register(DEFAULT_NAMESPACE, type, order, subscription);
    }

    private void ensureValid(final Object namespace) {
        if (!namespaceValidator.isValid(namespace)) {
            throw new IllegalStateException("EventBus doesn't permit usage of namespace: " +
                                            (namespace != DEFAULT_NAMESPACE
                                                    ? namespace.getClass().getName()
                                                    : "<none>"));
        }
    }

}
