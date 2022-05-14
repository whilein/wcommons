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

package w.eventbus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.util.mutable.MutableInt;
import w.util.mutable.Mutables;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractAsyncEvent implements AsyncEvent {

    Map<Object, MutableInt> intents = new HashMap<>();

    @Getter
    CompletableFuture<AsyncEvent> doneFuture = new CompletableFuture<>();

    Object mutex = new Object[0];

    @NonFinal
    volatile boolean fired;

    @NonFinal
    volatile int latch;

    @Override
    public void postDispatch() {
        synchronized (mutex) {
            if (latch == 0) {
                doneFuture.complete(this);
            }

            fired = true;
        }
    }

    @Override
    public void registerIntent(final @NotNull Object namespace) {
        synchronized (mutex) {
            if (fired) {
                throw new IllegalStateException("Event " + this + " has already been fired");
            }

            intents.computeIfAbsent(namespace, __ -> Mutables.newInt())
                    .incrementAndGet();
            latch++;
        }
    }

    @Override
    public void completeIntent(final @NotNull Object namespace) {
        synchronized (mutex) {
            val intentCount = intents.get(namespace);

            if (intentCount == null || intentCount.get() == 0) {
                throw new IllegalStateException("Plugin " + namespace + " has not registered intents for event " + this);
            }

            intentCount.decrementAndGet();

            val remaining = --latch;

            if (fired && remaining == 0) {
                doneFuture.complete(this);
            }
        }
    }

}
