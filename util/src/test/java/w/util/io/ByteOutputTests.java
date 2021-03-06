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

package w.util.io;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author whilein
 */
final class ByteOutputTests {

    @Test
    void uncapped_setLength() {
        val uncapped = UncappedByteOutput.create();
        assertEquals(0, uncapped.getLength());
        assertEquals(0, uncapped.getPosition());
        uncapped.write(1);
        uncapped.write(2);
        assertEquals(2, uncapped.getLength());
        assertEquals(2, uncapped.getPosition());
        assertArrayEquals(new byte[]{1, 2}, uncapped.toByteArray());
        uncapped.setPosition(0);
        assertEquals(2, uncapped.getLength());
        assertEquals(0, uncapped.getPosition());
        assertArrayEquals(new byte[]{1, 2}, uncapped.toByteArray());
        uncapped.setLength(0);
        assertEquals(0, uncapped.getLength());
        assertEquals(0, uncapped.getPosition());
        assertArrayEquals(new byte[0], uncapped.toByteArray());
    }

}
