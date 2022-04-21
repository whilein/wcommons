/*
 *    Copyright 2022 Whilein
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
 * Мутабельная пара значений, можно изменять левое и правое значение.
 * <p>
 * Недостаток этой реализации от обычной иммутабельной пары в том, что hashCode
 * не кешируется.
 *
 * @author whilein
 */
public interface MutablePair<L, R> extends Pair<L, R> {

    static <L, R> @NotNull MutablePair<L, R> of(final L left, final R right) {
        return Pairs.mutableOf(left, right);
    }

    static <L, R> @NotNull MutablePair<L, R> of(final @NotNull Map.Entry<L, R> entry) {
        return Pairs.mutableOf(entry.getKey(), entry.getValue());
    }

    void setLeft(L value);

    void setRight(R value);

    <L1> @NotNull MutablePair<L1, R> withLeft(L1 newValue);

    <R1> @NotNull MutablePair<L, R1> withRight(R1 newValue);

    @NotNull MutablePair<L, R> clone();

    @NotNull MutablePair<L, R> deepClone();

}
