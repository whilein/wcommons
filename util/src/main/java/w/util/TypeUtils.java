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

import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author whilein
 */
@UtilityClass
public class TypeUtils {

    private final Class<?>[] WRAPPERS = new Class[]{
            Boolean.class, Character.class, Byte.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class
    };

    private final Class<?>[] PRIMITIVES = new Class[]{
            boolean.class, char.class, byte.class, short.class,
            int.class, long.class, float.class, double.class
    };

    public @NotNull Optional<@NotNull Class<?>> getPrimitive(final @NotNull Class<?> wrapperType) {
        for (int i = 0, j = WRAPPERS.length; i < j; i++) {
            if (WRAPPERS[i] == wrapperType) {
                return Optional.of(PRIMITIVES[i]);
            }
        }

        return Optional.empty();
    }

    public @NotNull Optional<@NotNull Class<?>> getWrapper(final @NotNull Class<?> primitiveType) {
        for (int i = 0, j = PRIMITIVES.length; i < j; i++) {
            if (PRIMITIVES[i] == primitiveType) {
                return Optional.of(WRAPPERS[i]);
            }
        }

        return Optional.empty();
    }

    public @Unmodifiable @NotNull Set<@NotNull Class<?>> findTypes(final @NotNull Class<?> type) {
        if (type == Object.class || type.isPrimitive()) {
            return Set.of(type);
        }

        val result = new LinkedHashSet<Class<?>>();
        result.add(type);

        findTypes(type, result);

        return Collections.unmodifiableSet(result);
    }

    private void findTypes(final Class<?> type, final Set<Class<?>> result) {
        result.add(type);

        if (!type.isInterface()) {
            val superType = type.getSuperclass();

            if (superType != Object.class) {
                findTypes(superType, result);
            }
        }

        for (val interfaceType : type.getInterfaces()) {
            findTypes(interfaceType, result);
        }
    }

    public boolean isWrapper(final @NotNull Class<?> wrapperType) {
        for (val wrapper : WRAPPERS) {
            if (wrapper == wrapperType) {
                return true;
            }
        }

        return false;
    }

    public boolean isPrimitive(final @NotNull Class<?> primitiveType) {
        for (val primitive : PRIMITIVES) {
            if (primitive == primitiveType) {
                return true;
            }
        }

        return false;
    }

}
