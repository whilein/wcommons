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

package w.util.message;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author whilein
 */
final class MessageTests {

    @Test
    void commonFormatTest() {
        assertEquals(
                "Игрок§f зашел в игру (§a1§f/§a16§f)",
                Messages.create("%s§f зашел в игру (§a%s§f/§a%s§f)")
                        .format("Игрок", 1, 16)
        );
    }

    @Test
    void noFormat() {
        val text = "Text without formatting";

        val message = Messages.create(text);
        assertEquals(text, message.format());
    }

    @Test
    void escape() {
        val text = "%%1 escaped text %0";

        val message = Messages.create(text);
        assertEquals("%1 escaped text foo", message.format("foo"));
    }

    @Test
    void noFormatList() {
        val text = List.of(
                "Text without formatting",
                "Text without formatting",
                "Text without formatting"
        );

        val message = Messages.create(text);
        assertEquals(text, message.formatAsList());
    }

    @Test
    void oldStyleFormatAsList_atStart() {
        val message = Messages.create(List.of(
                "%s bar baz",
                "foo bar baz",
                "foo bar baz"
        ));

        assertEquals(List.of("foo bar baz", "foo bar baz", "foo bar baz"), message.formatAsList("foo"));
    }

    @Test
    void oldStyleFormatAsList_atEnd() {
        val message = Messages.create(List.of(
                "foo bar baz",
                "foo bar baz",
                "foo bar %s"
        ));

        assertEquals(List.of("foo bar baz", "foo bar baz", "foo bar baz"), message.formatAsList("baz"));
    }

    @Test
    void oldStyleFormatAsList_atCenter() {
        val message = Messages.create(List.of(
                "foo bar baz",
                "foo %s baz",
                "foo bar baz"
        ));

        assertEquals(List.of("foo bar baz", "foo bar baz", "foo bar baz"), message.formatAsList("bar"));
    }

    @Test
    void newStyleFormatAsList_atStart() {
        val message = Messages.create(List.of(
                "%0 bar baz",
                "foo bar baz",
                "foo bar baz"
        ));

        assertEquals(List.of("foo bar baz", "foo bar baz", "foo bar baz"), message.formatAsList("foo"));
    }

    @Test
    void newStyleFormatAsList_atEnd() {
        val message = Messages.create(List.of(
                "foo bar baz",
                "foo bar baz",
                "foo bar %0"
        ));

        assertEquals(List.of("foo bar baz", "foo bar baz", "foo bar baz"), message.formatAsList("baz"));
    }

    @Test
    void newStyleFormatAsList_atCenter() {
        val message = Messages.create(List.of(
                "foo bar baz",
                "foo %0 baz",
                "foo bar baz"
        ));

        assertEquals(List.of("foo bar baz", "foo bar baz", "foo bar baz"), message.formatAsList("bar"));
    }

    @Test
    void oldStyleFormat_atStart() {
        val message = Messages.create("%s bar baz");
        assertEquals("foo bar baz", message.format("foo"));
    }

    @Test
    void oldStyleFormat_atEnd() {
        val message = Messages.create("foo bar %s");
        assertEquals("foo bar baz", message.format("baz"));
    }

    @Test
    void oldStyleFormat_atCenter() {
        val message = Messages.create("foo %s baz");
        assertEquals("foo bar baz", message.format("bar"));
    }

    @Test
    void newStyleFormat_atStart() {
        val message = Messages.create("%0 bar baz");
        assertEquals("foo bar baz", message.format("foo"));
    }

    @Test
    void newStyleFormat_atEnd() {
        val message = Messages.create("foo bar %0");
        assertEquals("foo bar baz", message.format("baz"));
    }

    @Test
    void newStyleFormat_atCenter() {
        val message = Messages.create("foo %0 baz");
        assertEquals("foo bar baz", message.format("bar"));
    }

}
