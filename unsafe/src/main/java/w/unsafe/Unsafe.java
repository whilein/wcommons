package w.unsafe;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import w.agent.AgentInstrumentation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author whilein
 */
public interface Unsafe {

    static @NotNull Unsafe getUnsafe() {
        return Initializer.UNSAFE;
    }

    static boolean isUnsafeAvailable() {
        return Initializer.UNSAFE.isAvailable();
    }

    boolean isAvailable();

    void putField(@NotNull Field field, @NotNull Object object, @Nullable Object value);

    void putStaticField(@NotNull Field field, @Nullable Object value);

    @NotNull MethodHandles.Lookup trustedLookupIn(@NotNull Class<?> type);

    @NotNull MethodHandles.Lookup trustedLookup();

    @NotNull Class<?> defineClass(
            @NotNull String name,
            byte @NotNull [] bytecode,
            int offset,
            int length,
            @Nullable ClassLoader classLoader,
            @Nullable ProtectionDomain protectionDomain
    );

    @NotNull Object allocateUninitializedArray(@NotNull Class<?> componentType, int length);

    @UtilityClass
    final class Initializer {
        private final Class<?> UNSAFE_TYPE;
        private final Unsafe UNSAFE;

        static {
            Class<?> unsafeType;

            try {
                unsafeType = Class.forName("jdk.internal.misc.Unsafe");
            } catch (final Exception e) {
                unsafeType = null;
            }

            UNSAFE_TYPE = unsafeType;

            boolean available = false;

            if (unsafeType != null) {
                try {
                    Class.forName("w.agent.AgentInstrumentation");
                    openAccessWithAgent();

                    available = true;
                } catch (final ClassNotFoundException cfe) {
                    available = openAccessWithReflection();
                }
            }

            UNSAFE = available
                    ? new DefaultImpl()
                    : new StubImpl();
        }

        private static boolean openAccessWithReflection() {
            try {
                val modules = new HashSet<Module>();

                {
                    val type = Unsafe.class;

                    val base = type.getModule();

                    if (base.getLayer() != null) {
                        modules.addAll(base.getLayer().modules());
                    }

                    modules.addAll(ModuleLayer.boot().modules());

                    for (ClassLoader cl = type.getClassLoader(); cl != null; cl = cl.getParent()) {
                        modules.add(cl.getUnnamedModule());
                    }
                }

                val method = Module.class.getDeclaredMethod("implAddOpens", String.class);
                method.setAccessible(true);

                for (val module : modules) {
                    for (val name : module.getPackages()) {
                        method.invoke(module, name);
                    }
                }

                return true;
            } catch (final Exception ignored) {
                return false;
            }
        }

        private static void openAccessWithAgent() {
            val currentModule = Collections.singleton(Unsafe.class.getModule());

            AgentInstrumentation.redefineModule(
                    Object.class.getModule(),
                    Collections.emptySet(),
                    Map.of(
                            "jdk.internal.misc", currentModule,
                            "jdk.internal.loader", currentModule
                    ),
                    Map.of(
                            "java.lang", currentModule,
                            "java.lang.invoke", currentModule,
                            "jdk.internal.misc", currentModule,
                            "jdk.internal.loader", currentModule
                    ),
                    Collections.emptySet(),
                    Collections.emptyMap()
            );
        }

        private static final class DefaultImpl implements Unsafe {

            private static final MethodHandles.Lookup IMPL_LOOKUP;

            private static final MethodHandle UNSAFE__DEFINE_CLASS;

            private static final MethodHandle UNSAFE__ALLOCATE_UNINITIALIZED_ARRAY;

            private static final MethodHandle UNSAFE__PUT_REFERENCE;

            private static final MethodHandle UNSAFE__STATIC_FIELD_OFFSET;

            private static final MethodHandle UNSAFE__OBJECT_FIELD_OFFSET;

            static {
                // region IMPL_LOOKUP
                try {
                    val field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                    field.setAccessible(true);

                    if ((IMPL_LOOKUP = (MethodHandles.Lookup) field.get(null)) == null) {
                        throw new IllegalStateException("Lookup.IMPL_LOOKUP is null");
                    }
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
                // endregion

                // region Unsafe
                try {
                    val theUnsafe = IMPL_LOOKUP.findStaticVarHandle(UNSAFE_TYPE, "theUnsafe",
                            UNSAFE_TYPE).get();

                    UNSAFE__DEFINE_CLASS = IMPL_LOOKUP.findVirtual(UNSAFE_TYPE, "defineClass",
                                    methodType(Class.class, String.class, byte[].class, int.class, int.class,
                                            ClassLoader.class, ProtectionDomain.class))
                            .bindTo(theUnsafe);

                    UNSAFE__ALLOCATE_UNINITIALIZED_ARRAY = IMPL_LOOKUP.findVirtual(
                                    UNSAFE_TYPE,
                                    "allocateUninitializedArray",
                                    methodType(Object.class, Class.class, int.class)
                            )
                            .bindTo(theUnsafe);

                    UNSAFE__OBJECT_FIELD_OFFSET = IMPL_LOOKUP.findVirtual(
                                    UNSAFE_TYPE,
                                    "objectFieldOffset",
                                    methodType(long.class, Field.class)
                            )
                            .bindTo(theUnsafe);


                    UNSAFE__STATIC_FIELD_OFFSET = IMPL_LOOKUP.findVirtual(
                                    UNSAFE_TYPE,
                                    "staticFieldOffset",
                                    methodType(long.class, Field.class)
                            )
                            .bindTo(theUnsafe);

                    UNSAFE__PUT_REFERENCE = IMPL_LOOKUP.findVirtual(
                                    UNSAFE_TYPE,
                                    "putReference",
                                    methodType(void.class, Object.class, long.class, Object.class)
                            )
                            .bindTo(theUnsafe);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
                // endregion
            }

            @Override
            public @NotNull MethodHandles.Lookup trustedLookupIn(final @NotNull Class<?> type) {
                return IMPL_LOOKUP.in(type);
            }

            @Override
            public @NotNull MethodHandles.Lookup trustedLookup() {
                return IMPL_LOOKUP;
            }

            @Override
            @SneakyThrows
            public @NotNull Class<?> defineClass(
                    final @NotNull String name,
                    final byte @NotNull [] data,
                    final int offset, final int length,
                    final ClassLoader classLoader,
                    final ProtectionDomain protectionDomain
            ) {
                return (Class<?>) UNSAFE__DEFINE_CLASS.invokeExact(name, data,
                        offset, length, classLoader, protectionDomain);
            }

            @Override
            @SneakyThrows
            public @NotNull Object allocateUninitializedArray(final @NotNull Class<?> componentType, final int length) {
                return UNSAFE__ALLOCATE_UNINITIALIZED_ARRAY.invokeExact(componentType, length);
            }


            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            @SneakyThrows
            public void putField(final @NotNull Field field, final @NotNull Object object, final Object value) {
                UNSAFE__PUT_REFERENCE.invokeExact(object, (long) UNSAFE__OBJECT_FIELD_OFFSET.invokeExact(field), value);
            }

            @Override
            @SneakyThrows
            public void putStaticField(final @NotNull Field field, final Object value) {
                UNSAFE__PUT_REFERENCE.invokeExact(
                        (Object) field.getDeclaringClass(),
                        (long) UNSAFE__STATIC_FIELD_OFFSET.invokeExact(field),
                        value
                );
            }
        }

        private static final class StubImpl implements Unsafe {
            private UnsupportedOperationException constructException() {
                return new UnsupportedOperationException(
                        "Unsafe is not accessible on current JVM version (" + System.getProperty("java.version") + "). " +
                        "To fix the issue, add io.github.whilein.wcommons:wcommons-agent to dependencies"
                );
            }

            @Override
            public @NotNull MethodHandles.Lookup trustedLookupIn(final @NotNull Class<?> type) {
                throw constructException();
            }

            @Override
            public @NotNull MethodHandles.Lookup trustedLookup() {
                throw constructException();
            }

            @Override
            public @NotNull Class<?> defineClass(
                    final @NotNull String name,
                    final byte @NotNull [] data,
                    final int offset, final int length,
                    final ClassLoader classLoader,
                    final ProtectionDomain protectionDomain
            ) {
                throw constructException();
            }

            @Override
            public @NotNull Object allocateUninitializedArray(final @NotNull Class<?> componentType, final int length) {
                throw constructException();
            }

            @Override
            public boolean isAvailable() {
                return false;
            }

            @Override
            public void putField(final @NotNull Field field, final @NotNull Object object, final Object value) {
                throw constructException();
            }

            @Override
            public void putStaticField(final @NotNull Field field, final Object value) {
                throw constructException();
            }

        }

    }

}
