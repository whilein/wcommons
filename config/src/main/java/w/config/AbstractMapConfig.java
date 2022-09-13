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

package w.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Function;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractMapConfig implements Config {
    Map<String, Object> map;

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
    private String _getString(final String key) {
        val value = map.get(key);

        return value instanceof String || value instanceof Number || value instanceof Boolean
          ? value.toString()
          : null;
    }

    @Override
    public @NotNull Optional<@NotNull String> findString(final @NotNull String key) {
        return Optional.ofNullable(_getString(key));
    }

    @Override
    public @NotNull String getString(final @NotNull String key) {
        return require(_getString(key), key);
    }

    @Override
    public @Nullable String getString(final @NotNull String key, final @Nullable String defaultValue) {
        val value = _getString(key);

        return value == null
          ? defaultValue
          : value;
    }

    // endregion
    // region boolean
    private Boolean _getBoolean(final String key) {
        val value = map.get(key);

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        } else if (value instanceof Number numberValue) {
            return numberValue.byteValue() == 1 ? Boolean.TRUE : Boolean.FALSE;
        } else if (value instanceof String) {
            return value.equals("true") ? Boolean.TRUE : Boolean.FALSE;
        }

        return null;
    }

    @Override
    public boolean getBoolean(final @NotNull String key, final boolean defaultValue) {
        val value = _getBoolean(key);

        return value == null
          ? defaultValue
          : value;
    }

    @Override
    public boolean getBoolean(final @NotNull String key) {
        val value = _getBoolean(key);

        if (value == null) {
            throw new ConfigMissingKeyException(key);
        }

        return value;
    }

    @Override
    public @NotNull Optional<Boolean> findBoolean(final @NotNull String key) {
        return Optional.ofNullable(_getBoolean(key));
    }

    // endregion
    // region numbers
    private static final Function<String, Number> INT = Integer::valueOf;
    private static final Function<String, Number> LONG = Long::valueOf;
    private static final Function<String, Number> DOUBLE = Double::valueOf;

    private Number _getNumber(final String key, final Function<String, Number> parse) {
        val value = map.get(key);

        if (value instanceof Number numberValue) {
            return numberValue;
        } else if (value instanceof String stringValue) {
            return parse.apply(stringValue);
        } else {
            return null;
        }
    }

    @Override
    public int getInt(final @NotNull String key) {
        return require(_getNumber(key, INT), key).intValue();
    }

    @Override
    public int getInt(final @NotNull String key, final int defaultValue) {
        val value = _getNumber(key, INT);
        return value == null ? defaultValue : value.intValue();
    }

    @Override
    public @NotNull OptionalInt findInt(final @NotNull String key) {
        val value = _getNumber(key, INT);
        return value == null ? OptionalInt.empty() : OptionalInt.of(value.intValue());
    }

    @Override
    public double getDouble(final @NotNull String key) {
        return require(_getNumber(key, DOUBLE), key).doubleValue();
    }

    @Override
    public double getDouble(final @NotNull String key, final double defaultValue) {
        val value = _getNumber(key, DOUBLE);
        return value == null ? defaultValue : value.doubleValue();
    }

    @Override
    public @NotNull OptionalDouble findDouble(final @NotNull String key) {
        val value = _getNumber(key, DOUBLE);
        return value == null ? OptionalDouble.empty() : OptionalDouble.of(value.doubleValue());
    }

    @Override
    public long getLong(final @NotNull String key) {
        return require(_getNumber(key, LONG), key).longValue();
    }

    @Override
    public long getLong(final @NotNull String key, final long defaultValue) {
        val value = _getNumber(key, LONG);
        return value == null ? defaultValue : value.longValue();
    }

    @Override
    public @NotNull OptionalLong findLong(final @NotNull String key) {
        val value = _getNumber(key, LONG);
        return value == null ? OptionalLong.empty() : OptionalLong.of(value.longValue());
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
        return Optional.ofNullable(map.get(key))
          .map(object -> getAs(object, type));
    }

    @Override
    public <T> @NotNull T getAs(final @NotNull String key, final @NotNull Class<T> type) {
        return getAs(require(map.get(key), key), type);
    }

    @Override
    public <T> @Nullable T getAs(final @NotNull String key,
                                 final @NotNull Class<T> type,
                                 final @Nullable T defaultValue) {
        val value = map.get(key);
        return value == null ? defaultValue : getAs(value, type);
    }

    protected abstract <T> T getAs(Object value, Class<T> type);

    @Override
    public <T> T asType(final @NotNull Class<T> type) {
        return getAs(map, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Config getObject(final @NotNull String key) {
        val value = map.get(key);

        if (value instanceof Map map) {
            return createObject(map);
        }

        throw new ConfigMissingKeyException(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Optional<@NotNull Config> findObject(final @NotNull String key) {
        return Optional.ofNullable(map.get(key))
          .filter(Map.class::isInstance)
          .map(element -> createObject((Map<String, Object>) element));
    }

    // region list
    @Override
    public @Unmodifiable @NotNull List<@NotNull String> getStringList(final @NotNull String key) {
        return getList(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Unmodifiable @NotNull List<@NotNull Config> getObjectList(final @NotNull String key) {
        val value = map.get(key);

        if (value instanceof List list) {
            return list.stream()
              .map(element -> createObject((Map<String, Object>) element))
              .toList();
        }

        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Unmodifiable @NotNull <T> List<T> getList(final @NotNull String key, final @Nullable List<T> def) {
        val value = map.get(key);

        return value instanceof List<?>
          ? List.copyOf((List<T>) value)
          : def == null ? List.of() : def;
    }

    @Override
    public @Unmodifiable @NotNull <T> List<T> getList(final @NotNull String key) {
        return getList(key, null);
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Integer> getIntList(
      final @NotNull String key,
      final List<Integer> def
    ) {
        return getList(key, def);
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Long> getLongList(
      final @NotNull String key,
      final @Nullable List<Long> def
    ) {
        return getList(key, def);
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Short> getShortList(
      final @NotNull String key,
      final @Nullable List<Short> def
    ) {
        return getList(key, def);
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Double> getDoubleList(
      final @NotNull String key,
      final @Nullable List<Double> def
    ) {
        return getList(key, def);
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Float> getFloatList(
      final @NotNull String key,
      final @Nullable List<Float> def
    ) {
        return getList(key, def);
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Boolean> getBooleanList(
      final @NotNull String key,
      final @Nullable List<Boolean> def
    ) {
        return getList(key, def);
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Character> getCharList(
      final @NotNull String key,
      final @Nullable List<Character> def
    ) {
        return getList(key, def);
    }
    // endregion

    @Override
    public @NotNull Map<@NotNull String, @NotNull Object> asMap() {
        return map;
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

}
