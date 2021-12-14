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

package w.impl;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

@UtilityClass
public class ImplLoader {

    private final Map<Class<?>, ImplModel> IMPLEMENTATIONS = new WeakHashMap<>();

    private ImplModel getModel(final Class<?> cls) {
        synchronized (IMPLEMENTATIONS) {
            return IMPLEMENTATIONS.computeIfAbsent(cls, ImplLoader::readModel);
        }
    }

    private ImplModel readModel(final Class<?> cls) {
        try (val is = cls.getClassLoader().getResourceAsStream("META-INF/impl/" + cls.getName())) {
            if (is == null) return null;

            try (val br = new BufferedReader(new InputStreamReader(is))) {
                return br.lines()
                        .map(line -> {
                            val spec = line.split(":");

                            return new ImplModel(spec[0], spec[1], ImplPriority.valueOf(spec[2]));
                        })
                        .min(Comparator.naturalOrder())
                        .orElse(null);
            }
        } catch (final IOException e) {
            return null;
        }
    }

    public <T> @NotNull T create(
            final @NotNull Class<T> type,
            final @Nullable Object @NotNull ... parameters
    ) {
        return find(type, parameters).orElseThrow(() ->
                new IllegalStateException("No implementation found for " + type.getName()));
    }

    @SuppressWarnings("unchecked")
    public <T> @NotNull Optional<T> find(final @NotNull Class<T> type,
                                         final @Nullable Object @NotNull ... parameters) {
        val impl = getModel(type);

        if (impl == null) {
            return Optional.empty();
        }

        val factory = impl.getFactoryMethod();
        val implTypeName = impl.getImplType();

        try {
            val implType = Class.forName(implTypeName, true, type.getClassLoader());

            if (factory.equals("<init>")) {
                for (val constructor : implType.getDeclaredConstructors()) {
                    if (constructor.getParameterCount() == parameters.length) {
                        constructor.setAccessible(true);

                        return Optional.of((T) constructor.newInstance(parameters));
                    }
                }
            } else {
                for (val method : implType.getDeclaredMethods()) {
                    if (method.getName().equals(factory) && method.getParameterCount() == parameters.length) {
                        method.setAccessible(true);

                        return Optional.of((T) method.invoke(null, parameters));
                    }
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException("Cannot load implementation " + type.getName(), e);
        }

        return Optional.empty();
    }

}
