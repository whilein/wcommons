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

package w.commons.flow;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author whilein
 */
class FlowTests {

    private FlowConsumer<IntFlowSink> numEmitter(final int start, final int end) {
        return emitter -> {
            for (int i = start; i < end; i++) {
                emitter.next(i);
            }
        };
    }

    @Test
    void filter() {
        val result = Flows.ofSupplier(() -> "5")
                .filter("A"::equals)
                .map(Objects::requireNonNull)
                .call();

        assertNull(result);
    }


    @Test
    void intFilter() {
        val result = Flows.ofIntSupplier(() -> 5)
                .filter(x -> x == 1)
                .toOptional()
                .call();

        assertTrue(result.isEmpty());
    }

    @Test
    void collectToList() {
        final List<Number> numbers = Flows.ofIntEmitter(numEmitter(0, 100))
                .mapToObj(Integer::valueOf)
                .collect(Collectors.<Number>toUnmodifiableList())
                .call();

        assertEquals(IntStream.range(0, 100).boxed().collect(Collectors.toList()), numbers);
    }

    @Test
    void mapToInt() {
        val result = Flows.ofSupplier(() -> "5")
                .mapToInt(Integer::parseInt)
                .map(x -> x * x + 1)
                .call();

        assertEquals(26, result);
    }

    @Test
    void itemsFilter_withAnother() {
        val result = Flows.ofIntEmitter(numEmitter(0, 10))
                .mapToObj(String::valueOf)
                .filter("5"::equals)
                .findFirst()
                .filter("123"::equals)
                .orElse("321")
                .call();

        assertEquals("321", result);
    }

    @Test
    void itemsFilter_expectEmpty() {
        val result = Flows.ofIntEmitter(numEmitter(0, 10))
                .mapToObj(String::valueOf)
                .filter("10"::equals)
                .findFirst()
                .call();

        assertNull(result);
    }

    @Test
    void itemsFilter() {
        val result = Flows.ofIntEmitter(numEmitter(0, 10))
                .mapToObj(String::valueOf)
                .filter("5"::equals)
                .findFirst()
                .call();

        assertEquals("5", result);
    }


}