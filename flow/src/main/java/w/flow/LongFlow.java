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

import org.jetbrains.annotations.NotNull;
import w.flow.function.Long2IntFlowMapper;
import w.flow.function.Long2LongFlowMapper;
import w.flow.function.Long2ObjectFlowMapper;
import w.flow.function.LongFlowCombiner;
import w.flow.function.LongFlowPredicate;

import java.util.OptionalLong;
import java.util.concurrent.Executor;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * @author whilein
 */
public interface LongFlow extends BaseFlow {

    long call();

    long run() throws Exception;

    void callAsync(@NotNull LongConsumer result);

    void callAsync(@NotNull LongConsumer result, @NotNull Executor executor);

    @NotNull LongFlow map(
            @NotNull Long2LongFlowMapper mapper
    );

    @NotNull IntFlow mapToInt(
            @NotNull Long2IntFlowMapper mapper
    );

    <T> @NotNull Flow<T> mapToObj(
            @NotNull Long2ObjectFlowMapper<T> mapper
    );

    @NotNull Flow<@NotNull OptionalLong> toOptional();

    @NotNull LongFlow orElse(long value);

    @NotNull LongFlow orElseGet(@NotNull LongSupplier value);

    @NotNull LongFlow orElseCall(@NotNull Supplier<@NotNull LongFlow> value);

    @NotNull LongFlow filter(
            @NotNull LongFlowPredicate filter
    );

    @NotNull LongFlow compose(
            @NotNull Long2ObjectFlowMapper<@NotNull LongFlow> function
    );

    @NotNull LongFlow then(
            @NotNull LongFlow another
    );

    @NotNull LongFlow combine(
            @NotNull Long2ObjectFlowMapper<@NotNull LongFlow> another,
            @NotNull LongFlowCombiner combiner
    );

    @NotNull LongFlow combine(
            @NotNull LongFlow another,
            @NotNull LongFlowCombiner combiner
    );

}
