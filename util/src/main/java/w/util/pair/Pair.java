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

package w.util.pair;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Иммутабельная пара значений.
 *
 * @author whilein
 */
public interface Pair<L, R> extends Cloneable {

    static <L, R> @NotNull Pair<L, R> of(final L left, final R right) {
        return Pairs.immutableOf(left, right);
    }

    static <L, R> @NotNull Pair<L, R> of(final @NotNull Map.Entry<L, R> entry) {
        return Pairs.immutableOf(entry.getKey(), entry.getValue());
    }

    boolean equals(@NotNull Pair<?, ?> pair);

    L getLeft();

    R getRight();

    <L1> @NotNull Pair<L1, R> withLeft(L1 newValue);

    <R1> @NotNull Pair<L, R1> withRight(R1 newValue);

    /**
     * Склонировать пару.
     *
     * @return новая склонированая пара
     */
    @NotNull Pair<L, R> clone();

    /**
     * Склонировать пару.
     * <p>
     * Также, в отличие от {@link #clone()}, клонируется {@code L} и {@code R}, если
     * они наследуют Cloneable.
     *
     * @return новая склонированая пара
     */
    @NotNull Pair<L, R> deepClone();

}
