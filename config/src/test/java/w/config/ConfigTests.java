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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import w.config.transformer.Transformer;
import w.config.transformer.Transformers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
    void testPath() {
        val presentPath = config.walk("foo.bar.baz");
        val missingPath = config.walk("foo.bar.foo");

        config.createObject("foo").createObject("bar").set("baz", "123");

        assertTrue(presentPath.isPresent());
        assertFalse(missingPath.isPresent());

        assertEquals("123", presentPath.asString());

        try {
            missingPath.asString();

            fail();
        } catch (final ConfigMissingKeyException e) {
            assertEquals("foo.baz.bar", e.getMessage());
        }

        assertTrue(missingPath.asOptionalString().isEmpty());
    }

    @Test
    void testObjectList() {
        config.set("list", List.of(
                Map.of(
                        "x", "y"
                ),
                Map.of(
                        "a", "b"
                )
        ));

        val list = config.getObjectList("list");
        assertEquals(2, list.size());
        assertEquals("y", list.get(0).getString("x"));
        assertEquals("b", list.get(1).getString("a"));
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
    void testByteFromString() {
        assertTransformer((byte) 1, "1", Transformers.byteTransformer());
    }

    @Test
    void testByteFromAnotherNumber() {
        assertTransformer((byte) 2, 2, Transformers.byteTransformer());
    }

    @Test
    void testByteFromByte() {
        assertTransformer((byte) 3, (byte) 3, Transformers.byteTransformer());
    }

    @Test
    void testShortFromString() {
        assertTransformer((short) 1, "1", Transformers.shortTransformer());
    }

    @Test
    void testShortFromAnotherNumber() {
        assertTransformer((short) 2, 2, Transformers.shortTransformer());
    }

    @Test
    void testShortFromShort() {
        assertTransformer((short) 3, (short) 3, Transformers.shortTransformer());
    }

    @Test
    void testIntFromString() {
        assertTransformer(1, "1", Transformers.intTransformer());
    }

    @Test
    void testIntFromAnotherNumber() {
        assertTransformer(2, 2L, Transformers.intTransformer());
    }

    @Test
    void testIntFromInt() {
        assertTransformer(3, 3, Transformers.intTransformer());
    }

    @Test
    void testDoubleFromString() {
        assertTransformer(1.1d, "1.1", Transformers.doubleTransformer());
    }

    @Test
    void testDoubleFromAnotherNumber() {
        assertTransformer(2d, 2L, Transformers.doubleTransformer());
    }

    @Test
    void testDoubleFromDouble() {
        assertTransformer(3.3d, 3.3d, Transformers.doubleTransformer());
    }

    @Test
    void testStringFromBoolean() {
        assertTransformer("true", true, Transformers.stringTransformer());
    }

    @Test
    void testStringFromNumber() {
        assertTransformer("1.1", 1.1, Transformers.stringTransformer());
    }

    @Test
    void testStringFromString() {
        assertTransformer("1.1", "1.1", Transformers.stringTransformer());
    }

    @Test
    void testFloatFromString() {
        assertTransformer(1.1f, "1.1", Transformers.floatTransformer());
    }

    @Test
    void testFloatFromAnotherNumber() {
        assertTransformer(2f, 2L, Transformers.floatTransformer());
    }

    @Test
    void testFloatFromFloat() {
        assertTransformer(3.3f, 3.3f, Transformers.floatTransformer());
    }

    private <T> void assertTransformer(final T expect, final Object from, final Transformer<T> transformer) {
        assertEquals(expect, transformer.transform(from));
    }

}
