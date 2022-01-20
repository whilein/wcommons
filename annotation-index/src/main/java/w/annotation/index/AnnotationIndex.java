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

package w.annotation.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.util.ClassLoaderUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author whilein
 */
@UtilityClass
public class AnnotationIndex {

    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AnnotationIndexModel EMPTY = new AnnotationIndexModel(
            Collections.emptySet(),
            Collections.emptySet(),
            Collections.emptySet()
    );

    private AnnotationIndexModel _getModel(final ClassLoader cl, final String name) throws IOException {
        val is = cl.getResourceAsStream("META-INF/services/" + name + ".json");

        if (is == null) {
            return EMPTY;
        }

        try (val br = new BufferedReader(new InputStreamReader(is))) {
            return OBJECT_MAPPER.readValue(br, AnnotationIndexModel.class);
        }
    }

    public @NotNull List<@NotNull Field> getAnnotatedFields(
            final @NotNull Class<? extends Annotation> annotation
    ) {
        return getAnnotatedFields(annotation.getClassLoader(), annotation);
    }

    public @NotNull List<@NotNull Method> getAnnotatedMethods(
            final @NotNull Class<? extends Annotation> annotation
    ) {
        return getAnnotatedMethods(annotation.getClassLoader(), annotation);
    }


    @SneakyThrows
    public @NotNull List<@NotNull Method> getAnnotatedMethods(
            final @NotNull ClassLoader cl,
            final @NotNull Class<? extends Annotation> annotation
    ) {
        return _getModel(cl, annotation.getName()).getMethods().stream()
                .map(method -> ClassLoaderUtils.getMethod(cl, method.getType(),
                        method.getName(),
                        method.getParameters(),
                        method.getReturnType()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public @NotNull List<@NotNull Field> getAnnotatedFields(
            final @NotNull ClassLoader cl,
            final @NotNull Class<? extends Annotation> annotation
    ) {
        return _getModel(cl, annotation.getName()).getFields().stream()
                .map(field -> ClassLoaderUtils.getField(cl, field.getType(), field.getName()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Получить все имена классов, с аннотацией {@code annotation}
     *
     * @param cl         Загрузчик классов, из которого будет взят индекс аннотаций
     * @param annotation Тип аннотации
     * @return Список имён классов
     */
    @SneakyThrows
    public @NotNull List<@NotNull String> getAnnotatedTypeNames(
            final @NotNull ClassLoader cl,
            final @NotNull Class<? extends Annotation> annotation
    ) {
        return _getModel(cl, annotation.getName()).getTypes().stream()
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Получить все классы, с аннотацией {@code annotation}
     *
     * @param cl         Загрузчик классов, из которого будет взят индекс аннотаций
     * @param annotation Тип аннотации
     * @return Список классов
     */
    @SneakyThrows
    public @NotNull List<@NotNull Class<?>> getAnnotatedTypes(
            final @NotNull ClassLoader cl,
            final @NotNull Class<? extends Annotation> annotation
    ) {
        return _getModel(cl, annotation.getName()).getTypes().stream()
                .map(name -> ClassLoaderUtils.getClass(cl, name, false))
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Получить все имена классов, с аннотацией {@code annotation}. Индекс аннотаций будет взят из
     * класслоадера переданной аннотации.
     *
     * @param annotation Тип аннотации
     * @return Список имён классов
     */
    @SneakyThrows
    public @NotNull List<@NotNull String> getAnnotatedTypeNames(
            final @NotNull Class<? extends Annotation> annotation
    ) {
        return getAnnotatedTypeNames(annotation.getClassLoader(), annotation);
    }

    /**
     * Получить все классы, с аннотацией {@code annotation}. Индекс аннотаций будет взят из
     * класслоадера переданной аннотации.
     *
     * @param annotation Тип аннотации
     * @return Список классов
     */
    @SneakyThrows
    public @NotNull List<@NotNull Class<?>> getAnnotatedTypes(
            final @NotNull Class<? extends Annotation> annotation
    ) {
        return getAnnotatedTypes(annotation.getClassLoader(), annotation);
    }

}
