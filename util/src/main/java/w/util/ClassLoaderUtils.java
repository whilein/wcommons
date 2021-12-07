/*
 *    Copyright 2021 Whilein
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
import java.util.Optional;

/**
 * @author whilein
 */
@UtilityClass
public class ClassLoaderUtils {

    public byte @NotNull [] toByteArray(
            final @NonNull Class<?> type
    ) throws IOException {
        return _getResourceBytes(type.getClassLoader(), type.getName().replace('.', '/'));
    }

    public byte @NotNull [] getClassBytes(
            final @NonNull ClassLoader classLoader,
            final @NonNull String className
    ) throws IOException {
        return _getResourceBytes(classLoader, className.replace('.', '/'));
    }

    private byte[] _getResourceBytes(
            final ClassLoader classLoader,
            final String resourceName
    ) throws IOException {
        try (val inputStream = classLoader.getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new RuntimeException("Unexpected error occurred");
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

    public Optional<Class<?>> _findClass(
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
