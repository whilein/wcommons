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

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import w.util.mutable.Mutables;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author whilein
 */
class EventBusTests {

    EventBus bus;

    @BeforeEach
    void setup() {
        bus = SimpleEventBus.create(NamespaceValidator.permitAll());
    }

    @Setter
    @Getter
    private static class IntEvent implements Event {
        int value;
    }

    @Setter
    @Getter
    private static class CancellableIntEvent extends IntEvent implements Cancellable {
        boolean cancelled;
    }

    @Test
    void testInheritance() {
        val listener = new Object() {
            @Subscribe(types = CancellableIntEvent.class)
            public void handleInherited(final IntEvent event) {
                event.value++;
            }

            @Subscribe
            public void handle(final IntEvent event) {
                event.value++;
            }
        };

        bus.register(listener);

        IntEvent event;

        bus.dispatch(event = new IntEvent());
        assertEquals(2, event.value);

        bus.dispatch(event = new CancellableIntEvent());
        assertEquals(1, event.value);
    }

    @Test
    void testCancellableEvent() {
        val firstListener = new Object() {
            @Subscribe(order = PostOrder.LOWEST, ignoreCancelled = true)
            public void handle(final CancellableIntEvent event) {
                event.value += 10;
                event.setCancelled(true);
            }
        };

        val secondListener = new Object() {
            @Subscribe(ignoreCancelled = true)
            public void handle(final CancellableIntEvent event) {
                event.value += 5;
                event.setCancelled(true);
            }
        };

        bus.register(firstListener);
        bus.register(secondListener);

        CancellableIntEvent event;

        bus.dispatch(event = new CancellableIntEvent());
        assertEquals(10, event.value);

        bus.unregisterAll(firstListener);

        bus.dispatch(event = new CancellableIntEvent());
        assertEquals(5, event.value);

        bus.unregisterAll(secondListener);

        bus.dispatch(event = new CancellableIntEvent());
        assertEquals(0, event.value);
    }

    @Test
    void testObjectListener() {
        val listener = new Object() {
            @Subscribe
            public void handle(final IntEvent event) {
                event.value++;
            }
        };

        IntEvent event;

        bus.register(listener);
        bus.dispatch(event = new IntEvent());

        assertEquals(1, event.value);

        bus.unregisterAll(listener);
        bus.dispatch(event = new IntEvent());

        assertEquals(0, event.value);
    }

    @Test
    void testConsumerListener() {
        val subscription = bus.register(
                IntEvent.class,
                event -> event.value++
        );

        IntEvent event;
        bus.dispatch(event = new IntEvent());

        assertEquals(1, event.value);

        bus.unregister(subscription);
        bus.dispatch(event = new IntEvent());

        assertEquals(0, event.value);
    }
}
