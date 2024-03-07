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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author whilein
 */
final class ConfigProviderTests {

    static ConfigProvider provider;

    @BeforeAll
    static void setup() {
        provider = JacksonConfigProvider.create(new ObjectMapper(new YAMLFactory()));
    }

    @Test
    void configDeserializer() {
        record ConfigDeserializerTest(Config nested, List<Config> list, String text) {
        }

        val config = provider.parse("""
                nested:
                  id: 1
                  name: '1'
                list:
                  - id: 1
                    name: '1'
                  - id: 2
                    name: '2'
                text: '3'
                """);

        val result = config.asType(ConfigDeserializerTest.class);
        assertEquals("3", result.text);
        assertEquals(1, result.nested.getInt("id"));
        assertEquals("1", result.nested.getString("name"));
        assertEquals(1, result.list.get(0).getInt("id"));
        assertEquals("1", result.list.get(0).getString("name"));
        assertEquals(2, result.list.get(1).getInt("id"));
        assertEquals("2", result.list.get(1).getString("name"));
    }

    @Test
    void booleanKey() {
        val object = provider.parse("""
                keys:
                  yes: 111
                """);

        assertFalse(object.isEmpty());
        assertEquals(1, object.size());

        val keys = object.findObject("keys")
                .map(Config::keySet)
                .orElse(Collections.emptySet());

        assertEquals(Set.of("yes").toString(), keys.toString());
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    static class Dummy {
        String message;
    }

    @Test
    void asType() {
        val object = provider.parse("message: 'foo bar baz'\n");

        val dummy = object.asType(Dummy.class);
        assertEquals(dummy.getMessage(), "foo bar baz");
    }

    @Test
    void integerKey() {
        val object = provider.parse("""
                keys:
                  123: 111
                """);

        assertFalse(object.isEmpty());
        assertEquals(1, object.size());

        val keys = object.findObject("keys")
                .map(Config::keySet)
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
                """
                        items:
                          - id: 'a'
                            counter: 0
                          - id: 'b'
                            counter: 1
                          - id: 'c'
                            counter: 2
                        """
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
