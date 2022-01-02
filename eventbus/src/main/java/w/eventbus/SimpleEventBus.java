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
import w.util.ClassLoaderUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleEventBus<T extends SubscribeNamespace>
        implements EventBus<T> {

    @Getter
    Logger logger;

    Object mutex;

    List<RegisteredEventSubscription<?>> subscriptions;
    Map<Class<?>, List<RegisteredEventSubscription<?>>> byEventType;

    @NonFinal
    Map<Class<?>, EventDispatcher> dispatchers;

    public static <T extends SubscribeNamespace> @NotNull EventBus<T> create(final @NotNull Logger logger) {
        return new SimpleEventBus<>(logger, new Object[0], new ArrayList<>(), new HashMap<>(), new HashMap<>());
    }

    public static <T extends SubscribeNamespace> @NotNull EventBus<T> create() {
        return create(LoggerFactory.getLogger(EventBus.class));
    }

    private void registerObject(
            final SubscribeNamespace namespace,
            final Class<?> subscriptionType,
            final Object subscription
    ) {
        if (subscriptionType.isInterface()) {
            throw new IllegalStateException("Cannot register interface as subscription");
        }

        val map = new HashMap<Class<?>, List<RegisteredEventSubscription<?>>>();

        for (val method : subscriptionType.getDeclaredMethods()) {
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
                logger.error("Cannot subscribe to {}, because event is not assignable from it",
                        eventType.getName());

                continue;
            }

            map.put(eventType, register(ImmutableRegisteredEventSubscription.create(
                    AsmDispatchWriters.fromMethod(subscription, method),
                    subscription,
                    subscriptionType,
                    subscribe.order(),
                    subscribe.ignoreCancelled() && Cancellable.class.isAssignableFrom(eventType),
                    namespace,
                    eventType.asSubclass(Event.class)
            )));
        }

        bakeAll(map);
    }

    private void bakeAll(final Map<Class<?>, List<RegisteredEventSubscription<?>>> modifiedDispatchers) {
        val dispatchers = new HashMap<>(this.dispatchers);

        for (val entry : modifiedDispatchers.entrySet()) {
            bake(entry.getKey(), entry.getValue(), dispatchers);
        }

        this.dispatchers = dispatchers;
    }

    private void bake(final Class<?> type, final List<RegisteredEventSubscription<?>> subscriptions) {
        val dispatchers = new HashMap<>(this.dispatchers);
        bake(type, subscriptions, dispatchers);
        this.dispatchers = dispatchers;
    }

    @SneakyThrows
    private void bake(
            final Class<?> type,
            final List<RegisteredEventSubscription<?>> subscriptions,
            final Map<Class<?>, EventDispatcher> dispatchers
    ) {
        if (subscriptions.isEmpty()) {
            dispatchers.remove(type);
            return;
        }

        Collections.sort(subscriptions);

        val parameterTypes = new ArrayList<Class<?>>(subscriptions.size() + 1);
        parameterTypes.add(Logger.class);

        val parameters = new ArrayList<>(subscriptions.size() + 1);
        parameters.add(logger);

        int i, j = subscriptions.size();

        val name = "w/eventbus/GeneratedEventDispatcher";

        val cw = new ClassWriter(0);

        cw.visit(
                Opcodes.V1_1, ACC_PUBLIC, name, null,
                Type.getInternalName(Asm.MAGIC_ACCESSOR_BRIDGE),
                new String[]{Type.getInternalName(EventDispatcher.class)}
        );

        // region <init>
        {
            int stackSize = 1;
            int localSize = 3;

            val descriptor = new StringBuilder();
            descriptor.append("(Lorg/slf4j/Logger;");

            for (i = 0; i < j; i++) {
                val writer = subscriptions.get(i).getDispatchWriter();

                val handle = writer.getHandle();
                val handleType = writer.getType();

                if (handle == null) {
                    continue;
                }

                val size = handleType.getSize();

                stackSize = Math.max(stackSize, size + 1);
                localSize += size;

                descriptor.append(handleType.getDescriptor());

                parameterTypes.add(writer.getHandleType());
                parameters.add(handle);
            }

            descriptor.append(")V");

            val constructor = cw.visitMethod(ACC_PUBLIC, "<init>",
                    descriptor.toString(), null, null);
            constructor.visitVarInsn(ALOAD, 0);
            constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object",
                    "<init>", "()V", false);

            int local = 1;

            // region <init> logger
            {
                cw.visitField(ACC_PRIVATE | ACC_FINAL, "log", "Lorg/slf4j/Logger;",
                        null, null).visitEnd();

                constructor.visitVarInsn(ALOAD, 0);
                constructor.visitVarInsn(ALOAD, local++);
                constructor.visitFieldInsn(PUTFIELD, name, "log", "Lorg/slf4j/Logger;");
            }
            // endregion

            for (i = 0; i < j; i++) {
                val writer = subscriptions.get(i).getDispatchWriter();

                val handle = writer.getHandle();
                val handleType = writer.getType();

                if (handle == null) {
                    continue;
                }

                val fieldName = "_" + i;

                cw.visitField(ACC_PRIVATE | ACC_FINAL, fieldName, handleType.getDescriptor(),
                        null, null).visitEnd();

                constructor.visitVarInsn(ALOAD, 0);
                constructor.visitVarInsn(ALOAD, local);
                constructor.visitFieldInsn(PUTFIELD, name, fieldName, handleType.getDescriptor());

                local += handleType.getSize();
            }

            constructor.visitInsn(RETURN);
            constructor.visitMaxs(stackSize, localSize);
            constructor.visitEnd();
        }
        // endregion
        // region dispatch
        {
            val mv = cw.visitMethod(ACC_PUBLIC, "dispatch", "(Lw/eventbus/Event;)V",
                    null, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(type));
            mv.visitVarInsn(ASTORE, 1);

            boolean hasCastToCancellable = false;
            Label endIgnoreCancelled = null;

            for (i = 0; i < j; i++) {
                val subscription = subscriptions.get(i);
                val writer = subscription.getDispatchWriter();

                val fieldName = "_" + i;

                val start = new Label();
                val end = new Label();

                val handler = new Label();

                val next = new Label();

                if (subscription.isIgnoreCancelled()) {
                    if (!hasCastToCancellable) {
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitTypeInsn(CHECKCAST, "w/eventbus/Cancellable");
                        mv.visitVarInsn(ASTORE, 2);
                        hasCastToCancellable = true;
                    }

                    if (endIgnoreCancelled == null) {
                        endIgnoreCancelled = new Label();

                        mv.visitVarInsn(ALOAD, 2);

                        mv.visitMethodInsn(INVOKEINTERFACE, "w/eventbus/Cancellable", "isCancelled",
                                "()Z", true);

                        mv.visitJumpInsn(IFNE, endIgnoreCancelled);
                    }
                } else if (endIgnoreCancelled != null) {
                    mv.visitLabel(endIgnoreCancelled);
                    endIgnoreCancelled = null;
                }


                mv.visitLabel(start);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, name, fieldName, writer.getType().getDescriptor());

                writer.write(mv, name, fieldName);
                mv.visitJumpInsn(GOTO, next);
                mv.visitLabel(end);

                mv.visitLabel(handler);
                // region error logging
                mv.visitVarInsn(ASTORE, 3); // exception
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, name, "log", "Lorg/slf4j/Logger;");
                mv.visitLdcInsn("Error occurred whilst dispatching " + type.getName() + " to " + writer.getName());
                mv.visitVarInsn(ALOAD, 3);
                mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "error",
                        "(Ljava/lang/String;Ljava/lang/Throwable;)V", true);
                // endregion
                mv.visitLabel(next);

                mv.visitTryCatchBlock(start, end, handler, Type.getInternalName(Exception.class));
            }

            if (endIgnoreCancelled != null) {
                mv.visitLabel(endIgnoreCancelled);
            }

            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 4);
            mv.visitEnd();
        }
        // endregion

        val result = cw.toByteArray();

        Files.write(Paths.get(name.replace('/', '.') + ".class"), result);

        val generatedType = ClassLoaderUtils.defineClass(
                EventBus.class.getClassLoader(),
                name.replace('/', '.'),
                result
        );

        dispatchers.put(type, generatedType.asSubclass(EventDispatcher.class)
                .getDeclaredConstructor(parameterTypes.toArray(new Class[0]))
                .newInstance(parameters.toArray()));
    }

    @Override
    public void bake() {
        synchronized (mutex) {
            val dispatchers = new HashMap<Class<?>, EventDispatcher>();

            for (val subscription : byEventType.entrySet()) {
                bake(subscription.getKey(), subscription.getValue(), dispatchers);
            }

            this.dispatchers = dispatchers;
        }
    }

    @Override
    public void dispatch(final @NotNull Event event) {
        val dispatcher = dispatchers.get(event.getClass());

        if (dispatcher != null) {
            dispatcher.dispatch(event);
        }
    }

    private List<RegisteredEventSubscription<?>> removeFromIndex(final RegisteredEventSubscription<?> subscription) {
        val subscriptionType = subscription.getEvent();

        val subscriptions = byEventType.get(subscription.getEvent());
        subscriptions.remove(subscription);

        if (subscriptions.isEmpty()) {
            byEventType.remove(subscriptionType);
        }

        return subscriptions;
    }

    private void unregisterAll(final Predicate<RegisteredEventSubscription<?>> predicate) {
        final Map<Class<?>, List<RegisteredEventSubscription<?>>> modified = new HashMap<>();

        synchronized (mutex) {
            if (subscriptions.removeIf(subscription -> {
                final boolean result;

                if ((result = predicate.test(subscription))) {
                    modified.put(subscription.getEvent(), removeFromIndex(subscription));
                }

                return result;
            })) {
                bakeAll(modified);
            }
        }
    }

    @Override
    public void unregister(final @NotNull RegisteredEventSubscription<?> subscription) {
        synchronized (mutex) {
            if (subscriptions.remove(subscription)) {
                bake(subscription.getEvent(), removeFromIndex(subscription));
            }
        }
    }

    private List<RegisteredEventSubscription<?>> register(
            final RegisteredEventSubscription<?> subscription
    ) {
        synchronized (mutex) {
            subscriptions.add(subscription);

            val subscriptions = byEventType.computeIfAbsent(subscription.getEvent(),
                    __ -> new ArrayList<>());
            subscriptions.add(subscription);

            return subscriptions;
        }
    }

    @Override
    public void unregisterAll(final @NotNull Object holder) {
        unregisterAll(subscription -> subscription.getHolder() == holder);
    }

    @Override
    public void unregisterAll(final @NotNull Class<?> holderType) {
        unregisterAll(subscription -> subscription.getHolderType() == holderType);
    }

    @Override
    public void unregisterAll(final @NotNull T namespace) {
        unregisterAll(subscription -> subscription.getNamespace() == namespace);
    }

    @Override
    public void register(final @NotNull T namespace, final @NotNull Object subscription) {
        registerObject(namespace, subscription.getClass(), subscription);
    }

    @Override
    public void register(final @NotNull T namespace, final @NotNull Class<?> subscriptionType) {
        registerObject(namespace, subscriptionType, null);
    }

    @Override
    public @NotNull <E extends Event> RegisteredEventSubscription<E> register(
            final @NotNull T namespace,
            final @NotNull Class<E> type,
            final @NotNull Consumer<@NotNull E> subscription
    ) {
        return register(namespace, type, PostOrder.NORMAL, subscription);
    }

    @Override
    public @NotNull <E extends Event> RegisteredEventSubscription<E> register(
            final @NotNull T namespace,
            final @NotNull Class<E> type,
            final @NotNull PostOrder order,
            final @NotNull Consumer<@NotNull E> subscription
    ) {
        val registeredSubscription = ImmutableRegisteredEventSubscription.create(
                AsmDispatchWriters.fromConsumer(subscription),
                null,
                null,
                order,
                false,
                namespace,
                type
        );

        bake(type, register(registeredSubscription));

        return registeredSubscription;
    }

}
