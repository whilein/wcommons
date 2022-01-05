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

package w.config;

import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author whilein
 */
final class ConfigObjectTests {

    static ConfigProvider provider;

    @BeforeAll
    static void setup() {
        provider = YamlConfigProvider.create();
    }

    @Test
    void booleanKey() {
        val object = provider.parse("keys:\n" +
                "  yes: 111\n");

        assertFalse(object.isEmpty());
        assertEquals(1, object.size());

        val keys = object.findObject("keys")
                .map(ConfigObject::keySet)
                .orElse(Collections.emptySet());

        assertEquals(Set.of("yes").toString(), keys.toString());
    }

    @Test
    void integerKey() {
        val object = provider.parse("keys:\n" +
                "  123: 111\n");

        assertFalse(object.isEmpty());
        assertEquals(1, object.size());

        val keys = object.findObject("keys")
                .map(ConfigObject::keySet)
                .orElse(Collections.emptySet());

        assertEquals(Set.of("123").toString(), keys.toString());
    }

    @Test
    void parseString() {
        val object = provider.parse("message: 'Hello world!'\n");

        assertFalse(object.isEmpty());
        assertEquals(1, object.size());
        assertEquals("Hello world!", object.getString("message"));
    }

    @Test
    void parseInteger() {
        val object = provider.parse("counter: 1234567890\n");

        assertFalse(object.isEmpty());
        assertEquals(1, object.size());
        assertEquals(1234567890, object.getInt("counter"));
    }

    @Test
    void objectList() {
        val object = provider.parse(
                "items:\n" +
                        "  - id: 'a'\n" +
                        "    counter: 0\n" +
                        "  - id: 'b'\n" +
                        "    counter: 1\n" +
                        "  - id: 'c'\n" +
                        "    counter: 2\n"
        );

        assertFalse(object.isEmpty());
        assertEquals(1, object.size());

        val list = object.getObjectList("items");
        assertEquals(3, list.size());

        for (int i = 0; i < list.size(); i++) {
            val item = list.get(i);

            val id = item.getString("id");
            val counter = item.getInt("counter");

            assertEquals(Character.toString('a' + i), id);
            assertEquals(i, counter);
        }
    }

}
