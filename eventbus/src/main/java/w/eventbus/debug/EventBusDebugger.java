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

package w.eventbus.debug;

import org.jetbrains.annotations.NotNull;
import w.eventbus.EventBus;
import w.eventbus.RegisteredSubscription;

import java.util.List;

/**
 * @author whilein
 */
public interface EventBusDebugger {

    void handleBake(
            @NotNull EventBus<?> eventBus,
            byte @NotNull [] output,
            @NotNull String outputTypeName,
            @NotNull Class<?> type,
            @NotNull List<@NotNull RegisteredSubscription> subscriptions,
            long nanos
    );

    default @NotNull EventBusDebugger compose(final @NotNull EventBusDebugger another) {
        return (eventBus, output, outputTypeName, type, subscriptions, nanos) -> {
            this.handleBake(eventBus, output, outputTypeName, type, subscriptions, nanos);
            another.handleBake(eventBus, output, outputTypeName, type, subscriptions, nanos);
        };
    }

}
