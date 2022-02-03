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

package w.util;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author whilein
 */
final class HexTests {

    @Test
    void toHexZeroPadded() {
        final BigInteger value = new BigInteger("12345", 16);
        assertEquals("00012345", Hex.toHexZeroPadded(value, 8));
        assertEquals("0x00012345", Hex.toHexZeroPadded("0x", value, 8));
    }

    @Test
    void toHex() {
        assertEquals("123456", Hex.toHex(new byte[]{0x12, 0x34, 0x56}));
    }

    @Test
    void fromHex() {
        assertArrayEquals(new byte[]{0x12, 0x34, 0x56}, Hex.fromHex("123456"));
    }

    @Test
    void parseHex() {
        assertEquals(0x123456, Hex.parseHex("123456"));
    }

}
