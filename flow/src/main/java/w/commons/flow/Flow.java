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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author whilein
 */
public interface Flow<T> {

    CompletableFuture<T> toFuture();

    T run() throws Exception;

    T call();

    void callAsync();

    void callAsync(@NotNull Executor executor);

    void callAsync(@NotNull Consumer<T> result);

    void callAsync(@NotNull Consumer<T> result, @NotNull Executor executor);

    <A> @NotNull Flow<A> map(
            @NotNull FlowMapper<T, A> function
    );

    @NotNull IntFlow mapToInt(
            @NotNull ToIntFlowMapper<T> mapper
    );

    @NotNull Flow<@NotNull Optional<T>> toOptional();

    @NotNull Flow<T> orElse(T value);

    @NotNull Flow<T> orElseGet(@NotNull Supplier<T> value);

    @NotNull Flow<T> orElseCall(@NotNull Supplier<@NotNull Flow<T>> value);

    @NotNull Flow<T> filter(
            @NotNull FlowFilter<T> filter
    );

    <A> @NotNull Flow<A> compose(
            @NotNull FlowMapper<T, @NotNull Flow<@NotNull A>> function
    );

    <A> @NotNull Flow<A> then(
            @NotNull Flow<A> another
    );

    <A, R> @NotNull Flow<A> combine(
            @NotNull FlowMapper<T, @NotNull Flow<R>> another,
            @NotNull FlowCombiner<T, R, A> combiner
    );

    <A, R> @NotNull Flow<A> combine(
            @NotNull Flow<R> another,
            @NotNull FlowCombiner<T, R, A> combiner
    );

}
