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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import w.config.Config;
import w.config.ConfigMissingKeyException;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * @author whilein
 */
public interface ConfigPath {

    boolean isPresent();

    @NotNull Optional<@NotNull String> asOptionalString();

    <T> @NotNull Optional<T> asOptional(@NotNull Class<T> type);

    @NotNull OptionalInt asOptionalInt();

    @NotNull OptionalLong asOptionalLong();

    @NotNull OptionalDouble asOptionalDouble();

    <T> T asType(@NotNull Class<T> type) throws ConfigMissingKeyException;

    @NotNull String asString() throws ConfigMissingKeyException;

    @Contract("!null -> !null")
    @Nullable String asString(@Nullable String defaultValue);

    @NotNull Object asRaw() throws ConfigMissingKeyException;

    @Contract("!null -> !null")
    @Nullable Object asRaw(@Nullable Object defaultValue);

    boolean asBoolean(boolean defaultValue);

    boolean asBoolean() throws ConfigMissingKeyException;

    int asInt() throws ConfigMissingKeyException;

    double asDouble() throws ConfigMissingKeyException;

    long asLong() throws ConfigMissingKeyException;

    int asInt(int defaultValue);

    double asDouble(double defaultValue);

    long asLong(long defaultValue);

    @NotNull Config asObject() throws ConfigMissingKeyException;

    @Unmodifiable @NotNull List<@NotNull String> asStringList();

    @Unmodifiable @NotNull List<? extends @NotNull Config> asObjectList();

    @Contract("!null -> !null")
    @Unmodifiable @Nullable List<@NotNull Byte> asByteList(@Nullable List<Byte> def);

    @Unmodifiable @NotNull List<@NotNull Byte> asByteList();

    @Contract("!null -> !null")
    @Unmodifiable @Nullable List<@NotNull Integer> asIntList(@Nullable List<Integer> def);

    @Unmodifiable @NotNull List<@NotNull Integer> asIntList();

    @Contract("!null -> !null")
    @Unmodifiable @Nullable List<@NotNull Long> asLongList(@Nullable List<Long> def);

    @Unmodifiable @NotNull List<@NotNull Long> asLongList();

    @Contract("!null -> !null")
    @Unmodifiable @Nullable List<@NotNull Short> asShortList(@Nullable List<Short> def);

    @Unmodifiable @NotNull List<@NotNull Short> asShortList();

    @Contract("!null -> !null")
    @Unmodifiable @Nullable List<@NotNull Double> asDoubleList(@Nullable List<Double> def);

    @Unmodifiable @NotNull List<@NotNull Double> asDoubleList();

    @Contract("!null -> !null")
    @Unmodifiable @Nullable List<@NotNull Float> asFloatList(@Nullable List<Float> def);

    @Unmodifiable @NotNull List<@NotNull Float> asFloatList();

    @Contract("!null -> !null")
    @Unmodifiable @Nullable List<@NotNull Boolean> asBooleanList(@Nullable List<Boolean> def);

    @Unmodifiable @NotNull List<@NotNull Boolean> asBooleanList();

}
