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

package w.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.ToDoubleFunction;

/**
 * @author whilein
 */
@UtilityClass
public class RandomUtils {

    private final Random RANDOM = new Random();

    private <T> Collection<T> _getRandomElements(final List<T> in, final int count, final boolean distinct) {
        val list = new ArrayList<T>(count);

        for (int i = 0; i < count; i++) {
            val index = RANDOM.nextInt(in.size());
            list.add(distinct ? in.remove(index) : in.get(index));
        }

        return list;
    }

    public <T> Collection<T> getRandomElements(
            final T @NotNull [] array,
            final int count,
            final boolean distinct
    ) {
        return _getRandomElements(wrapList(List.of(array), distinct), count, distinct);
    }

    public <T> Collection<T> getRandomElements(
            final @NotNull List<T> list,
            final int count,
            final boolean distinct
    ) {
        return _getRandomElements(
                wrapList(list, distinct),
                count, distinct
        );
    }

    private <T> List<T> wrapList(final List<T> list, final boolean distinct) {
        return distinct
                ? new ArrayList<>(list) // копия, ибо будут удаляться элементы
                : list;
    }

    public <T> Collection<T> getRandomElements(
            final @NotNull Collection<T> collection,
            final int count,
            final boolean distinct
    ) {
        return _getRandomElements(
                collection instanceof List
                        ? wrapList((List<T>) collection, distinct)
                        : new ArrayList<>(collection),
                count, distinct
        );
    }

    public <T> @Nullable T weightedRandom(
            final @NotNull Collection<T> items,
            final @NotNull ToDoubleFunction<T> weightCalculator
    ) {
        val sum = items.stream()
                .mapToDouble(weightCalculator)
                .sum();

        val randomizedSum = RANDOM.nextDouble() * sum;

        double from = 0.0;

        T last = null;

        for (val item : items) {
            val itemWeight = weightCalculator.applyAsDouble(item);

            if (randomizedSum >= from && randomizedSum < from + itemWeight) {
                return item;
            }

            last = item;
            from += itemWeight;
        }

        return last;
    }

    public <T> T getRandomElement(final T @NotNull [] array) {
        return array[RANDOM.nextInt(array.length)];
    }

    public <T> T getRandomElement(final @NotNull List<T> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }

    /**
     * @see #getRandomElement(Object[])
     * @deprecated
     */
    @Deprecated
    public <T> T getElement(final T @NotNull [] array) {
        return getRandomElement(array);
    }

    /**
     * @see #getRandomElement(List)
     * @deprecated
     */
    @Deprecated
    public <T> T getElement(final @NotNull List<T> list) {
        return getRandomElement(list);
    }

    public int getInt(final int min, final int max) {
        return RANDOM.nextInt((max - min) + 1) + min;
    }

    public char getLetter(final boolean upperCase) {
        return (char) (upperCase ? getInt('A', 'Z') : getInt('a', 'z'));
    }

    public char getNumber() {
        return (char) getInt('0', '9');
    }

    public int getInt(final int bound) {
        return RANDOM.nextInt(bound);
    }

    public int getInt() {
        return RANDOM.nextInt();
    }

    public boolean getBoolean() {
        return RANDOM.nextBoolean();
    }

}
