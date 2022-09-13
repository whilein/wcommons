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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TODO тесты на raw, object, stringlist, objectlist
 *
 * @author whilein
 */
final class ConfigTests {

    Config config;

    @BeforeEach
    void setup() {
        config = SimpleConfig.create();
    }

    @Test
    void testInts() {
        config.set("str", "123");
        config.set("num", 321);

        // get
        assertEquals(123, config.getInt("str"));
        assertEquals(321, config.getInt("num"));

        // optional
        assertEquals(123, config.findInt("str").orElse(0));
        assertEquals(321, config.findInt("num").orElse(0));

        // missing
        assertTrue(config.findInt("idk").isEmpty());
        assertThrows(ConfigMissingKeyException.class, () -> config.getInt("idk"));

        // default
        assertEquals(123, config.getInt("str", 0));
        assertEquals(321, config.getInt("num", 0));
        assertEquals(666, config.getInt("idk", 666));
    }

    @Test
    void testBooleans() {
        config.set("str", "true");
        config.set("num", 1);
        config.set("bool", false);

        // get
        assertTrue(config.getBoolean("str"));
        assertTrue(config.getBoolean("num"));
        assertFalse(config.getBoolean("bool"));

        // optional
        assertEquals(Boolean.TRUE, config.findBoolean("str").orElse(null));
        assertEquals(Boolean.TRUE, config.findBoolean("num").orElse(null));
        assertEquals(Boolean.FALSE, config.findBoolean("bool").orElse(null));

        // missing
        assertTrue(config.findBoolean("idk").isEmpty());
        assertThrows(ConfigMissingKeyException.class, () -> config.getBoolean("idk"));

        // default
        assertTrue(config.getBoolean("str", false));
        assertTrue(config.getBoolean("num", false));
        assertFalse(config.getBoolean("bool", true));
        assertTrue(config.getBoolean("idk", true));
    }

    @Test
    void testDoubles() {
        config.set("str", "123.1");
        config.set("num", 321.1);

        // get
        assertEquals(123.1, config.getDouble("str"));
        assertEquals(321.1, config.getDouble("num"));

        // optional
        assertEquals(123.1, config.findDouble("str").orElse(0));
        assertEquals(321.1, config.findDouble("num").orElse(0));

        // missing
        assertTrue(config.findDouble("idk").isEmpty());
        assertThrows(ConfigMissingKeyException.class, () -> config.getDouble("idk"));

        // default
        assertEquals(123.1, config.getDouble("str", 0.0));
        assertEquals(321.1, config.getDouble("num", 0.0));
        assertEquals(666.666, config.getDouble("idk", 666.666));
    }

    @Test
    void testLongs() {
        config.set("str", "123");
        config.set("num", 321L);

        // get
        assertEquals(123L, config.getLong("str"));
        assertEquals(321L, config.getLong("num"));

        // optional
        assertEquals(123L, config.findLong("str").orElse(0));
        assertEquals(321L, config.findLong("num").orElse(0));

        // missing
        assertTrue(config.findLong("idk").isEmpty());
        assertThrows(ConfigMissingKeyException.class, () -> config.getLong("idk"));

        // default
        assertEquals(123L, config.getLong("str", 0L));
        assertEquals(321L, config.getLong("num", 0L));
        assertEquals(666L, config.getLong("idk", 666L));
    }

    @Test
    void testStrings() {
        config.set("str", "The string");
        config.set("num", 123);

        // get
        assertEquals("The string", config.getString("str"));
        assertEquals("123", config.getString("num"));

        // optional
        assertEquals("The string", config.findString("str").orElse(null));
        assertEquals("123", config.findString("num").orElse(null));

        // missing
        assertTrue(config.findString("idk").isEmpty());
        assertThrows(ConfigMissingKeyException.class, () -> config.getString("idk"));

        // default
        assertEquals("The string", config.getString("str", null));
        assertEquals("123", config.getString("num", null));
        assertEquals("default", config.getString("idk", "default"));
    }

    @Test
    void testList() {
        config.set("list", List.of("element0", "element1"));

        config.set("intList", List.of(123));
        config.set("charList", List.of('c', 'a'));
        config.set("floatList", List.of(0.123f));
        config.set("longList", List.of(1234567890L));
        config.set("doubleList", List.of(123.321));

        config.set("emptyList", List.of());

        // get
        assertEquals(List.of("element0", "element1"), config.getList("list"));
        assertEquals(List.of(123), config.getList("intList"));
        assertEquals(List.of('c', 'a'), config.getList("charList"));
        assertEquals(List.of(0.123f), config.getList("floatList"));
        assertEquals(List.of(123.321), config.getList("doubleList"));
        assertEquals(List.of(1234567890L), config.getList("longList"));

        assertEquals(List.of(123), config.getIntList("intList"));
        assertEquals(List.of('c', 'a'), config.getCharList("charList"));
        assertEquals(List.of(0.123f), config.getFloatList("floatList"));
        assertEquals(List.of(123.321), config.getDoubleList("doubleList"));
        assertEquals(List.of(1234567890L), config.getLongList("longList"));

        assertTrue(config.getList("emptyList").isEmpty());

        // default
        assertEquals(List.of("element0", "element1"), config.getList("list", null));
        assertEquals(List.of("element0", "element1"), config.getList("idk", List.of("element0", "element1")));
    }
}
