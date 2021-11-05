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

package w.commons.flow;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Collectors for fastutil's collections
 *
 * @author whilein
 */
@UtilityClass
public class IntFlowCollectors {

    public static <V, T> @NotNull FlowCollector<T, ?, @NotNull Int2ObjectMap<V>> toIntObjectMap(
            final @NotNull ToIntFlowMapper<T> keyMapper,
            final @NotNull FlowMapper<T, V> valueMapper
    ) {
        return new ToIntObjectMap<>(keyMapper, valueMapper);
    }

    public static <K, T> @NotNull FlowCollector<T, ?, @NotNull Object2IntMap<K>> toObjectIntMap(
            final @NotNull FlowMapper<T, K> keyMapper,
            final @NotNull ToIntFlowMapper<T> valueMapper
    ) {
        return new ToObjectIntMap<>(keyMapper, valueMapper);
    }


    public static @NotNull IntFlowCollector<@NotNull IntList, int @NotNull []> toArray() {
        return ToArray.INSTANCE;
    }

    public static @NotNull IntFlowCollector<?, @NotNull IntList> toIntList() {
        return new ToIntCollection<>(IntArrayList::new);
    }

    public static @NotNull IntFlowCollector<?, @NotNull IntSet> toIntSet() {
        return new ToIntCollection<>(IntOpenHashSet::new);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ToArray implements IntFlowCollector<IntList, int[]> {

        private static final ToArray INSTANCE = new ToArray();

        @Override
        public IntList init() {
            return new IntArrayList();
        }

        @Override
        public int[] empty() {
            return new int[0];
        }

        @Override
        public void accumulate(final IntList collection, final int value) {
            collection.add(value);
        }

        @Override
        public int[] finish(final IntList collection) {
            return collection.toIntArray();
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ToObjectIntMap<K, T> implements FlowCollector<T, Object2IntMap<K>, Object2IntMap<K>> {

        FlowMapper<T, K> keyMapper;
        ToIntFlowMapper<T> valueMapper;

        @Override
        public Object2IntOpenHashMap<K> init() {
            return new Object2IntOpenHashMap<>();
        }

        @Override
        public Object2IntOpenHashMap<K> empty() {
            return new Object2IntOpenHashMap<>();
        }

        @Override
        public void accumulate(final Object2IntMap<K> map, final T value) throws Exception {
            map.put(keyMapper.map(value), valueMapper.map(value));
        }

        @Override
        public Object2IntMap<K> finish(final Object2IntMap<K> map) {
            return map;
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ToIntObjectMap<V, T> implements FlowCollector<T, Int2ObjectMap<V>, Int2ObjectMap<V>> {

        ToIntFlowMapper<T> keyMapper;
        FlowMapper<T, V> valueMapper;

        @Override
        public Int2ObjectOpenHashMap<V> init() {
            return new Int2ObjectOpenHashMap<>();
        }

        @Override
        public Int2ObjectOpenHashMap<V> empty() {
            return new Int2ObjectOpenHashMap<>();
        }

        @Override
        public void accumulate(final Int2ObjectMap<V> map, final T value) throws Exception {
            map.put(keyMapper.map(value), valueMapper.map(value));
        }

        @Override
        public Int2ObjectMap<V> finish(final Int2ObjectMap<V> map) {
            return map;
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ToIntCollection<R extends IntCollection> implements IntFlowCollector<R, R> {

        Supplier<R> factory;

        @Override
        public R init() {
            return factory.get();
        }

        @Override
        public R empty() {
            return factory.get();
        }

        @Override
        public void accumulate(final R collection, final int value) throws Exception {
            collection.add(value);
        }

        @Override
        public R finish(final R collection) {
            return collection;
        }

    }

}
