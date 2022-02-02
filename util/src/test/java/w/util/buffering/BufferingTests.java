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

package w.util.buffering;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author whilein
 */
final class BufferingTests {

    @Test
    void test() {
        final StringBuilder x, y;

        try (val result = Buffering.getStringBuilder()) {
            x = result.get();

            try (val nested = Buffering.getStringBuilder()) {
                y = nested.get();
            }
        }

        try (val result = Buffering.getStringBuilder()) {
            assertSame(x, result.get());

            try (val nested = Buffering.getStringBuilder()) {
                assertSame(y, nested.get());
            }
        }
    }

    @Test
    void testNested() {
        try (val first = Buffering.getStringBuilder()) {
            first.get().append("A");

            try (val second = Buffering.getStringBuilder()) {
                second.get().append("B");

                try (val third = Buffering.getStringBuilder()) {
                    third.get().append("C");

                    assertEquals("A", first.get().toString());
                    assertEquals("B", second.get().toString());
                    assertEquals("C", third.get().toString());
                }
            }
        }
    }

    @Test
    void testRelease() {
        try (val result = Buffering.getStringBuilder()) {
            val builder = result.get();
            builder.append("Hello world!");

            assertEquals("Hello world!", builder.toString());
        }

        try (val result = Buffering.getStringBuilder()) {
            assertEquals(0, result.get().length());
        }
    }
}
