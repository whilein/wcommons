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

package w.flow;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import w.flow.function.FlowCombiner;
import w.flow.function.FlowConsumer;
import w.flow.function.FlowMapper;
import w.flow.function.FlowPredicate;
import w.flow.function.Object2IntFlowMapper;
import w.flow.function.Object2LongFlowMapper;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * @author whilein
 */
public interface Flow<T> extends BaseFlow {

    /**
     * Выполняет флоу асинхронно и результат будет в фьючере
     *
     * @return фьючер
     */
    @NotNull CompletableFuture<T> toFuture();

    @NotNull CompletableFuture<T> toFuture(@NotNull Executor executor);

    @ApiStatus.Internal
    T run() throws Exception;

    T call();

    void callAsync(@NotNull FlowConsumer<T> result);

    void callAsync(@NotNull FlowConsumer<T> result, @NotNull Executor executor);

    <A> @NotNull Flow<A> map(
            @NotNull FlowMapper<T, A> function
    );

    @NotNull IntFlow mapToInt(
            @NotNull Object2IntFlowMapper<T> mapper
    );

    @NotNull LongFlow mapToLong(
            @NotNull Object2LongFlowMapper<T> mapper
    );

    @NotNull Flow<@NotNull Optional<T>> toOptional();

    <A, R> @NotNull Flow<R> parallel(
            @NotNull Flow<A> another,
            @NotNull FlowCombiner<@Nullable T, @Nullable A, R> combiner
    );

    /**
     * Если текущий флоу пустой, то первое значение в combiner'е будет {@code null},
     * а если another пустой, то второе значение будет {@code null}.
     *
     * @param another  Второе значение
     * @param combiner Функция, которая объединит первое и второе значение
     * @param executor Исполнитель функции {@code combiner}
     * @param <A>      Тип второго значения
     * @param <R>      Тип после объединения первого и второго значения
     * @return Результат объединения
     */
    <A, R> @NotNull Flow<R> parallel(
            @NotNull Flow<A> another,
            @NotNull FlowCombiner<@Nullable T, @Nullable A, R> combiner,
            @NotNull Executor executor
    );

    @NotNull Flow<T> orElse(T value);

    @NotNull Flow<T> orElseGet(@NotNull Supplier<T> value);

    @NotNull Flow<T> orElseCall(@NotNull Supplier<@NotNull Flow<T>> value);

    @NotNull Flow<T> filter(
            @NotNull FlowPredicate<T> filter
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
