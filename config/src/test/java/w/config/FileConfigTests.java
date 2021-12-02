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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author whilein
 */
final class FileConfigTests {

    File file;

    FileConfig object;

    @BeforeEach
    void before() {
        file = new File("config.yml");

        object = SimpleFileConfig.create(file);
        object.saveDefaults("/config.yml");
    }

    @AfterEach
    void after() {
        file.delete();
    }

    @Test
    void save() {
        assertEquals("foo bar", object.getString("text"));
        object.set("text", "baz qux");
        assertEquals("baz qux", object.getString("text"));
        object.save();
        object.reload();
        assertEquals("baz qux", object.getString("text"));
    }

    @Test
    void reload() {
        assertEquals("foo bar", object.getString("text"));
        object.set("text", "baz qux");
        assertEquals("baz qux", object.getString("text"));
        object.reload();
        assertEquals("foo bar", object.getString("text"));
    }

}
