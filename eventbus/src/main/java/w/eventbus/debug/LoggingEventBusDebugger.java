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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import w.eventbus.EventBus;
import w.eventbus.RegisteredSubscription;

import java.util.List;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LoggingEventBusDebugger implements EventBusDebugger {

    public static final @NotNull EventBusDebugger INSTANCE = new LoggingEventBusDebugger(null);

    Logger logger;

    public static @NotNull EventBusDebugger create(final @NotNull Logger logger) {
        return new LoggingEventBusDebugger(logger);
    }

    @Override
    public void handleBake(
            final @NotNull EventBus<?> eventBus,
            final byte @NotNull [] output,
            final @NotNull String outputTypeName,
            final @NotNull Class<?> type,
            final @NotNull List<@NotNull RegisteredSubscription> subscriptions,
            final long nanos
    ) {
        Logger logger = this.logger;

        if (logger == null) {
            logger = eventBus.getLogger();
        }

        logger.debug("Dispatcher for {} ({} subscriptions) baked in {}ms",
                type, subscriptions.size(), Math.round(nanos / 1E4) / 1E2);
    }
}
