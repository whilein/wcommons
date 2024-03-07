/*
 *    Copyright 2024 Whilein
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

package w.config.path;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import w.config.Config;
import w.config.ConfigMissingKeyException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleConfigPath implements ConfigPath {

    Config root;

    String path;

    @NonFinal
    String name;

    public static @NotNull ConfigPath create(@NotNull Config root, @NotNull String path) {
        return new SimpleConfigPath(root, path);
    }

    private Config getParent() {
        val parent = getParent0();

        if (parent == null) {
            throw new ConfigMissingKeyException(path);
        }

        return parent;
    }

    private Config getParent0() {
        int i1 = -1, i2;
        Config step = root;

        val path = this.path;

        while ((i1 = path.indexOf('.', i2 = i1 + 1)) != -1) {
            step = step.findObject(path.substring(i2, i1))
                    .orElse(null);

            if (step == null) {
                return null;
            }
        }

        if (name == null) {
            name = path.substring(i2);
        }

        return step;
    }

    @Override
    public boolean isPresent() {
        val parent = getParent0();
        return parent != null && parent.contains(name);
    }

    @Override
    public @NotNull Optional<@NotNull String> asOptionalString() {
        val parent = getParent0();

        return parent != null
                ? parent.findString(name)
                : Optional.empty();
    }

    @Override
    public @NotNull <T> Optional<T> asOptional(@NotNull Class<T> type) {
        val parent = getParent0();

        return parent != null
                ? parent.findAs(name, type)
                : Optional.empty();
    }

    @Override
    public @NotNull OptionalInt asOptionalInt() {
        val parent = getParent0();

        return parent != null
                ? parent.findInt(name)
                : OptionalInt.empty();
    }

    @Override
    public @NotNull OptionalLong asOptionalLong() {
        val parent = getParent0();

        return parent != null
                ? parent.findLong(name)
                : OptionalLong.empty();
    }

    @Override
    public @NotNull OptionalDouble asOptionalDouble() {
        val parent = getParent0();

        return parent != null
                ? parent.findDouble(name)
                : OptionalDouble.empty();
    }

    @Override
    public <T> T asType(@NotNull Class<T> type) throws ConfigMissingKeyException {
        val parent = getParent();

        try {
            return parent.getAs(name, type);
        } catch (ConfigMissingKeyException e) {
            throw new ConfigMissingKeyException(path, e);
        }
    }

    @Override
    public @NotNull String asString() throws ConfigMissingKeyException {
        val parent = getParent();

        try {
            return parent.getString(name);
        } catch (ConfigMissingKeyException e) {
            throw new ConfigMissingKeyException(path, e);
        }
    }

    @Override
    public @Nullable String asString(@Nullable String defaultValue) {
        val parent = getParent();

        try {
            return parent.getString(name, defaultValue);
        } catch (ConfigMissingKeyException e) {
            throw new ConfigMissingKeyException(path, e);
        }
    }

    @Override
    public @NotNull Object asRaw() throws ConfigMissingKeyException {
        val parent = getParent();

        try {
            return parent.getRaw(name);
        } catch (ConfigMissingKeyException e) {
            throw new ConfigMissingKeyException(path, e);
        }
    }

    @Override
    public @Nullable Object asRaw(@Nullable Object defaultValue) {
        val parent = getParent();

        try {
            return parent.getRaw(name, defaultValue);
        } catch (ConfigMissingKeyException e) {
            throw new ConfigMissingKeyException(path, e);
        }
    }

    @Override
    public boolean asBoolean(boolean defaultValue) {
        val parent = getParent();

        try {
            return parent.getBoolean(name, defaultValue);
        } catch (ConfigMissingKeyException e) {
            throw new ConfigMissingKeyException(path, e);
        }
    }

    @Override
    public boolean asBoolean() throws ConfigMissingKeyException {
        val parent = getParent();

        try {
            return parent.getBoolean(name);
        } catch (ConfigMissingKeyException e) {
            throw new ConfigMissingKeyException(path, e);
        }
    }

    @Override
    public int asInt() throws ConfigMissingKeyException {
        val parent = getParent();

        try {
            return parent.getInt(name);
        } catch (ConfigMissingKeyException e) {
            throw new ConfigMissingKeyException(path, e);
        }
    }

    @Override
    public double asDouble() throws ConfigMissingKeyException {
        val parent = getParent();

        try {
            return parent.getDouble(name);
        } catch (ConfigMissingKeyException e) {
            throw new ConfigMissingKeyException(path, e);
        }
    }

    @Override
    public long asLong() throws ConfigMissingKeyException {
        val parent = getParent();

        try {
            return parent.getLong(name);
        } catch (ConfigMissingKeyException e) {
            throw new ConfigMissingKeyException(path, e);
        }
    }

    @Override
    public int asInt(int defaultValue) {
        val parent = getParent();

        try {
            return parent.getInt(name, defaultValue);
        } catch (ConfigMissingKeyException e) {
            throw new ConfigMissingKeyException(path, e);
        }
    }

    @Override
    public double asDouble(double defaultValue) {
        val parent = getParent();

        try {
            return parent.getDouble(name, defaultValue);
        } catch (ConfigMissingKeyException e) {
            throw new ConfigMissingKeyException(path, e);
        }
    }

    @Override
    public long asLong(long defaultValue) {
        val parent = getParent();

        try {
            return parent.getLong(name, defaultValue);
        } catch (ConfigMissingKeyException e) {
            throw new ConfigMissingKeyException(path, e);
        }
    }

    @Override
    public @NotNull Config asObject() throws ConfigMissingKeyException {
        val parent = getParent();

        try {
            return parent.getObject(name);
        } catch (ConfigMissingKeyException e) {
            throw new ConfigMissingKeyException(path, e);
        }
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull String> asStringList() {
        val parent = getParent0();

        return parent != null
                ? parent.getStringList(name)
                : Collections.emptyList();
    }

    @Override
    public @Unmodifiable @NotNull List<? extends @NotNull Config> asObjectList() {
        val parent = getParent0();

        return parent != null
                ? parent.getObjectList(name)
                : Collections.emptyList();
    }


    @Override
    public @Unmodifiable @Nullable List<@NotNull Byte> asByteList(@Nullable List<Byte> def) {
        val parent = getParent0();

        return parent != null
                ? parent.getByteList(name, def)
                : def;
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Byte> asByteList() {
        val parent = getParent0();

        return parent != null
                ? parent.getByteList(name)
                : Collections.emptyList();
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Integer> asIntList(@Nullable List<Integer> def) {
        val parent = getParent0();

        return parent != null
                ? parent.getIntList(name, def)
                : def;
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Integer> asIntList() {
        val parent = getParent0();

        return parent != null
                ? parent.getIntList(name)
                : Collections.emptyList();
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Long> asLongList(@Nullable List<Long> def) {
        val parent = getParent0();

        return parent != null
                ? parent.getLongList(name, def)
                : def;
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Long> asLongList() {
        val parent = getParent0();

        return parent != null
                ? parent.getLongList(name)
                : Collections.emptyList();
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Short> asShortList(@Nullable List<Short> def) {
        val parent = getParent0();

        return parent != null
                ? parent.getShortList(name, def)
                : def;
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Short> asShortList() {
        val parent = getParent0();

        return parent != null
                ? parent.getShortList(name)
                : Collections.emptyList();
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Double> asDoubleList(@Nullable List<Double> def) {
        val parent = getParent0();

        return parent != null
                ? parent.getDoubleList(name, def)
                : def;
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Double> asDoubleList() {
        val parent = getParent0();

        return parent != null
                ? parent.getDoubleList(name)
                : Collections.emptyList();
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Float> asFloatList(@Nullable List<Float> def) {
        val parent = getParent0();

        return parent != null
                ? parent.getFloatList(name, def)
                : def;
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Float> asFloatList() {
        val parent = getParent0();

        return parent != null
                ? parent.getFloatList(name)
                : Collections.emptyList();
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Boolean> asBooleanList(@Nullable List<Boolean> def) {
        val parent = getParent0();

        return parent != null
                ? parent.getBooleanList(name, def)
                : def;
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Boolean> asBooleanList() {
        val parent = getParent0();

        return parent != null
                ? parent.getBooleanList(name)
                : Collections.emptyList();
    }

}
