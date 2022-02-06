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
import w.flow.function.Long2IntFlowMapper;
import w.flow.function.Long2LongFlowMapper;
import w.flow.function.Long2ObjectFlowMapper;
import w.flow.function.LongFlowConsumer;
import w.flow.function.LongFlowCountedLoop;
import w.flow.function.LongFlowPredicate;

/**
 * @author whilein
 */
public interface LongFlowItems extends BaseFlowItems {

    <A, R> @NotNull Flow<R> collect(@NotNull LongFlowCollector<A, R> collector);

    @NotNull LongFlow findFirst();

    <A> @NotNull Flow<A> mapFirstToObj(
            @NotNull Long2ObjectFlowMapper<A> function
    );

    @NotNull
    IntFlow mapFirstToInt(
            @NotNull Long2IntFlowMapper function
    );

    @NotNull
    LongFlow mapFirst(
            @NotNull Long2LongFlowMapper function
    );

    @NotNull LongFlowItems map(
            @NotNull Long2LongFlowMapper mapper
    );

    @NotNull IntFlowItems mapToInt(
            @NotNull Long2IntFlowMapper mapper
    );

    <A> @NotNull FlowItems<A> mapToObj(
            @NotNull Long2ObjectFlowMapper<A> mapper
    );

    @NotNull LongFlowItems forEachCounted(@NotNull LongFlowCountedLoop loop);

    @NotNull LongFlowItems forEach(@NotNull LongFlowConsumer loop);

    @NotNull LongFlowItems filter(
            @NotNull LongFlowPredicate filter
    );

}
