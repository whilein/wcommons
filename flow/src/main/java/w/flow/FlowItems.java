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

package w.flow;

import org.jetbrains.annotations.NotNull;
import w.flow.function.FlowConsumer;
import w.flow.function.FlowCountedLoop;
import w.flow.function.FlowMapper;
import w.flow.function.FlowPredicate;
import w.flow.function.Object2IntFlowMapper;

import java.util.concurrent.Executor;

/**
 * @author whilein
 */
public interface FlowItems<T> extends BaseFlowItems {

    <A, R> @NotNull Flow<R> collect(@NotNull FlowCollector<? super T, A, R> collector);

    @NotNull
    Flow<T> findFirst();

    <A> @NotNull Flow<A> mapFirst(
            @NotNull FlowMapper<T, A> function
    );

    @NotNull
    IntFlow mapFirstToInt(
            @NotNull Object2IntFlowMapper<T> function
    );

    <A> @NotNull FlowItems<A> map(
            @NotNull FlowMapper<T, A> function
    );

    <A> @NotNull FlowItems<A> flatMap(
            @NotNull FlowMapper<T, @NotNull FlowItems<A>> fn
    );

    <A> @NotNull FlowItems<A> flatMapParallel(
            @NotNull FlowMapper<T, @NotNull FlowItems<A>> fn
    );

    <A> @NotNull FlowItems<A> flatMapParallel(
            @NotNull FlowMapper<T, @NotNull FlowItems<A>> fn,
            @NotNull Executor executor
    );

    <A> @NotNull FlowItems<A> compose(
            @NotNull FlowMapper<T, @NotNull Flow<A>> fn
    );

    <A> @NotNull FlowItems<A> composeParallel(
            @NotNull FlowMapper<T, @NotNull Flow<A>> fn
    );

    /**
     * Параллельно заменяет все T на A
     *
     * @param fn       Функция, которая заменит T, на {@code Flow&lt;A&gt;}
     * @param <A>      Новое значение
     * @param executor Экзекутор, который выполнит замену
     * @return Течение с новыми значениями
     */
    <A> @NotNull FlowItems<A> composeParallel(
            @NotNull FlowMapper<T, @NotNull Flow<A>> fn,
            @NotNull Executor executor
    );

    @NotNull FlowItems<T> forEachCounted(@NotNull FlowCountedLoop<T> loop);

    @NotNull FlowItems<T> forEach(@NotNull FlowConsumer<T> loop);

    @NotNull
    IntFlowItems mapToInt(
            @NotNull Object2IntFlowMapper<T> mapper
    );

    @NotNull FlowItems<T> filter(
            @NotNull FlowPredicate<T> filter
    );

}
