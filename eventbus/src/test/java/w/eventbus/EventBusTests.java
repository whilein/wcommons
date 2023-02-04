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

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    public static class IntEvent implements Event {
        int value;
    }

    @Setter
    @Getter
    public static class CancellableIntEvent extends IntEvent implements Cancellable {
        boolean cancelled;
    }

    public static final class TestInheritanceListener {
        @Subscribe(types = CancellableIntEvent.class)
        public void handleInherited(final IntEvent event) {
            event.value++;
        }

        @Subscribe
        public void handle(final IntEvent event) {
            event.value++;
        }
    }

    @Test
    void testInheritance() {
        val listener = new TestInheritanceListener();
        bus.register(listener);

        IntEvent event;

        bus.dispatch(event = new IntEvent());
        assertEquals(2, event.value);

        bus.dispatch(event = new CancellableIntEvent());
        assertEquals(1, event.value);
    }

    public static final class TestCancellableEventFirstListener {
        @Subscribe(order = PostOrder.LOWEST, ignoreCancelled = true)
        public void handle(final CancellableIntEvent event) {
            event.value += 10;
            event.setCancelled(true);
        }
    }

    public static final class TestCancellableEventSecondListener {
        @Subscribe(order = PostOrder.LOWEST, ignoreCancelled = true)
        public void handle(final CancellableIntEvent event) {
            event.value += 5;
            event.setCancelled(true);
        }
    }

    @Test
    void testCancellableEvent() {
        bus.register(new TestCancellableEventFirstListener());
        bus.register(new TestCancellableEventSecondListener());

        CancellableIntEvent event;

        bus.dispatch(event = new CancellableIntEvent());
        assertEquals(10, event.value);

        bus.unregisterAll(TestCancellableEventFirstListener.class);

        bus.dispatch(event = new CancellableIntEvent());
        assertEquals(5, event.value);

        bus.unregisterAll(TestCancellableEventSecondListener.class);

        bus.dispatch(event = new CancellableIntEvent());
        assertEquals(0, event.value);
    }

    public static final class TestObjectListener {
        @Subscribe
        public void handle(final IntEvent event) {
            event.value++;
        }
    }

    @Test
    void testObjectListener() {
        IntEvent event;

        bus.register(new TestObjectListener());
        bus.dispatch(event = new IntEvent());

        assertEquals(1, event.value);

        bus.unregisterAll(TestObjectListener.class);
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

    @Test
    @SneakyThrows
    void testAsyncRegistration() {
        IntEvent event;

        // register listeners
        val executor = Executors.newFixedThreadPool(16);

        final int iterations = 100;

        val latch = new CountDownLatch(iterations);

        for (int i = 0; i < iterations; i++) {
            executor.submit(() -> {
                bus.register(new TestObjectListener());

                latch.countDown();
            });
        }

        latch.await();

        bus.dispatch(event = new IntEvent());

        assertEquals(iterations, event.value);

        bus.unregisterAll(TestObjectListener.class);
        bus.dispatch(event = new IntEvent());

        assertEquals(0, event.value);
    }
}
