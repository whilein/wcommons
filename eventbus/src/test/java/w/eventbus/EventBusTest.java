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

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import w.eventbus.debug.LoggingEventBusDebugger;
import w.util.ClassLoaderUtils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author whilein
 */
class EventBusTest implements SubscribeNamespace {

    EventBus<EventBusTest> bus;

    @BeforeEach
    void setup() {
        bus = SimpleEventBus.create();
        bus.addDebugger(LoggingEventBusDebugger.INSTANCE);
    }

    public static class EntityDamageEvent implements Event {

    }

    public static class EntityDamageByEntityEvent extends EntityDamageEvent {

    }

    public static class ChildListener {

        @Subscribe
        void catchDamage(final EntityDamageEvent event) {
            System.out.println("DAMAGE CAUGHT: " + event);
        }

    }

    public static class ParentListener extends ChildListener {

        @Subscribe
        void catchDamageByEntity(final EntityDamageByEntityEvent event) {
            System.out.println("DAMAGE BY ENTITY CAUGHT: " + event);
        }

    }

    @Test
    void testInheritance() {
        bus.register(this, new ParentListener());
        // bus.dispatch(new EntityDamageEvent());
        bus.dispatch(new EntityDamageByEntityEvent());
    }

    @Test
    @SneakyThrows
    void testDifferentClassLoaderDispatch() {
        val event = new DummyEvent();
        val cl = EventBusTest.class.getClassLoader();

        val bytes = ClassLoaderUtils.getResourceBytes(cl, "bytecode/DummyListener.class");

        val type = ClassLoaderUtils.defineClass(new URLClassLoader(new URL[0], cl),
                "w.eventbus.DummyListener", bytes);

        val instance = type.newInstance();

        bus.register(this, instance);
        bus.dispatch(event);
        bus.unregisterAll(instance);
    }

    @Test
    void testDispatch() {
        val state = new AtomicBoolean();

        val subscription = bus.register(this, DummyEvent.class,
                receivedEvent -> state.set(true));

        bus.dispatch(new DummyEvent());
        assertTrue(state.get());

        bus.unregister(subscription);
    }
}
