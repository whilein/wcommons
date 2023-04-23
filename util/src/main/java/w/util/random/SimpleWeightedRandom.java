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

package w.util.random;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SimpleWeightedRandom<E, T> implements WeightedRandom<T> {

    double sum;

    Collection<E> collection;
    Function<E, T> objectFunction;
    ToDoubleFunction<E> weightFunction;

    T defaultValue;

    public static <T> @NotNull WeightedRandomBuilder<T> builder(
            @NotNull Map<T, Double> map
    ) {
        return new Builder<>(map.entrySet(), Map.Entry::getKey, Map.Entry::getValue);
    }

    public static <T> @NotNull WeightedRandomBuilder<T> builder(
            @NotNull Collection<T> collection,
            @NotNull ToDoubleFunction<T> weightFunction
    ) {
        return new Builder<>(collection, Function.identity(), weightFunction);
    }

    public static <E, T> @NotNull WeightedRandomBuilder<T> builder(
            @NotNull Collection<E> collection,
            @NotNull Function<E, T> objectFunction,
            @NotNull ToDoubleFunction<E> weightFunction
    ) {
        return new Builder<>(collection, objectFunction, weightFunction);
    }

    @Override
    public T nextObject() {
        val collection = this.collection;

        if (collection.isEmpty()) {
            throw new IllegalStateException("Collection is empty");
        }

        val randomizedSum = Math.random() * sum;

        double from = 0.0;

        for (val element : collection) {
            val itemWeight = weightFunction.applyAsDouble(element);

            if (randomizedSum >= from && randomizedSum < from + itemWeight) {
                return objectFunction.apply(element);
            }

            from += itemWeight;
        }

        return defaultValue;
    }

    @FieldDefaults(makeFinal = true)
    @RequiredArgsConstructor
    private static final class Builder<E, T> implements WeightedRandomBuilder<T> {
        Collection<E> collection;
        Function<E, T> objectFunction;
        ToDoubleFunction<E> weightFunction;

        @NonFinal
        boolean manualSum;

        @NonFinal
        double sum;

        @NonFinal
        T defaultValue;

        @Override
        public @NotNull WeightedRandomBuilder<T> sum(double value, T defaultValue) {
            this.manualSum = true;

            this.sum = value;
            this.defaultValue = defaultValue;

            return this;
        }

        @Override
        public @NotNull WeightedRandomBuilder<T> autoSum() {
            manualSum = false;

            return this;
        }

        @Override
        public @NotNull WeightedRandom<T> build() {
            if (collection.isEmpty()) {
                throw new IllegalStateException("Collection is empty");
            }

            val sum = manualSum ? this.sum : collection.stream().mapToDouble(weightFunction).sum();

            return new SimpleWeightedRandom<>(sum, collection, objectFunction, weightFunction, defaultValue);
        }
    }
}
