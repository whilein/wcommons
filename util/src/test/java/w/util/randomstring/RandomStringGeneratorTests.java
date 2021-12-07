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

package w.util.randomstring;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author whilein
 */
final class RandomStringGeneratorTests {

    @Test
    void testNumbers() {
        val generator = RandomStringGenerator.builder()
                .addNumbers()
                .build();

        for (int i = 0; i < 1000; i++) {
            assertTrue(generator.nextString(100).matches("\\d{100}"));
        }
    }

    @Test
    void testLetters() {
        val generator = RandomStringGenerator.builder()
                .addLetters()
                .build();

        for (int i = 0; i < 1000; i++) {
            assertTrue(generator.nextString(100).matches("\\w{100}"));
        }
    }

    @Test
    void testCustomDictionary_0() {
        val generator = RandomStringGenerator.builder()
                .setDictionary("хуй")
                .build();

        for (int i = 0; i < 1000; i++) {
            assertTrue(generator.nextString(100).matches("[хуй]{100}"));
        }
    }

    @Test
    void testCustomDictionary_1() {
        val generator = RandomStringGenerator.builder()
                .addDictionary("хуй")
                .addDictionary("пизда")
                .build();

        for (int i = 0; i < 1000; i++) {
            assertTrue(generator.nextString(100).matches("[хуйпизда]{100}"));
        }
    }


}
