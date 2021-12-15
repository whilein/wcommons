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
import java.util.stream.Collectors;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractMapConfigObject implements ConfigObject {
    Map<String, Object> map;

    protected abstract ConfigObject createObject(final Map<String, Object> map);

    @Override
    public @NotNull Map<@NotNull String, @NotNull Object> asMap() {
        return map;
    }

    @Override
    public @NotNull Set<@NotNull String> keySet() {
        return map.keySet();
    }

    @Override
    public @NotNull Collection<@NotNull Object> values() {
        return map.values();
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
    public @NotNull ConfigObject createObject(final @NotNull String key) {
        val object = new LinkedHashMap<String, Object>();
        set(key, object);

        return createObject(object);
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

    @Override
    public @Nullable String getString(final @NotNull String key) {
        return getString(key, null);
    }

    @Override
    public @Nullable String getString(final @NotNull String key, final @Nullable String defaultValue) {
        val value = map.get(key);

        return value instanceof String
                ? (String) value
                : defaultValue;
    }

    @Override
    public boolean getBoolean(final @NotNull String key, final boolean defaultValue) {
        val value = map.get(key);

        if (value instanceof Boolean) {
            return (boolean) value;
        } else if (value instanceof Number) {
            return ((Number) value).byteValue() == 1;
        } else if (value instanceof String) {
            return value.equals("true");
        } else {
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean(final @NotNull String key) {
        return getBoolean(key, false);
    }

    @Override
    public int getInt(final @NotNull String key) {
        return getInt(key, 0);
    }

    @Override
    public double getDouble(final @NotNull String key) {
        return getDouble(key, 0);
    }

    @Override
    public long getLong(final @NotNull String key) {
        return getLong(key, 0);
    }

    @Override
    public int getInt(final @NotNull String key, final int defaultValue) {
        val value = map.get(key);

        if (value instanceof Integer) {
            return (int) value;
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            return defaultValue;
        }
    }

    @Override
    public double getDouble(final @NotNull String key, final double defaultValue) {
        val value = map.get(key);

        if (value instanceof Double) {
            return (double) value;
        } else if (value instanceof String) {
            return Double.parseDouble((String) value);
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else {
            return defaultValue;
        }
    }

    @Override
    public long getLong(final @NotNull String key, final long defaultValue) {
        val value = map.get(key);

        if (value instanceof Long) {
            return (long) value;
        } else if (value instanceof String) {
            return Long.parseLong((String) value);
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else {
            return defaultValue;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable ConfigObject getObject(final @NotNull String key) {
        val value = map.get(key);

        return value instanceof Map
                ? createObject((Map<String, Object>) value)
                : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Unmodifiable @NotNull List<@NotNull String> getStringList(final @NotNull String key) {
        val value = map.get(key);

        return value instanceof List
                ? List.copyOf((List<String>) value)
                : Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Unmodifiable @NotNull List<@NotNull ConfigObject> getObjectList(final @NotNull String key) {
        val value = map.get(key);

        if (value instanceof List) {
            return ((List<?>) value).stream()
                    .map(element -> createObject((Map<String, Object>) element))
                    .collect(Collectors.toUnmodifiableList());
        }

        return Collections.emptyList();
    }

    @Override
    public @NotNull OptionalInt findInt(final @NotNull String key) {
        val value = map.get(key);

        if (value instanceof Integer) {
            return OptionalInt.of((int) value);
        } else if (value instanceof String) {
            return OptionalInt.of(Integer.parseInt((String) value));
        } else if (value instanceof Number) {
            return OptionalInt.of(((Number) value).intValue());
        } else {
            return OptionalInt.empty();
        }
    }

    @Override
    public @NotNull OptionalDouble findDouble(final @NotNull String key) {
        val value = map.get(key);

        if (value instanceof Double) {
            return OptionalDouble.of((double) value);
        } else if (value instanceof String) {
            return OptionalDouble.of(Double.parseDouble((String) value));
        } else if (value instanceof Number) {
            return OptionalDouble.of(((Number) value).intValue());
        } else {
            return OptionalDouble.empty();
        }
    }

    @Override
    public @NotNull OptionalLong findLong(final @NotNull String key) {
        val value = map.get(key);

        if (value instanceof Long) {
            return OptionalLong.of((long) value);
        } else if (value instanceof String) {
            return OptionalLong.of(Long.parseLong((String) value));
        } else if (value instanceof Number) {
            return OptionalLong.of(((Number) value).longValue());
        } else {
            return OptionalLong.empty();
        }
    }

    @Override
    public @NotNull Optional<@NotNull String> findString(final @NotNull String key) {
        return Optional.ofNullable(map.get(key))
                .filter(String.class::isInstance)
                .map(String.class::cast);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Optional<@NotNull ConfigObject> findObject(final @NotNull String key) {
        return Optional.ofNullable(map.get(key))
                .filter(Map.class::isInstance)
                .map(element -> createObject((Map<String, Object>) element));
    }

}
