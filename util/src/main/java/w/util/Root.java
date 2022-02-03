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

package w.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import w.agent.AgentInstrumentation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author whilein
 */
@UtilityClass
public class Root {

    /**
     * Класс {@code MagicAccessorImpl}
     */
    public final Class<?> MAGIC_ACCESSOR_IMPL_TYPE;
    /**
     * Класс {@code Unsafe}
     */
    public final Class<?> UNSAFE_TYPE;

    private final Unsafe UNSAFE;

    static {
        MAGIC_ACCESSOR_IMPL_TYPE = ClassLoaderUtils.loadAny(
                "sun.reflect.MagicAccessorImpl",
                "jdk.internal.reflect.MagicAccessorImpl"
        );

        UNSAFE_TYPE = ClassLoaderUtils.getClass("jdk.internal.misc.Unsafe");

        Unsafe unsafe;

        try {
            val agentInstrumentationExists = ClassLoaderUtils.isClassAvailable(
                    "w.agent.AgentInstrumentation");

            if (agentInstrumentationExists) {
                openAccessWithAgent();
            } else {
                openAccessWithReflection();
            }

            unsafe = new UnsafeImpl();
        } catch (final Exception e) {
            unsafe = new UnsafeStub();
        }

        UNSAFE = unsafe;
    }

    public boolean isUnsafeSupported() {
        return UNSAFE.isSupported();
    }

    @SneakyThrows
    public @NotNull Class<?> defineClass(
            final @NotNull String name,
            final byte @NotNull [] data,
            final int offset,
            final int length,
            final @Nullable ClassLoader classLoader,
            final @Nullable ProtectionDomain protectionDomain
    ) {
        return UNSAFE.defineClass(name, data, offset, length, classLoader, protectionDomain);
    }

    public @NotNull Object allocateUninitializedArray(final Class<?> componentType, final int length) {
        return UNSAFE.allocateUninitializedArray(componentType, length);
    }

    public @NotNull MethodHandles.Lookup trustedLookup() {
        return UNSAFE.trustedLookup();
    }

    public @NotNull MethodHandles.Lookup trustedLookupIn(final @NotNull Class<?> type) {
        return UNSAFE.trustedLookupIn(type);
    }

    private static final class UnsafeImpl extends Unsafe {

        private static final MethodHandles.Lookup IMPL_LOOKUP;

        private static final MethodHandle UNSAFE__DEFINE_CLASS;

        private static final MethodHandle UNSAFE__ALLOCATE_UNINITIALIZED_ARRAY;

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
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            // endregion
        }

        @Override
        public MethodHandles.Lookup trustedLookupIn(final Class<?> type) {
            return IMPL_LOOKUP.in(type);
        }

        @Override
        public MethodHandles.Lookup trustedLookup() {
            return IMPL_LOOKUP;
        }

        @Override
        @SneakyThrows
        public Class<?> defineClass(
                final String name,
                final byte[] data,
                final int offset, final int length,
                final ClassLoader classLoader,
                final ProtectionDomain protectionDomain
        ) {
            return (Class<?>) UNSAFE__DEFINE_CLASS.invokeExact(name, data,
                    offset, length, classLoader, protectionDomain);
        }

        @Override
        @SneakyThrows
        public Object allocateUninitializedArray(final Class<?> componentType, final int length) {
            return UNSAFE__ALLOCATE_UNINITIALIZED_ARRAY.invokeExact(componentType, length);
        }


        @Override
        public boolean isSupported() {
            return true;
        }
    }

    private static final class UnsafeStub extends Unsafe {

        private UnsupportedOperationException constructException() {
            return new UnsupportedOperationException(
                    "Root is not available on current JVM version. " +
                    "To fix the issue, add io.github.whilein.wcommons:agent to dependencies"
            );
        }

        @Override
        public MethodHandles.Lookup trustedLookupIn(final Class<?> type) {
            throw constructException();
        }

        @Override
        public MethodHandles.Lookup trustedLookup() {
            throw constructException();
        }

        @Override
        public Class<?> defineClass(
                final String name,
                final byte[] data,
                final int offset, final int length,
                final ClassLoader classLoader,
                final ProtectionDomain protectionDomain
        ) {
            throw constructException();
        }

        @Override
        public Object allocateUninitializedArray(final Class<?> componentType, final int length) {
            throw constructException();
        }

        @Override
        public boolean isSupported() {
            return false;
        }

    }

    private static abstract class Unsafe {

        public abstract boolean isSupported();

        public abstract MethodHandles.Lookup trustedLookupIn(Class<?> type);

        public abstract MethodHandles.Lookup trustedLookup();

        public abstract Class<?> defineClass(
                String name,
                byte[] data,
                int offset,
                int length,
                ClassLoader classLoader,
                ProtectionDomain protectionDomain
        );

        public abstract Object allocateUninitializedArray(Class<?> componentType, int length);

    }

    @SneakyThrows
    private static void openAccessWithReflection() {
        val modules = new HashSet<Module>();

        {
            val type = Root.class;

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
    }

    private static void openAccessWithAgent() {
        val currentModule = Collections.singleton(Root.class.getModule());

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

}
