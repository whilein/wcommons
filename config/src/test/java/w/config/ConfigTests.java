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
import w.config.mapper.Mapper;
import w.config.mapper.NumberMapper;
import w.config.mapper.StringMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TODO тесты на raw, object, stringlist, objectlist
 *
 * @author whilein
 */
final class ConfigTests {

    MutableConfig config;

    @BeforeEach
    void setup() {
        config = InconvertibleMutableConfig.create();
    }

    @Test
    void testEquals() {
        val anotherConfig = InconvertibleMutableConfig.create();
        anotherConfig.set("blablabla", List.of("123123"));

        config.set("blablabla", Arrays.asList("123123"));

        assertEquals(config.hashCode(), anotherConfig.hashCode());
        assertEquals(config, anotherConfig);
    }

    @Test
    void testCopyContents() {
        config.set("number", 1);
        config.set("list", Arrays.asList("1", "2", "3"));
        config.createObject("object").set("string", "321");

        val clonedConfig = config.copyContents();

        assertNotSame(config, clonedConfig);

        { // clone object
            config.getObject("object").set("string", "123");

            assertNotEquals(
                    config.getObject("object").getString("string"),
                    clonedConfig.getObject("object").getString("string")
            );
        }

        { // clone list
            ((List<?>) config.getRaw("list")).set(0, null);

            assertNotEquals(
                    config.getStringList("list"),
                    clonedConfig.getStringList("list")
            );
        }

        assertSame(config.getRaw("number"), clonedConfig.getRaw("number"));
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
        assertMapper((byte) 1, "1", NumberMapper.byteMapper());
    }

    @Test
    void testByteFromAnotherNumber() {
        assertMapper((byte) 2, 2, NumberMapper.byteMapper());
    }

    @Test
    void testByteFromByte() {
        assertMapper((byte) 3, (byte) 3, NumberMapper.byteMapper());
    }

    @Test
    void testShortFromString() {
        assertMapper((short) 1, "1", NumberMapper.shortMapper());
    }

    @Test
    void testShortFromAnotherNumber() {
        assertMapper((short) 2, 2, NumberMapper.shortMapper());
    }

    @Test
    void testShortFromShort() {
        assertMapper((short) 3, (short) 3, NumberMapper.shortMapper());
    }

    @Test
    void testIntFromString() {
        assertMapper(1, "1", NumberMapper.intMapper());
    }

    @Test
    void testIntFromAnotherNumber() {
        assertMapper(2, 2L, NumberMapper.intMapper());
    }

    @Test
    void testIntFromInt() {
        assertMapper(3, 3, NumberMapper.intMapper());
    }

    @Test
    void testDoubleFromString() {
        assertMapper(1.1d, "1.1", NumberMapper.doubleMapper());
    }

    @Test
    void testDoubleFromAnotherNumber() {
        assertMapper(2d, 2L, NumberMapper.doubleMapper());
    }

    @Test
    void testDoubleFromDouble() {
        assertMapper(3.3d, 3.3d, NumberMapper.doubleMapper());
    }

    @Test
    void testStringFromBoolean() {
        assertMapper("true", true, StringMapper.stringMapper());
    }

    @Test
    void testStringFromNumber() {
        assertMapper("1.1", 1.1, StringMapper.stringMapper());
    }

    @Test
    void testStringFromString() {
        assertMapper("1.1", "1.1", StringMapper.stringMapper());
    }

    @Test
    void testFloatFromString() {
        assertMapper(1.1f, "1.1", NumberMapper.floatMapper());
    }

    @Test
    void testFloatFromAnotherNumber() {
        assertMapper(2f, 2L, NumberMapper.floatMapper());
    }

    @Test
    void testFloatFromFloat() {
        assertMapper(3.3f, 3.3f, NumberMapper.floatMapper());
    }

    private <T> void assertMapper(final T expect, final Object from, final Mapper<T> mapper) {
        assertEquals(expect, mapper.mapStrict(from));
    }

}
