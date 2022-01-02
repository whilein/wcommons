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
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author whilein
 */
class EventBusTest implements SubscribeNamespace {

    EventBus<EventBusTest> bus;

    @BeforeEach
    void setup() {
        bus = SimpleEventBus.create();
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class NumberEvent implements Event, Cancellable {
        int value;

        @Getter
        @Setter
        @NonFinal
        boolean cancelled;
    }

    @Subscribe(ignoreCancelled = true)
    private void a(final NumberEvent event) {
    }

    @Subscribe
    private void b(final NumberEvent event) {
    }

    @Subscribe(ignoreCancelled = true)
    private void c(final NumberEvent event) {
    }

    @Subscribe
    private void d(final NumberEvent event) {
    }

    @Subscribe(ignoreCancelled = true)
    private void e(final NumberEvent event) {
    }

    @Subscribe
    private void f(final NumberEvent event) {
    }

    @Subscribe(ignoreCancelled = true)
    private void g(final NumberEvent event) {
    }

    @Test
    void testDispatch() {
        val state = new Object() {
            int value;
        };

        bus.register(this, this);

        val subscription = bus.register(this, NumberEvent.class,
                receivedEvent -> state.value = receivedEvent.value);

        bus.dispatch(new NumberEvent(1));
        assertEquals(1, state.value);

        bus.unregister(subscription);
    }
}
