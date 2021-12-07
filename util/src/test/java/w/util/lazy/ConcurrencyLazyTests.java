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

package w.util.lazy;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author whilein
 */
final class ConcurrencyLazyTests extends LazyTests {

    private static final int ITERATIONS = 1024;

    @BeforeEach
    void setup() {
        lazy = ConcurrentLazy.create(UUID::randomUUID);
    }

    @Test
    @SneakyThrows
    void testNotAtomic() {
        val threadPool = Executors.newFixedThreadPool(8);
        val latch = new CountDownLatch(ITERATIONS);

        final Set<UUID> uniqueResults = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < ITERATIONS; i++) {
            threadPool.execute(() -> {
                lazy.clear();

                uniqueResults.add(lazy.get());
                latch.countDown();
            });
        }

        latch.await();

        assertNotEquals(ITERATIONS, uniqueResults.size());
    }

    @Test
    @SneakyThrows
    void testAtomic() {
        val threadPool = Executors.newFixedThreadPool(8);
        val latch = new CountDownLatch(ITERATIONS);

        final Set<UUID> uniqueResults = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < ITERATIONS; i++) {
            threadPool.execute(() -> {
                uniqueResults.add(lazy.clearAndGet());
                latch.countDown();
            });
        }

        latch.await();

        assertEquals(ITERATIONS, uniqueResults.size());
    }

    @Test
    @SneakyThrows
    void testConcurrency() {
        val threadPool = Executors.newFixedThreadPool(8);

        val latch = new CountDownLatch(64);

        final Set<UUID> uniqueResults = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < 64; i++) {
            threadPool.execute(() -> {
                uniqueResults.add(lazy.get());
                latch.countDown();
            });
        }

        latch.await();

        assertEquals(1, uniqueResults.size());
    }

}
