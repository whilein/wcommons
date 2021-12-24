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

package w.annotation.index;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.util.ClassLoaderUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author whilein
 */
@UtilityClass
public class AnnotationIndex {

    private Stream<String> _getAnnotated(final ClassLoader cl, final String name) throws IOException {
        val is = cl.getResourceAsStream("META-INF/services/" + name);

        if (is == null) {
            return Stream.empty();
        }

        try (val br = new BufferedReader(new InputStreamReader(is))) {
            return br.lines();
        }
    }

    @SneakyThrows
    private @NotNull List<@NotNull String> getAnnotatedTypeNames(final @NotNull ClassLoader cl, final @NotNull Class<?> annotation) {
        return _getAnnotated(cl, annotation.getName())
                .collect(Collectors.toUnmodifiableList());
    }

    @SneakyThrows
    public @NotNull List<@NotNull Class<?>> getAnnotatedTypes(final @NotNull ClassLoader cl, final @NotNull Class<?> annotation) {
        return _getAnnotated(cl, annotation.getName())
                .map(ClassLoaderUtils::getClass)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());
    }


    @SneakyThrows
    private @NotNull List<@NotNull String> getAnnotatedTypeNames(final @NotNull Class<?> annotation) {
        return getAnnotatedTypeNames(annotation.getClassLoader(), annotation);
    }

    @SneakyThrows
    public @NotNull List<@NotNull Class<?>> getAnnotatedTypes(final @NotNull Class<?> annotation) {
        return getAnnotatedTypes(annotation.getClassLoader(), annotation);
    }

}
