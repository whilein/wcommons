/*
 *    Copyright 2023 Whilein
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

package w.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import w.config.mapper.BooleanMapper;
import w.config.mapper.Mapper;
import w.config.mapper.NumberMapper;
import w.config.mapper.StringMapper;
import w.config.path.ConfigPath;
import w.config.path.SimpleConfigPath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MapBasedMutableConfig implements MutableConfig, Mapper<MutableConfig> {

    Map<String, Object> map;

    @Override
    public boolean equals(@Nullable Object o) {
        return o == this || (o instanceof Config config && map.equals(config.asMap()));
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public @NotNull Config copyContents() {
        return createObject(copyContents(map));
    }

    @SuppressWarnings("unchecked")
    private Object copy(Object object) {
        if (object instanceof Map<?, ?> contents) {
            return copyContents((Map<String, Object>) contents);
        } else if (object instanceof List<?> contents) {
            return copyContents(contents);
        } else {
            return object;
        }
    }

    private List<Object> copyContents(List<?> contents) {
        val copiedList = new ArrayList<>(contents.size());

        for (val element : contents) {
            copiedList.add(copy(element));
        }

        return copiedList;
    }

    private Map<String, Object> copyContents(Map<String, Object> contents) {
        val copiedMap = new HashMap<String, Object>();

        for (val entry : contents.entrySet()) {
            copiedMap.put(entry.getKey(), copy(entry.getValue()));
        }

        return copiedMap;
    }

    @Override
    public @NotNull Mapper<? extends MutableConfig> configMapper() {
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable MutableConfig map(@Nullable Object o) {
        if (o == null) {
            return null;
        }

        if (o instanceof Map<?, ?>) {
            return createObject((Map<String, Object>) o);
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable MutableConfig mapStrict(@Nullable Object o) {
        if (o == null) return null;

        if (o instanceof Map<?, ?>) {
            return createObject((Map<String, Object>) o);
        }

        throw new IllegalStateException("Cannot map " + o + " to config");
    }

    protected abstract MutableConfig createObject(Map<String, Object> map);

    private <T> T require(T value, String key) {
        if (value == null) {
            throw new ConfigMissingKeyException(key);
        }

        return value;
    }

    // region delegate
    @Override
    public @NotNull Set<@NotNull String> keySet() {
        return map.keySet();
    }

    @Override
    public @NotNull Collection<@NotNull Object> values() {
        return map.values();
    }

    @Override
    public void set(@NotNull String key, @Nullable Object object) {
        map.put(key, object);
    }

    @Override
    public void remove(@NotNull String key) {
        map.remove(key);
    }

    @Override
    public boolean contains(@NotNull String key) {
        return map.containsKey(key);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public int size() {
        return map.size();
    }

    // endregion
    // region string
    @Override
    public @NotNull Optional<@NotNull String> findString(@NotNull String key) {
        return find(key, StringMapper.stringMapper());
    }

    @Override
    public @NotNull String getString(@NotNull String key) {
        return get(key, StringMapper.stringMapper());
    }

    @Override
    public @Nullable String getString(@NotNull String key, @Nullable String defaultValue) {
        return get(key, StringMapper.stringMapper(), defaultValue);
    }

    // endregion
    // region boolean
    @Override
    public boolean getBoolean(@NotNull String key, boolean defaultValue) {
        return get(key, BooleanMapper.booleanMapper(), defaultValue);
    }

    @Override
    public boolean getBoolean(@NotNull String key) {
        return get(key, BooleanMapper.booleanMapper());
    }

    @Override
    public @NotNull Optional<Boolean> findBoolean(@NotNull String key) {
        return find(key, BooleanMapper.booleanMapper());
    }
    // endregion
    // region numbers

    @Override
    public int getInt(@NotNull String key) {
        return get(key, NumberMapper.intMapper());
    }

    @Override
    public int getInt(@NotNull String key, int defaultValue) {
        return get(key, NumberMapper.intMapper(), defaultValue);
    }

    @Override
    public @NotNull OptionalInt findInt(@NotNull String key) {
        return find0(key, OptionalInt::empty, OptionalInt::of, NumberMapper.intMapper());
    }

    @Override
    public double getDouble(@NotNull String key) {
        return get(key, NumberMapper.doubleMapper());
    }

    @Override
    public double getDouble(@NotNull String key, double defaultValue) {
        return get(key, NumberMapper.doubleMapper(), defaultValue);
    }

    @Override
    public @NotNull OptionalDouble findDouble(@NotNull String key) {
        return find0(key, OptionalDouble::empty, OptionalDouble::of, NumberMapper.doubleMapper());
    }

    @Override
    public long getLong(@NotNull String key) {
        return get(key, NumberMapper.longMapper());
    }

    @Override
    public long getLong(@NotNull String key, long defaultValue) {
        return get(key, NumberMapper.longMapper(), defaultValue);
    }

    @Override
    public @NotNull OptionalLong findLong(@NotNull String key) {
        return find0(key, OptionalLong::empty, OptionalLong::of, NumberMapper.longMapper());
    }

    // endregion

    @Override
    public @NotNull Object getRaw(@NotNull String key) {
        return require(map.get(key), key);
    }

    @Override
    public @Nullable Object getRaw(@NotNull String key, @Nullable Object defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public @NotNull Optional<Object> findRaw(@NotNull String key) {
        return Optional.ofNullable(map.get(key));
    }

    @Override
    public @NotNull <T> Optional<T> findAs(@NotNull String key, @NotNull Class<T> type) {
        return find(key, mapAs(type));
    }

    @Override
    public <T> @NotNull T getAs(@NotNull String key, @NotNull Class<T> type) {
        return get(key, mapAs(type));
    }

    @Override
    public <T> @Nullable T getAs(@NotNull String key,
                                 @NotNull Class<T> type,
                                 @Nullable T def) {
        return get(key, mapAs(type), def);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public @NotNull MutableConfig getObject(@NotNull String key) {
        val value = map.get(key);

        if (value instanceof Map mapValue) {
            return createObject(mapValue);
        }

        throw new ConfigMissingKeyException(key);
    }

    @Override
    public @NotNull Optional<? extends @NotNull MutableConfig> findObject(@NotNull String key) {
        return find(key, configMapper());
    }

    @Override
    public <T> @NotNull T get(
            @NotNull String key,
            @NotNull Mapper<T> mapper
    ) throws ConfigMissingKeyException {
        return mapper.mapStrict(require(map.get(key), key));
    }

    @Override
    public <T> @Nullable T get(
            @NotNull String key,
            @NotNull Mapper<T> mapper,
            @Nullable T def
    ) {
        val result = mapper.map(map.get(key));
        return result == null ? def : result;
    }

    @Override
    public @NotNull <T> Optional<T> find(@NotNull String key, @NotNull Mapper<T> mapper) {
        return find0(key, Optional::empty, Optional::of, mapper);
    }

    private <T, U> T find0(
            String key,
            Supplier<T> empty,
            Function<U, T> wrap,
            Mapper<U> mapper
    ) {
        val result = mapper.map(map.get(key));

        return result == null ? empty.get() : wrap.apply(result);
    }

    // region list


    @Override
    public @Unmodifiable @Nullable List<@NotNull Byte> getByteList(
            @NotNull String key,
            @Nullable List<Byte> def
    ) {
        return getList(key, NumberMapper.byteMapper());
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Byte> getByteList(@NotNull String key) {
        return getByteList(key, Collections.emptyList());
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull String> getStringList(@NotNull String key) {
        return getList(key, StringMapper.stringMapper());
    }

    @Override
    public @Unmodifiable @NotNull List<? extends @NotNull MutableConfig> getObjectList(@NotNull String key) {
        return getList(key, configMapper(), Collections.emptyList());
    }

    @Override
    @Contract("_, _, !null -> !null")
    public @Unmodifiable @Nullable <T> List<T> getList(
            @NotNull String key,
            @NotNull Mapper<T> mapper,
            @Nullable List<T> def
    ) {
        val value = map.get(key);

        if (value instanceof List<?> list) {
            return list.stream()
                    .map(mapper::mapStrict)
                    .toList();
        }

        return def;
    }

    @Override
    public @Unmodifiable @NotNull <T> List<T> getList(
            @NotNull String key,
            @NotNull Mapper<T> mapper
    ) {
        return getList(key, mapper, Collections.emptyList());
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Integer> getIntList(
            @NotNull String key
    ) {
        return getIntList(key, Collections.emptyList());
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Integer> getIntList(
            @NotNull String key,
            @Nullable List<Integer> def
    ) {
        return getList(key, NumberMapper.intMapper(), def);
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Long> getLongList(
            @NotNull String key,
            @Nullable List<Long> def
    ) {
        return getList(key, NumberMapper.longMapper(), def);
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Short> getShortList(
            @NotNull String key,
            @Nullable List<Short> def
    ) {
        return getList(key, NumberMapper.shortMapper());
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Double> getDoubleList(
            @NotNull String key,
            @Nullable List<Double> def
    ) {
        return getList(key, NumberMapper.doubleMapper(), def);
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Float> getFloatList(
            @NotNull String key,
            @Nullable List<Float> def
    ) {
        return getList(key, NumberMapper.floatMapper(), def);
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Boolean> getBooleanList(
            @NotNull String key,
            @Nullable List<Boolean> def
    ) {
        return getList(key, BooleanMapper.booleanMapper(), def);
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Long> getLongList(@NotNull String key) {
        return getLongList(key, Collections.emptyList());
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Short> getShortList(@NotNull String key) {
        return getShortList(key, Collections.emptyList());
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Double> getDoubleList(@NotNull String key) {
        return getDoubleList(key, Collections.emptyList());
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Float> getFloatList(@NotNull String key) {
        return getFloatList(key, Collections.emptyList());
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Boolean> getBooleanList(@NotNull String key) {
        return getBooleanList(key, Collections.emptyList());
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull Object> asMap() {
        return map;
    }

    @Override
    public void setAll(@NotNull Config config) {
        merge(map, config.asMap());
    }

    private <E> void merge(List<E> oldList, List<E> newList) {
        oldList.addAll(newList);
    }

    @SuppressWarnings("unchecked")
    private <T> T merge(T oldValue, T newValue) {
        if (oldValue instanceof Map<?, ?> && newValue instanceof Map<?, ?>) {
            merge((Map<String, Object>) oldValue, (Map<String, Object>) newValue);
            return oldValue;
        }

        if (oldValue instanceof List<?> && newValue instanceof List<?>) {
            merge((List<Object>) newValue, (List<Object>) oldValue);
            return oldValue;
        }

        return newValue;
    }

    private <K, V> void merge(Map<K, V> oldMap, Map<K, V> newMap) {
        for (val entry : newMap.entrySet()) {
            val key = entry.getKey();
            val newValue = entry.getValue();
            val oldValue = oldMap.get(key);

            oldMap.put(key, merge(oldValue, newValue));
        }
    }

    @Override
    public void writeTo(@NotNull File file) {
        try (val os = new FileOutputStream(file)) {
            writeTo(os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeTo(@NotNull Path path) {
        try (val os = Files.newOutputStream(path)) {
            writeTo(os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull MutableConfig createObject(@NotNull String key) {
        val object = new LinkedHashMap<String, Object>();
        set(key, object);

        return createObject(object);
    }

    @Override
    public @NotNull ConfigPath walk(@NotNull String path) {
        return SimpleConfigPath.create(this, path);
    }
}
