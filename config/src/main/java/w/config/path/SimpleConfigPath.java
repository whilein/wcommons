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
import w.config.mapper.BooleanMapper;
import w.config.mapper.Mapper;
import w.config.mapper.NumberMapper;
import w.config.mapper.StringMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;

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

    private <T> @Nullable Mapper<T> mapAsInternal(@NotNull Class<T> type) {
        val parent = getParent0();
        if (parent == null) return null;

        val object = parent.getObject(name, null);
        if (object == null) return null;
        return object.mapAs(type);
    }

    @Override
    public @NotNull <T> Mapper<T> mapAs(@NotNull Class<T> type) {
        val mapper = mapAsInternal(type);
        if (mapper != null) return mapper;
        throw new ConfigMissingKeyException(path);
    }

    @Override
    public <T> @NotNull T as(@NotNull Mapper<T> mapper) throws ConfigMissingKeyException {
        val parent = getParent();

        try {
            return parent.get(name, mapper);
        } catch (ConfigMissingKeyException e) {
            throw new ConfigMissingKeyException(path, e);
        }
    }

    @Override
    public <T> @Nullable T as(@NotNull Mapper<T> mapper, @Nullable T defaultValue) {
        val parent = getParent0();
        return parent != null
                ? parent.get(name, mapper, defaultValue)
                : defaultValue;
    }

    private <T, U> T find(Supplier<T> defaultValue, Function<U, T> wrap, Mapper<U> mapper) {
        val parent = getParent0();
        if (parent == null) return defaultValue.get();

        val result = parent.get(name, mapper, null);
        return result == null
                ? defaultValue.get()
                : wrap.apply(result);
    }

    @Override
    public @NotNull Optional<@NotNull String> asOptionalString() {
        return find(Optional::empty, Optional::of, StringMapper.stringMapper());
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
        return find(OptionalInt::empty, OptionalInt::of, NumberMapper.intMapper());
    }

    @Override
    public @NotNull OptionalLong asOptionalLong() {
        return find(OptionalLong::empty, OptionalLong::of, NumberMapper.longMapper());
    }

    @Override
    public @NotNull OptionalDouble asOptionalDouble() {
        return find(OptionalDouble::empty, OptionalDouble::of, NumberMapper.doubleMapper());
    }

    @Override
    public <T> @NotNull T asType(@NotNull Class<T> type) throws ConfigMissingKeyException {
        val parent = getParent();

        try {
            return parent.getAs(name, type);
        } catch (ConfigMissingKeyException e) {
            throw new ConfigMissingKeyException(path, e);
        }
    }

    @Override
    public <T> @Nullable T asType(@NotNull Class<T> type, @Nullable T defaultValue) {
        val parent = getParent0();
        return parent != null
                ? parent.getAs(name, type, defaultValue)
                : defaultValue;
    }

    @Override
    public @NotNull String asString() throws ConfigMissingKeyException {
        return as(StringMapper.stringMapper());
    }

    @Override
    public @Nullable String asString(@Nullable String defaultValue) {
        return as(StringMapper.stringMapper(), defaultValue);
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
        val parent = getParent0();
        return parent != null
                ? parent.getRaw(name, defaultValue)
                : defaultValue;
    }

    @Override
    public boolean asBoolean() throws ConfigMissingKeyException {
        return as(BooleanMapper.booleanMapper());
    }

    @Override
    public boolean asBoolean(boolean defaultValue) {
        return as(BooleanMapper.booleanMapper(), defaultValue);
    }

    @Override
    public int asInt() throws ConfigMissingKeyException {
        return as(NumberMapper.intMapper());
    }

    @Override
    public double asDouble() throws ConfigMissingKeyException {
        return as(NumberMapper.doubleMapper());
    }

    @Override
    public long asLong() throws ConfigMissingKeyException {
        return as(NumberMapper.longMapper());
    }

    @Override
    public int asInt(int defaultValue) {
        return as(NumberMapper.intMapper(), defaultValue);
    }

    @Override
    public double asDouble(double defaultValue) {
        return as(NumberMapper.doubleMapper(), defaultValue);
    }

    @Override
    public long asLong(long defaultValue) {
        return as(NumberMapper.longMapper(), defaultValue);
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
    public @Unmodifiable <T> @Nullable List<T> asList(@NotNull Mapper<T> mapper, @Nullable List<T> def) {
        val parent = getParent0();
        return parent != null
                ? parent.getList(name, mapper, def)
                : def;
    }

    @Override
    public @Unmodifiable <T> @NotNull List<T> asList(@NotNull Mapper<T> mapper) {
        return asList(mapper, Collections.emptyList());
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull String> asStringList() {
        return asList(StringMapper.stringMapper());
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
        return asList(NumberMapper.byteMapper(), def);
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Byte> asByteList() {
        return asList(NumberMapper.byteMapper());
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Integer> asIntList(@Nullable List<Integer> def) {
        return asList(NumberMapper.intMapper(), def);
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Integer> asIntList() {
        return asList(NumberMapper.intMapper());
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Long> asLongList(@Nullable List<Long> def) {
        return asList(NumberMapper.longMapper(), def);
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Long> asLongList() {
        return asList(NumberMapper.longMapper());
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Short> asShortList(@Nullable List<Short> def) {
        return asList(NumberMapper.shortMapper(), def);
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Short> asShortList() {
        return asList(NumberMapper.shortMapper());
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Double> asDoubleList(@Nullable List<Double> def) {
        return asList(NumberMapper.doubleMapper(), def);
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Double> asDoubleList() {
        return asList(NumberMapper.doubleMapper());
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Float> asFloatList(@Nullable List<Float> def) {
        return asList(NumberMapper.floatMapper(), def);
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Float> asFloatList() {
        return asList(NumberMapper.floatMapper());
    }

    @Override
    public @Unmodifiable @Nullable List<@NotNull Boolean> asBooleanList(@Nullable List<Boolean> def) {
        return asList(BooleanMapper.booleanMapper(), def);
    }

    @Override
    public @Unmodifiable @NotNull List<@NotNull Boolean> asBooleanList() {
        return asList(BooleanMapper.booleanMapper());
    }

}
