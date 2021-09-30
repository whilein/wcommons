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

/**
 * @author whilein
 */
public interface IntFlowItems {

    <C> @NotNull Flow<C> collect(@NotNull IntFlowCollector<C> collector);

    @NotNull IntFlow findFirst();

    @NotNull IntFlowItems map(
            @NotNull IntToIntFlowMapper mapper
    );

    <A> @NotNull FlowItems<A> mapToObj(
            @NotNull IntFlowMapper<A> mapper
    );

    @NotNull IntFlowItems forEachCounted(@NotNull IntFlowCountedLoop loop);

    @NotNull IntFlowItems forEach(@NotNull IntFlowConsumer loop);

    @NotNull IntFlowItems filter(
            @NotNull IntFlowFilter filter
    );

    void call();

    void callAsync();

}
