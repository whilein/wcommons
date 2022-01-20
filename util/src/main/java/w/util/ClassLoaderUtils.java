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

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * @author whilein
 */
@UtilityClass
public class ClassLoaderUtils {

    private static final class SharedClassLoader extends ClassLoader {

        private static final MethodHandle LOAD_CLASS;

        static {
            try {
                LOAD_CLASS = Root.trustedLookupIn(ClassLoader.class).findVirtual(ClassLoader.class,
                        "loadClass", MethodType.methodType(Class.class, String.class, boolean.class));
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        private final Set<ClassLoader> shared;

        private SharedClassLoader(final ClassLoader parent, final Set<ClassLoader> shared) {
            super(parent);

            this.shared = shared;
        }

        @Override
        protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
            return loadClass0(name, resolve);
        }

        private Class<?> loadClass0(final String name, final boolean resolve)
                throws ClassNotFoundException {
            try {
                return super.loadClass(name, resolve);
            } catch (final ClassNotFoundException ignored) {
            }

            for (val loader : shared) {
                if (loader != this) {
                    try {
                        return (Class<?>) LOAD_CLASS.invokeExact(loader, name, resolve);
                    } catch (final Throwable ignored) {
                    }
                }
            }

            throw new ClassNotFoundException(name);
        }

        public Class<?> defineClass(final String name, final byte[] data) {
            return defineClass(name, data, 0, data.length, null);
        }

    }

    private static final class GeneratedClassLoader extends ClassLoader {
        private GeneratedClassLoader(final ClassLoader parent) {
            super(parent);
        }

        public Class<?> defineClass(final String name, final byte[] data) {
            return defineClass(name, data, 0, data.length, null);
        }

    }

    /**
     * Создать класс, который может брать классы нескольких загрузчиков классов.
     *
     * @param parent Основной загрузчик классов
     * @param shared Сет загрузчиков классов
     * @param name   Имя нового класса
     * @param data   Байткод
     * @return Новый класс
     */
    public @NotNull Class<?> defineSharedClass(
            final @NotNull ClassLoader parent,
            final @NotNull Set<@NotNull ClassLoader> shared,
            final @NotNull String name,
            final byte @NotNull [] data
    ) {
        return new SharedClassLoader(parent, shared).defineClass(name, data);
    }

    public @NotNull Class<?> defineClass(
            final @NotNull ClassLoader parent,
            final @NotNull String name,
            final byte @NotNull [] data
    ) {
        return new GeneratedClassLoader(parent).defineClass(name, data);
    }

    public byte @NotNull [] toByteArray(
            final @NonNull Class<?> type
    ) throws IOException {
        return _getResourceBytes(type.getClassLoader(), type.getName().replace('.', '/'));
    }

    public byte @NotNull [] getClassBytes(
            final @NonNull ClassLoader classLoader,
            final @NonNull String className
    ) throws IOException {
        return _getResourceBytes(classLoader, className.replace('.', '/') + ".class");
    }

    private byte[] _getResourceBytes(
            final ClassLoader classLoader,
            final String resourceName
    ) throws IOException {
        try (val inputStream = classLoader.getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new RuntimeException("Resource not found: " + resourceName);
            }

            return inputStream.readAllBytes();
        }
    }

    public byte @NotNull [] getResourceBytes(
            final @NonNull ClassLoader classLoader,
            final @NonNull String resourceName
    ) throws IOException {
        return _getResourceBytes(classLoader, resourceName);
    }

    private Optional<Class<?>> _findClass(
            final ClassLoader classLoader,
            final String className,
            final boolean loadIfNeeded
    ) {
        try {
            return Optional.of(Class.forName(className, loadIfNeeded, classLoader));
        } catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    public @NotNull Optional<@NotNull Class<?>> findClass(
            final @NonNull ClassLoader classLoader,
            final @NonNull String className,
            final boolean loadIfNeeded
    ) {
        return _findClass(classLoader, className, loadIfNeeded);
    }

    public @NotNull Optional<@NotNull Class<?>> findClass(
            final @NonNull String className,
            final boolean loadIfNeeded
    ) {
        return _findClass(ClassLoaderUtils.class.getClassLoader(), className, loadIfNeeded);
    }

    public @NotNull Class<?> loadAny(final @NotNull String @NotNull ... classNames) {
        for (val className : classNames) {
            try {
                return Class.forName(className);
            } catch (final ClassNotFoundException ignored) {
            }
        }

        throw new IllegalStateException("Cannot load any of " + Arrays.toString(classNames));
    }

    public @NotNull Optional<@NotNull Class<?>> findClass(final @NonNull String className) {
        try {
            return Optional.of(Class.forName(className));
        } catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private Class<?> _getClass(
            final ClassLoader classLoader,
            final String className,
            final boolean loadIfNeeded
    ) {
        try {
            return Class.forName(className, loadIfNeeded, classLoader);
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    public @Nullable Class<?> getClass(
            final @NonNull ClassLoader classLoader,
            final @NonNull String className,
            final boolean loadIfNeeded
    ) {
        return _getClass(classLoader, className, loadIfNeeded);
    }

    public @Nullable Class<?> getClass(
            final @NonNull String className,
            final boolean loadIfNeeded
    ) {
        return _getClass(ClassLoaderUtils.class.getClassLoader(), className, loadIfNeeded);
    }

    public @Nullable Field getField(
            final @NotNull ClassLoader cl,
            final @NotNull String className,
            final @NotNull String fieldName) {
        try {
            return cl.loadClass(className).getDeclaredField(fieldName);
        } catch (final Exception e) {
            return null;
        }
    }

    public @Nullable Method getMethod(
            final @NotNull ClassLoader cl,
            final @NotNull String className,
            final @NotNull String methodName,
            final @NotNull String @NotNull [] parameters,
            final @NotNull String returnType
    ) {
        try {
            val type = cl.loadClass(className);

            methodLoop:
            for (val method : type.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getReturnType().getName().equals(returnType)) {
                    val parameterTypes = method.getParameterTypes();

                    if (parameters.length != parameterTypes.length) {
                        continue;
                    }

                    for (int i = 0, j = parameters.length; i < j; i++) {
                        if (!parameters[i].equals(parameterTypes[i].getName())) {
                            continue methodLoop;
                        }
                    }

                    return method;
                }
            }
        } catch (final Exception ignored) {
        }

        return null;
    }

    public @Nullable Class<?> getClass(final @NonNull String className) {
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    private boolean _isClassAvailable(
            final ClassLoader classLoader,
            final String className,
            final boolean loadIfNeeded
    ) {
        try {
            Class.forName(className, loadIfNeeded, classLoader);
        } catch (final ClassNotFoundException e) {
            return false;
        }

        return true;
    }


    public boolean isClassAvailable(
            final @NonNull ClassLoader classLoader,
            final @NonNull String className,
            final boolean loadIfNeeded
    ) {
        return _isClassAvailable(classLoader, className, loadIfNeeded);
    }

    public boolean isClassAvailable(
            final @NonNull String className,
            final boolean loadIfNeeded
    ) {
        return _isClassAvailable(ClassLoaderUtils.class.getClassLoader(), className, loadIfNeeded);
    }

    public boolean isClassAvailable(final @NonNull String className) {
        try {
            Class.forName(className);
        } catch (final ClassNotFoundException e) {
            return false;
        }

        return true;
    }

}
