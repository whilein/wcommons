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

package w.config.mapper;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class NumberMapper<T> extends AbstractMapper<T> {

    private static final AbstractMapper<Byte> BYTE = new NumberMapper<>(
            Byte.class,
            Byte::valueOf,
            Number::byteValue
    );

    private static final AbstractMapper<Short> SHORT = new NumberMapper<>(
            Short.class,
            Short::valueOf,
            Number::shortValue
    );

    private static final AbstractMapper<Integer> INT = new NumberMapper<>(
            Integer.class,
            Integer::valueOf,
            Number::intValue
    );

    private static final AbstractMapper<Long> LONG = new NumberMapper<>(
            Long.class,
            Long::valueOf,
            Number::longValue
    );

    private static final AbstractMapper<Double> DOUBLE = new NumberMapper<>(
            Double.class,
            Double::valueOf,
            Number::doubleValue
    );

    private static final AbstractMapper<Float> FLOAT = new NumberMapper<>(
            Float.class,
            Float::valueOf,
            Number::floatValue
    );

    Function<String, T> fromString;
    Function<Number, T> fromNumber;

    private NumberMapper(
            Class<T> type,
            Function<String, T> fromString,
            Function<Number, T> fromNumber
    ) {
        super(type);

        this.fromString = fromString;
        this.fromNumber = fromNumber;
    }

    public static @NotNull AbstractMapper<Short> shortMapper() {
        return SHORT;
    }

    public static @NotNull AbstractMapper<Byte> byteMapper() {
        return BYTE;
    }

    public static @NotNull AbstractMapper<Integer> intMapper() {
        return INT;
    }

    public static @NotNull AbstractMapper<Long> longMapper() {
        return LONG;
    }

    public static @NotNull AbstractMapper<Double> doubleMapper() {
        return DOUBLE;
    }

    public static @NotNull AbstractMapper<Float> floatMapper() {
        return FLOAT;
    }

    @Override
    protected T doMap(final Object o) {
        if (o instanceof String s) {
            try {
                return fromString.apply(s);
            } catch (final NumberFormatException e) {
                return null;
            }
        }

        if (o instanceof Number n) {
            return fromNumber.apply(n);
        }

        return null;
    }

}
