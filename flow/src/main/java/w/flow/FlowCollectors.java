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

package w.flow;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import w.util.pair.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * @author whilein
 */
@UtilityClass
@SuppressWarnings({"rawtypes", "unchecked"})
public class FlowCollectors {

    private static final FlowCollector TO_LIST = new ToCollection(ArrayList::new);
    private static final FlowCollector TO_SET = new ToCollection(HashSet::new);

    private static final FlowCollector TO_UNMODIFIABLE_LIST = new ToUnmodifiableCollection<List<?>>(
            ArrayList::new,
            Collections::emptyList,
            Collections::unmodifiableList
    );

    private static final FlowCollector TO_UNMODIFIABLE_SET = new ToUnmodifiableCollection<Set<?>>(
            HashSet::new,
            Collections::emptySet,
            Collections::unmodifiableSet
    );

    public <T> @NotNull FlowCollector<T, ?, T @NotNull []> toFixedLengthArray(
            final @NonNull Supplier<T[]> factory,
            final @NonNull ToIntFunction<T> toIndex
    ) {
        return new ToFixedArray<>(factory, toIndex);
    }

    public <T> @NotNull FlowCollector<T, @NotNull List<T>, T @NotNull []> toArray(
            final @NonNull IntFunction<T[]> factory
    ) {
        return new ToArray<>(factory);
    }

    public <T> @NotNull FlowCollector<T, ?, @NotNull List<T>> toUnmodifiableList() {
        return TO_UNMODIFIABLE_LIST;
    }

    public @NotNull FlowCollector<@NotNull String, @NotNull StringBuilder, @NotNull String> joining() {
        return new Joining("");
    }

    public @NotNull FlowCollector<@NotNull String, @NotNull StringBuilder, @NotNull String> joining(
            final @NotNull String delimiter
    ) {
        return new Joining(delimiter);
    }

    public <T> @NotNull FlowCollector<T, ?, @NotNull Set<T>> toUnmodifiableSet() {
        return TO_UNMODIFIABLE_SET;
    }

    public <K, V, T extends Pair<K, V>> @NotNull FlowCollector<T, ?, @NotNull Map<K, V>> toUnmodifiableMap(
            final @NotNull Supplier<Map<K, V>> mapFactory
    ) {
        return new ToUnmodifiableMap<>(Pair::getLeft, Pair::getRight, mapFactory);
    }

    public <K, V, T extends Pair<K, V>> @NotNull FlowCollector<T, ?, @NotNull Map<K, V>> toMap(
            final @NotNull Supplier<Map<K, V>> mapFactory
    ) {
        return new ToMap<>(Pair::getLeft, Pair::getRight, mapFactory);
    }

    public <K extends Enum<K>, V, T> @NotNull FlowCollector<T, ?, Map<K, V>> toUnmodifiableEnumMap(
            final @NotNull FlowMapper<? super T, ? extends K> keyMapper,
            final @NotNull FlowMapper<? super T, ? extends V> valueMapper,
            final @NotNull Class<K> enumType
    ) {
        return new ToUnmodifiableMap<>(keyMapper, valueMapper, () -> new EnumMap<>(enumType));
    }

    public <K extends Enum<K>, V, T> @NotNull FlowCollector<T, ?, Map<K, V>> toEnumMap(
            final @NotNull FlowMapper<? super T, ? extends K> keyMapper,
            final @NotNull FlowMapper<? super T, ? extends V> valueMapper,
            final @NotNull Class<K> enumType
    ) {
        return new ToMap<>(keyMapper, valueMapper, () -> new EnumMap<>(enumType));
    }

    public <K extends Enum<K>, V, T extends Pair<K, V>> @NotNull FlowCollector<T, ?, Map<K, V>> toUnmodifiableEnumMap(
            final @NotNull Class<K> enumType
    ) {
        return new ToUnmodifiableMap<>(Pair::getLeft, Pair::getRight, () -> new EnumMap<>(enumType));
    }

    public <K extends Enum<K>, V, T extends Pair<K, V>> @NotNull FlowCollector<T, ?, Map<K, V>> toEnumMap(
            final @NotNull Class<K> enumType
    ) {
        return new ToMap<>(Pair::getLeft, Pair::getRight, () -> new EnumMap<>(enumType));
    }

    public <K, V, T> @NotNull FlowCollector<T, ?, @NotNull Map<K, V>> toUnmodifiableMap(
            final @NotNull FlowMapper<? super T, ? extends K> keyMapper,
            final @NotNull FlowMapper<? super T, ? extends V> valueMapper,
            final @NotNull Supplier<Map<K, V>> mapFactory
    ) {
        return new ToUnmodifiableMap<>(keyMapper, valueMapper, mapFactory);
    }

    public <K, V, T> @NotNull FlowCollector<T, ?, @NotNull Map<K, V>> toMap(
            final @NotNull FlowMapper<? super T, ? extends K> keyMapper,
            final @NotNull FlowMapper<? super T, ? extends V> valueMapper,
            final @NotNull Supplier<Map<K, V>> mapFactory
    ) {
        return new ToMap<>(keyMapper, valueMapper, mapFactory);
    }

    public <K, V, T extends Pair<K, V>> @NotNull FlowCollector<T, ?, @NotNull Map<K, V>> toUnmodifiableMap() {
        return new ToUnmodifiableMap<>(Pair::getLeft, Pair::getRight, HashMap::new);
    }

    public <K, V, T extends Pair<K, V>> @NotNull FlowCollector<T, ?, @NotNull Map<K, V>> toMap() {
        return new ToMap<>(Pair::getLeft, Pair::getRight, HashMap::new);
    }

    public <K, V, T> @NotNull FlowCollector<T, ?, @NotNull Map<K, V>> toUnmodifiableMap(
            final @NotNull FlowMapper<? super T, ? extends K> keyMapper,
            final @NotNull FlowMapper<? super T, ? extends V> valueMapper
    ) {
        return new ToUnmodifiableMap<>(keyMapper, valueMapper, HashMap::new);
    }

    public <K, V, T> @NotNull FlowCollector<T, ?, @NotNull Map<K, V>> toMap(
            final @NotNull FlowMapper<? super T, ? extends K> keyMapper,
            final @NotNull FlowMapper<? super T, ? extends V> valueMapper
    ) {
        return new ToMap<>(keyMapper, valueMapper, HashMap::new);
    }

    public <T> @NotNull FlowCollector<T, ?, @NotNull List<T>> toList() {
        return (FlowCollector) TO_LIST;
    }

    public <T> @NotNull FlowCollector<T, ?, @NotNull Set<T>> toSet() {
        return (FlowCollector) TO_SET;
    }

    public <T, R extends Collection<T>> @NotNull FlowCollector<T, ?, R> toCollection(
            final @NonNull Supplier<R> factory
    ) {
        return (FlowCollector<T, ?, R>) new ToCollection<>(factory);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Joining implements FlowCollector<String, StringBuilder, String> {

        final String delimiter;

        boolean previous;
        boolean finished;

        @Override
        public StringBuilder init() {
            if (finished) {
                throw new IllegalStateException("Cannot reuse FlowCollectors#joining");
            }

            return new StringBuilder();
        }

        @Override
        public String empty() {
            finished = true;
            return "";
        }

        @Override
        public void accumulate(final StringBuilder container, final String value) throws Exception {
            if (previous) {
                container.append(delimiter);
            }

            container.append(value);
            previous = true;
        }

        @Override
        public String finish(final StringBuilder container) {
            finished = true;
            return container.toString();
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ToArray<T> implements FlowCollector<T, List<T>, T[]> {

        IntFunction<T[]> factory;

        @Override
        public List<T> init() {
            return new ArrayList<>();
        }

        @Override
        public T[] empty() {
            return factory.apply(0);
        }

        @Override
        public void accumulate(final List<T> collection, final T value) {
            collection.add(value);
        }

        @Override
        public T[] finish(final List<T> collection) {
            return collection.toArray(factory);
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ToFixedArray<T> implements FlowCollector<T, T[], T[]> {

        Supplier<T[]> factory;
        ToIntFunction<T> toIndex;

        @Override
        public T[] init() {
            return factory.get();
        }

        @Override
        public T[] empty() {
            return factory.get();
        }

        @Override
        public void accumulate(final T[] collection, final T value) {
            collection[toIndex.applyAsInt(value)] = value;
        }

        @Override
        public T[] finish(final T[] collection) {
            return collection;
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ToUnmodifiableCollection<R extends Collection> implements FlowCollector<Object, R, R> {

        Supplier<R> factory;
        Supplier<R> empty;

        Function<R, R> toUnmodifiable;

        @Override
        public R init() {
            return factory.get();
        }

        @Override
        public R empty() {
            return empty.get();
        }

        @Override
        public void accumulate(final R collection, final Object value) {
            collection.add(value);
        }

        @Override
        public R finish(final R collection) {
            return toUnmodifiable.apply(collection);
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ToCollection<C extends Collection> implements FlowCollector<Object, C, C> {

        Supplier<C> factory;

        @Override
        public C init() {
            return factory.get();
        }

        @Override
        public C empty() {
            return factory.get();
        }

        @Override
        public void accumulate(final C collection, final Object value) {
            collection.add(value);
        }

        @Override
        public C finish(final C collection) {
            return collection;
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ToUnmodifiableMap<K, V, T> implements FlowCollector<T, Map<K, V>, Map<K, V>> {

        FlowMapper<? super T, ? extends K> keyMapper;
        FlowMapper<? super T, ? extends V> valueMapper;

        Supplier<Map<K, V>> factory;

        @Override
        public Map<K, V> init() {
            return factory.get();
        }

        @Override
        public Map<K, V> empty() {
            return Collections.emptyMap();
        }

        @Override
        public void accumulate(final Map<K, V> map, final T value) throws Exception {
            map.put(keyMapper.map(value), valueMapper.map(value));
        }

        @Override
        public Map<K, V> finish(final Map<K, V> map) {
            return Collections.unmodifiableMap(map);
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ToMap<K, V, T> implements FlowCollector<T, Map<K, V>, Map<K, V>> {

        FlowMapper<? super T, ? extends K> keyMapper;
        FlowMapper<? super T, ? extends V> valueMapper;

        Supplier<Map<K, V>> factory;

        @Override
        public Map<K, V> init() {
            return factory.get();
        }

        @Override
        public Map<K, V> empty() {
            return factory.get();
        }

        @Override
        public void accumulate(final Map<K, V> map, final T value) throws Exception {
            map.put(keyMapper.map(value), valueMapper.map(value));
        }

        @Override
        public Map<K, V> finish(final Map<K, V> map) {
            return map;
        }

    }
}
