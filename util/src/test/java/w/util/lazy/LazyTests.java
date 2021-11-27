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

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author whilein
 */
final class LazyTests {

    Supplier<UUID> value;

    @BeforeEach
    void setup() {
        value = UUID::randomUUID;
    }

    @Test
    void concurrentLazy1() {
        val lazy = ConcurrentLazy.create(value);

        val threadPool = Executors.newFixedThreadPool(8);

        final Set<UUID> result = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < 64; i++) {
            threadPool.execute(() -> {
                result.add(lazy.get());
            });
        }

        // cannot be returned two different values in concurrent lazy
        assertEquals(1, result.size());
    }

    @Test
    void concurrentLazy0() {
        lazy(ConcurrentLazy.create(value));
    }

    @Test
    void threadLocalLazy() {
        lazy(ThreadLocalLazy.create(value));
    }

    @Test
    void simpleLazy() {
        lazy(SimpleLazy.create(value));
    }

    void lazy(final Lazy<UUID> lazy) {
        val firstValue = lazy.get();
        assertNotNull(firstValue);
        val secondValue = lazy.get();
        assertEquals(firstValue, secondValue);

        lazy.clear();

        val thirdValue = lazy.get();
        assertNotEquals(firstValue, thirdValue);
    }

}
