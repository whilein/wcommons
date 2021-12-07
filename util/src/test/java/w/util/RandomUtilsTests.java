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

package w.util;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author whilein
 */
final class RandomUtilsTests {

    List<UUID> randomUuids;

    @BeforeEach
    void setup() {
        randomUuids = Stream.generate(UUID::randomUUID)
                .limit(100)
                .collect(Collectors.toList());
    }

    @Test
    void testGetRandomElements_notDistinct() {
        assertTrue(randomUuids.containsAll(RandomUtils.getRandomElements(randomUuids, 50, false)));
    }

    @Test
    void testGetRandomElements_distinct() {
        val randomElements = RandomUtils.getRandomElements(randomUuids, 50, true);
        assertEquals(randomElements.size(), 50);
        assertEquals(randomUuids.size(), 100); // чтобы remove не удалял из оригинального списка

        assertEquals(50L, randomElements.stream()
                .distinct()
                .count());

        assertTrue(randomUuids.containsAll(randomElements));
    }

    @Test
    void testGetElement() {
        assertTrue(randomUuids.contains(RandomUtils.getElement(randomUuids)));
    }
}
