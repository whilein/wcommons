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
import w.config.path.ConfigPath;
import w.config.path.SimpleConfigPath;
import w.config.transformer.Transformer;
import w.config.transformer.Transformers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractMapConfig implements Config, Transformer<Config> {

    Map<String, Object> map;

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
    public @NotNull Transformer<Config> configTransformer() {
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Config transformOrNull(final Object o) {
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
    public Config transform(final Object o) {
        if (o == null) {
            return null;
        }

        if (o instanceof Map<?, ?>) {
            return createObject((Map<String, Object>) o);
        }

        throw new IllegalStateException("Cannot transform " + o + " to config");
    }

    protected abstract Config createObject(final Map<String, Object> map);

    private <T> T require(final T value, final String key) {
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
    public void set(final @NotNull String key, final @Nullable Object object) {
        map.put(key, object);
    }

    @Override
    public void remove(final @NotNull String key) {
        map.remove(key);
    }

    @Override
    public boolean contains(final @NotNull String key) {
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
    public @NotNull Optional<@NotNull String> findString(final @NotNull String key) {
        return find(key, Transformers.stringTransformer());
    }

    @Override
    public @NotNull String getString(final @NotNull String key) {
        return get(key, Transformers.stringTransformer());
    }

    @Override
    public @Nullable String getString(final @NotNull String key, final @Nullable String defaultValue) {
        return get(key, Transformers.stringTransformer(), defaultValue);
    }

    // endregion
    // region boolean
    @Override
    public boolean getBoolean(final @NotNull String key, final boolean defaultValue) {
        return get(key, Transformers.booleanTransformer(), defaultValue);
    }

    @Override
    public boolean getBoolean(final @NotNull String key) {
        return get(key, Transformers.booleanTransformer());
    }

    @Override
    public @NotNull Optional<Boolean> findBoolean(final @NotNull String key) {
        return find(key, Transformers.booleanTransformer());
    }
    // endregion
    // region numbers

    @Override
    public int getInt(final @NotNull String key) {
        return get(key, Transformers.intTransformer());
    }

    @Override
    public int getInt(final @NotNull String key, final int defaultValue) {
        return get(key, Transformers.intTransformer(), defaultValue);
    }

    @Override
    public @NotNull OptionalInt findInt(final @NotNull String key) {
        return find0(key, OptionalInt::empty, OptionalInt::of, Transformers.intTransformer());
    }

    @Override
    public double getDouble(final @NotNull String key) {
        return get(key, Transformers.doubleTransformer());
    }

    @Override
    public double getDouble(final @NotNull String key, final double defaultValue) {
        return get(key, Transformers.doubleTransformer(), defaultValue);
    }

    @Override
    public @NotNull OptionalDouble findDouble(final @NotNull String key) {
        return find0(key, OptionalDouble::empty, OptionalDouble::of, Transformers.doubleTransformer());
    }

    @Override
    public long getLong(final @NotNull String key) {
        return get(key, Transformers.longTransformer());
    }

    @Override
    public long getLong(final @NotNull String key, final long defaultValue) {
        return get(key, Transformers.longTransformer(), defaultValue);
    }

    @Override
    public @NotNull OptionalLong findLong(final @NotNull String key) {
        return find0(key, OptionalLong::empty, OptionalLong::of, Transformers.longTransformer());
    }

    // endregion

    @Override
    public @NotNull Object getRaw(final @NotNull String key) {
        return require(map.get(key), key);
    }

    @Override
    public @Nullable Object getRaw(final @NotNull String key, final @Nullable Object defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public @NotNull Optional<Object> findRaw(final @NotNull String key) {
        return Optional.ofNullable(map.get(key));
    }

    @Override
    public @NotNull <T> Optional<T> findAs(final @NotNull String key, final @NotNull Class<T> type) {
        return find(key, transformAs(type));
    }

    @Override
    public <T> @NotNull T getAs(final @NotNull String key, final @NotNull Class<T> type) {
        return get(key, transformAs(type));
    }

    @Override
    public <T> @Nullable T getAs(final @NotNull String key,
                                 final @NotNull Class<T> type,
                                 final @Nullable T def) {
        return get(key, transformAs(type), def);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public @NotNull Config getObject(final @NotNull String key) {
        val value = map.get(key);

        if (value instanceof Map mapValue) {
            return createObject(mapValue);
        }

        throw new ConfigMissingKeyException(key);
    }

    @Override
    public @NotNull Optional<@NotNull Config> findObject(final @NotNull String key) {
        return find(key, configTransformer());
    }



    @Override
    public <T> @NotNull T get(
            final @NotNull String key,
            final @NotNull Transformer<T> transformer
    ) throws ConfigMissingKeyException {
        return require(transformer.transform(map.get(key)), key);
    }

    @Override
    public <T> @Nullable T get(
            final @NotNull String key,
            final @NotNull Transformer<T> transformer,
            final @Nullable T def
    ) {
        val result = transformer.transformOrNull(map.get(key));
        return result == null ? def : result;
    }

    @Override
    public @NotNull <T> Optional<T> find(final @NotNull String key, final @NotNull Transformer<T> transformer) {
        return find0(key, Optional::empty, Optional::of, transformer);
    }

    private <T, U> T find0(
            final String key,
            final Supplier<T> empty,
            final Function<U, T> wrap,
            final Transformer<U> transformer
    ) {
        val result = transformer.transformOrNull(map.get(key));

        return result == null ? empty.get() : wrap.apply(result);
    }

    // region list


    @Override
    public @Unmodifiable @Nullable List<@NotNull Byte> getByteList(
            final @NotNull String key,
            final @Nullable List<Byte> def
    ) {
        return getList(key, Transformers.byteTransformer());
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Byte> getByteList(final @NotNull String key) {
        return getByteList(key, Collections.emptyList());
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull String> getStringList(final @NotNull String key) {
        return getList(key, Transformers.stringTransformer());
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Config> getObjectList(final @NotNull String key) {
        return getList(key, configTransformer(), Collections.emptyList());
    }

    @Override
    @Contract("_, _, !null -> !null")
    public @Unmodifiable @Nullable <T> List<T> getList(
            final @NotNull String key,
            final @NotNull Transformer<T> transformer,
            final @Nullable List<T> def
    ) {
        val value = map.get(key);

        if (value instanceof List<?> list) {
            return list.stream()
                    .map(transformer::transform)
                    .toList();
        }

        return def;
    }

    @Override
    public @Unmodifiable @NotNull <T> List<T> getList(
            final @NotNull String key,
            final @NotNull Transformer<T> transformer
    ) {
        return getList(key, transformer, Collections.emptyList());
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Integer> getIntList(
            final @NotNull String key
    ) {
        return getIntList(key, Collections.emptyList());
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Integer> getIntList(
            final @NotNull String key,
            final @Nullable List<Integer> def
    ) {
        return getList(key, Transformers.intTransformer(), def);
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Long> getLongList(
            final @NotNull String key,
            final @Nullable List<Long> def
    ) {
        return getList(key, Transformers.longTransformer(), def);
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Short> getShortList(
            final @NotNull String key,
            final @Nullable List<Short> def
    ) {
        return getList(key, Transformers.shortTransformer());
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Double> getDoubleList(
            final @NotNull String key,
            final @Nullable List<Double> def
    ) {
        return getList(key, Transformers.doubleTransformer(), def);
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Float> getFloatList(
            final @NotNull String key,
            final @Nullable List<Float> def
    ) {
        return getList(key, Transformers.floatTransformer(), def);
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Boolean> getBooleanList(
            final @NotNull String key,
            final @Nullable List<Boolean> def
    ) {
        return getList(key, Transformers.booleanTransformer(), def);
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Long> getLongList(final @NotNull String key) {
        return getLongList(key, Collections.emptyList());
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Short> getShortList(final @NotNull String key) {
        return getShortList(key, Collections.emptyList());
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Double> getDoubleList(final @NotNull String key) {
        return getDoubleList(key, Collections.emptyList());
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Float> getFloatList(final @NotNull String key) {
        return getFloatList(key, Collections.emptyList());
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Boolean> getBooleanList(final @NotNull String key) {
        return getBooleanList(key, Collections.emptyList());
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull Object> asMap() {
        return map;
    }

    @Override
    public void setAll(@NotNull Config config) {
        map.putAll(config.asMap());
    }

    @Override
    public void writeTo(final @NotNull File file) {
        try (val os = new FileOutputStream(file)) {
            writeTo(os);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeTo(final @NotNull Path path) {
        try (val os = Files.newOutputStream(path)) {
            writeTo(os);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Config createObject(final @NotNull String key) {
        val object = new LinkedHashMap<String, Object>();
        set(key, object);

        return createObject(object);
    }

    @Override
    public @NotNull ConfigPath walk(@NotNull String path) {
        return SimpleConfigPath.create(this, path);
    }
}
