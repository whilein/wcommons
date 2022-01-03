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
import w.flow.function.Int2IntFlowMapper;
import w.flow.function.Int2ObjectFlowMapper;
import w.flow.function.IntFlowConsumer;
import w.flow.function.IntFlowCountedLoop;
import w.flow.function.IntFlowPredicate;

/**
 * @author whilein
 */
public interface IntFlowItems extends BaseFlowItems {

    <A, R> @NotNull Flow<R> collect(@NotNull IntFlowCollector<A, R> collector);

    @NotNull IntFlow findFirst();

    <A> @NotNull Flow<A> mapFirstToObj(
            @NotNull Int2ObjectFlowMapper<A> function
    );

    @NotNull
    IntFlow mapFirst(
            @NotNull Int2IntFlowMapper function
    );

    @NotNull IntFlowItems map(
            @NotNull Int2IntFlowMapper mapper
    );

    <A> @NotNull FlowItems<A> mapToObj(
            @NotNull Int2ObjectFlowMapper<A> mapper
    );

    @NotNull IntFlowItems forEachCounted(@NotNull IntFlowCountedLoop loop);

    @NotNull IntFlowItems forEach(@NotNull IntFlowConsumer loop);

    @NotNull IntFlowItems filter(
            @NotNull IntFlowPredicate filter
    );

}
