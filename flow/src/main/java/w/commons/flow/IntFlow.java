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

package w.commons.flow;

import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;
import java.util.concurrent.Executor;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * @author whilein
 */
public interface IntFlow {

    int call();

    int run() throws Exception;

    void callAsync();

    void callAsync(@NotNull Executor executor);

    void callAsync(@NotNull IntConsumer result);

    void callAsync(@NotNull IntConsumer result, @NotNull Executor executor);

    @NotNull IntFlow map(
            @NotNull IntToIntFlowMapper mapper
    );

    <T> @NotNull Flow<T> mapToObj(
            @NotNull IntFlowMapper<T> mapper
    );

    @NotNull
    Flow<@NotNull OptionalInt> toOptional();

    @NotNull IntFlow orElse(int value);

    @NotNull IntFlow orElseGet(@NotNull IntSupplier value);

    @NotNull IntFlow orElseCall(@NotNull Supplier<@NotNull IntFlow> value);

    @NotNull IntFlow filter(
            @NotNull IntFlowFilter filter
    );

    @NotNull IntFlow compose(
            @NotNull IntFlowMapper<@NotNull IntFlow> function
    );

    @NotNull IntFlow then(
            @NotNull IntFlow another
    );

    @NotNull IntFlow combine(
            @NotNull IntFlowMapper<@NotNull IntFlow> another,
            @NotNull IntFlowCombiner combiner
    );

    <A, R> @NotNull IntFlow combine(
            @NotNull IntFlow another,
            @NotNull IntFlowCombiner combiner
    );

}
