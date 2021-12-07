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

package w.util.unsafe;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import w.util.ClassLoaderUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author whilein
 */
@UtilityClass
public class UnsafeInternals {

    public final @Nullable Class<?> UNSAFE_CLASS;

    public final @Nullable Class<?> MAGIC_ACCESSOR_IMPL_CLASS;

    public final @Nullable String UNSAFE_CLASS_NAME;

    public final @Nullable String MAGIC_ACCESSOR_IMPL_CLASS_NAME;

    private final @Nullable MethodHandle UNSAFE__STATIC_FIELD_BASE__METHOD_HANDLE;

    private final @Nullable MethodHandle UNSAFE__STATIC_FIELD_OFFSET__METHOD_HANDLE;

    private final @Nullable MethodHandle UNSAFE__GET_OBJECT__METHOD_HANDLE;

    static {
        {
            UNSAFE_CLASS = ClassLoaderUtils.findClass("sun.misc.Unsafe")
                    .orElseGet(() -> ClassLoaderUtils.getClass("jdk.internal.misc.Unsafe"));

            if (UNSAFE_CLASS == null) {
                UNSAFE_CLASS_NAME = null;

                UNSAFE__STATIC_FIELD_BASE__METHOD_HANDLE = null;
                UNSAFE__STATIC_FIELD_OFFSET__METHOD_HANDLE = null;
                UNSAFE__GET_OBJECT__METHOD_HANDLE = null;
            } else {
                UNSAFE_CLASS_NAME = UNSAFE_CLASS.getName();

                Object theUnsafe;
                {
                    final Optional<Field> optionalTheUnsafeField;
                    if ((optionalTheUnsafeField = Arrays.stream(UNSAFE_CLASS.getDeclaredFields())
                            .filter(field -> Modifier.isStatic(field.getModifiers())
                                    && field.getName().equals("theUnsafe")
                                    && UNSAFE_CLASS.isAssignableFrom(field.getType())
                            ).findAny()).isPresent()) {
                        final Field theUnsafeField;
                        val accessible = (theUnsafeField = optionalTheUnsafeField.get()).isAccessible();
                        theUnsafeField.setAccessible(true);
                        try {
                            theUnsafe = theUnsafeField.get(null);
                        } catch (final IllegalAccessException e) {
                            theUnsafe = null;
                        } finally {
                            theUnsafeField.setAccessible(accessible);
                        }
                    } else theUnsafe = null;
                }

                if (theUnsafe == null) {
                    UNSAFE__STATIC_FIELD_BASE__METHOD_HANDLE = null;
                    UNSAFE__STATIC_FIELD_OFFSET__METHOD_HANDLE = null;
                    UNSAFE__GET_OBJECT__METHOD_HANDLE = null;
                } else {
                    val lookup = MethodHandles.lookup();

                    UNSAFE__STATIC_FIELD_BASE__METHOD_HANDLE = tryCreateUnsafeMethodHandle(
                            lookup, "staticFieldBase", methodType(Object.class, Field.class), theUnsafe
                    );
                    UNSAFE__STATIC_FIELD_OFFSET__METHOD_HANDLE = tryCreateUnsafeMethodHandle(
                            lookup, "staticFieldOffset", methodType(long.class, Field.class), theUnsafe
                    );
                    UNSAFE__GET_OBJECT__METHOD_HANDLE = tryCreateUnsafeMethodHandle(
                            lookup, "getObject", methodType(Object.class, Object.class, long.class), theUnsafe
                    );
                }
            }
        }

        {
            MAGIC_ACCESSOR_IMPL_CLASS = ClassLoaderUtils.findClass("jdk.internal.reflect.MagicAccessorImpl")
                    .orElseGet(() -> ClassLoaderUtils.getClass("sun.reflect.MagicAccessorImpl"));

            MAGIC_ACCESSOR_IMPL_CLASS_NAME = MAGIC_ACCESSOR_IMPL_CLASS == null
                    ? null : MAGIC_ACCESSOR_IMPL_CLASS.getName();
        }
    }

    private static MethodHandle tryCreateUnsafeMethodHandle(final MethodHandles.Lookup lookup,
                                                            final String name,
                                                            final MethodType type,
                                                            final Object unsafe) {
        final MethodHandle methodHandle;
        try {
            methodHandle = lookup.findVirtual(UNSAFE_CLASS, name, type);
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
        return methodHandle.bindTo(unsafe);
    }


    @SneakyThrows
    public static @Nullable Object staticFieldValue(final @NonNull Field field) {
        if (!Modifier.isStatic(field.getModifiers())) throw new IllegalArgumentException(
                "Field " + field + " is not static"
        );

        if (UNSAFE__STATIC_FIELD_BASE__METHOD_HANDLE == null
                || UNSAFE__STATIC_FIELD_OFFSET__METHOD_HANDLE == null
                || UNSAFE__GET_OBJECT__METHOD_HANDLE == null) {
            return null;
        }

        return UNSAFE__GET_OBJECT__METHOD_HANDLE.invokeExact(
                UNSAFE__STATIC_FIELD_BASE__METHOD_HANDLE.invokeExact(field),
                (long) UNSAFE__STATIC_FIELD_OFFSET__METHOD_HANDLE.invokeExact(field)
        );
    }
}