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
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author whilein
 */
@UtilityClass
public class Root {

    private final MethodHandles.Lookup IMPL_LOOKUP;

    /**
     * Класс {@code Unsafe}
     */
    public final Class<?> UNSAFE_TYPE;

    /**
     * Класс {@code MagicAccessorImpl}
     */
    public final Class<?> MAGIC_ACCESSOR_IMPL_TYPE;

    private final MethodHandle UNSAFE__DEFINE_CLASS;

    private final MethodHandle UNSAFE__ALLOCATE_UNINITIALIZED_ARRAY;

    private final ModuleAccessor MODULE_ACCESSOR;

    static {
        MAGIC_ACCESSOR_IMPL_TYPE = ClassLoaderUtils.loadAny(
                "sun.reflect.MagicAccessorImpl",
                "jdk.internal.reflect.MagicAccessorImpl"
        );

        val agentInstrumentationExists = ClassLoaderUtils.isClassAvailable("w.agent.AgentInstrumentation");

        MODULE_ACCESSOR = agentInstrumentationExists
                ? new AgentModuleAccessor()
                : new SimpleModuleAccessor();

        openAccess(Root.class);

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
            UNSAFE_TYPE = Class.forName("jdk.internal.misc.Unsafe");

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

    @SneakyThrows
    public @NotNull Class<?> defineClass(
            final @NotNull String name,
            final byte @NotNull [] data,
            final int offset,
            final int length,
            final @Nullable ClassLoader classLoader,
            final @Nullable ProtectionDomain protectionDomain
    ) {
        return (Class<?>) UNSAFE__DEFINE_CLASS.invokeExact(name, data,
                offset, length, classLoader, protectionDomain);
    }

    @SneakyThrows
    public @NotNull Object allocateUninitializedArray(final Class<?> componentType, final int length) {
        return UNSAFE__ALLOCATE_UNINITIALIZED_ARRAY.invokeExact(componentType, length);
    }

    public @NotNull MethodHandles.Lookup trustedLookup() {
        return IMPL_LOOKUP;
    }

    public @NotNull MethodHandles.Lookup trustedLookupIn(final @NotNull Class<?> type) {
        return IMPL_LOOKUP.in(type);
    }

    @SneakyThrows
    public void openAccess(final @NotNull Class<?> type) {
        MODULE_ACCESSOR.openAccess(type);
    }

    private interface ModuleAccessor {

        void openAccess(@NotNull Class<?> type);

    }

    private static Set<Module> listBaseModules(final Class<?> type) {
        val base = type.getModule();

        val modules = new HashSet<Module>();

        if (base.getLayer() != null) {
            modules.addAll(base.getLayer().modules());
        }

        modules.addAll(ModuleLayer.boot().modules());

        for (ClassLoader cl = type.getClassLoader(); cl != null; cl = cl.getParent()) {
            modules.add(cl.getUnnamedModule());
        }

        return modules;
    }

    private static final class SimpleModuleAccessor implements ModuleAccessor {

        private static final Method MODULE__IMPL_ADD_OPENS;

        static {
            try {
                MODULE__IMPL_ADD_OPENS = Module.class.getDeclaredMethod("implAddOpens", String.class);
                MODULE__IMPL_ADD_OPENS.setAccessible(true);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        @SneakyThrows
        public void openAccess(final @NotNull Class<?> type) {
            for (val module : listBaseModules(type)) {
                for (val name : module.getPackages()) {
                    MODULE__IMPL_ADD_OPENS.invoke(module, name);
                }
            }
        }

    }

    private static final class AgentModuleAccessor implements ModuleAccessor {

        // спиздил у какого-то нн fiškа
        @Override
        public void openAccess(final @NotNull Class<?> type) {
            val currentModule = Collections.singleton(type.getModule());

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

}
