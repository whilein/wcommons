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

package w.flow;

import lombok.val;
import org.junit.jupiter.api.Test;
import w.flow.function.FlowConsumer;
import w.flow.function.FlowSink;
import w.flow.function.IntFlowSink;

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

    private FlowConsumer<FlowSink<String>> objectEmitter(final int start, final int end) {
        return emitter -> {
            for (int i = start; i < end; i++) {
                if (!emitter.next(String.valueOf(i))) break;
            }
        };
    }

    private FlowConsumer<IntFlowSink> numEmitter(final int start, final int end) {
        return emitter -> {
            for (int i = start; i < end; i++) {
                if (!emitter.next(i)) break;
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
    void parallel() {
        val result = delayed("Hello ")
                .parallel(delayed("world"), (x, y) -> x + y)
                .parallel(delayed("!"), (x, y) -> x + y)
                .call();

        assertEquals("Hello world!", result);
    }

    private static <T> Flow<T> delayed(final T value) {
        return Flows.ofSupplier(() -> {
            Thread.sleep(1000);

            return value;
        });
    }

    @Test
    void mapFirst() {
        int result;

        result = Flows.ofEmitter(objectEmitter(10, 15))
                .mapFirstToInt(Integer::parseInt)
                .call();

        assertEquals(10, result);

        result = Flows.<String>emptyFlowItems()
                .mapFirstToInt(Integer::parseInt)
                .call();

        assertEquals(0, result);
    }

    @Test
    void mapFirstInt() {
        String result;

        result = IntFlows.ofEmitter(numEmitter(0, 10))
                .mapFirstToObj(String::valueOf)
                .call();

        assertEquals("0", result);

        result = IntFlows.emptyFlowItems()
                .mapFirstToObj(String::valueOf)
                .call();

        assertNull(result);
    }

    @Test
    void intFilter() {
        val result = IntFlows.ofSupplier(() -> 5)
                .filter(x -> x == 1)
                .toOptional()
                .call();

        assertTrue(result.isEmpty());
    }

    @Test
    void collectToList() {
        final List<Number> numbers = IntFlows.ofEmitter(numEmitter(0, 100))
                .mapToObj(Integer::valueOf)
                .collect(FlowCollectors.<Number>toUnmodifiableList())
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
        val result = IntFlows.ofEmitter(numEmitter(0, 10))
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
        val result = IntFlows.ofEmitter(numEmitter(0, 10))
                .mapToObj(String::valueOf)
                .filter("10"::equals)
                .findFirst()
                .call();

        assertNull(result);
    }

    @Test
    void itemsFilter() {
        val result = IntFlows.ofEmitter(numEmitter(0, 10))
                .mapToObj(String::valueOf)
                .filter("5"::equals)
                .findFirst()
                .call();

        assertEquals("5", result);
    }


}