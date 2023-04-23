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
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.util.RandomUtils;

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

    public static <E, T> @NotNull WeightedRandom<T> create(
            @NotNull Collection<E> collection,
            @NotNull Function<E, T> objectFunction,
            @NotNull ToDoubleFunction<E> weightFunction
    ) {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("Collection cannot be empty");
        }

        val sum = collection.stream()
                .mapToDouble(weightFunction)
                .sum();

        return new SimpleWeightedRandom<>(sum, collection, objectFunction, weightFunction);
    }

    /**
     * Создать заранее подготовленный рандом с весами.
     * <p>
     * Функция веса должна возвращать константное значение для каждого объекта, либо веса
     * не должны превышать изначальную сумму всех весов.
     * В противном случае алгоритм не будет работать корректно.
     * <p>
     * Также нежелательно изменять коллекцию, либо изменять вес каждого элемента, чтобы он
     * не превышал изначальную сумму всех весов.
     * <p>
     * Если у вас изменяется коллекция или веса элементов, то следует использовать
     * {@link RandomUtils#weightedRandom(Collection, ToDoubleFunction)}
     *
     * @param collection     коллекция объектов
     * @param weightFunction функция расчёта веса объекта
     * @param <T>            тип отъекта
     * @return рандом с весами
     */
    public static <T> @NotNull WeightedRandom<T> create(
            @NotNull Collection<T> collection,
            @NotNull ToDoubleFunction<T> weightFunction
    ) {
        return create(collection, Function.identity(), weightFunction);
    }

    public static <T> @NotNull WeightedRandom<T> create(
            @NotNull Map<T, Double> map
    ) {
        return create(map.entrySet(), Map.Entry::getKey, Map.Entry::getValue);
    }

    @Override
    public T nextObject() {
        val collection = this.collection;

        if (collection.isEmpty()) {
            throw new IllegalStateException("Collection is empty");
        }

        val randomizedSum = Math.random() * sum;

        double from = 0.0;

        E result = null;

        for (val element : collection) {
            val itemWeight = weightFunction.applyAsDouble(element);

            if (randomizedSum >= from && randomizedSum < from + itemWeight) {
                result = element;
                break;
            }

            result = element;
            from += itemWeight;
        }

        return objectFunction.apply(result);
    }
}
