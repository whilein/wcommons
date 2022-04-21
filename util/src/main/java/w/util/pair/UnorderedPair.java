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
 * @author whilein
 */
public interface UnorderedPair<L, R> extends Pair<L, R> {

    static <L, R> @NotNull UnorderedPair<L, R> of(final L left, final R right) {
        return Pairs.unorderedOf(left, right);
    }

    static <L, R> @NotNull UnorderedPair<L, R> of(final @NotNull Map.Entry<L, R> entry) {
        return Pairs.unorderedOf(entry.getKey(), entry.getValue());
    }

    @NotNull UnorderedPair<R, L> reverse();

    @NotNull Object getGreater();

    @NotNull Object getLower();

    <L1> @NotNull UnorderedPair<L1, R> withLeft(L1 newValue);

    <R1> @NotNull UnorderedPair<L, R1> withRight(R1 newValue);

    @NotNull UnorderedPair<L, R> clone();

    @NotNull UnorderedPair<L, R> deepClone();

}
