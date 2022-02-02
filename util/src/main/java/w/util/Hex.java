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

import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.util.buffering.Buffering;

/**
 * @author whilein
 */
@UtilityClass
public class Hex {

    private final char[] HEX_CHAR_MAP = "0123456789abcdef".toCharArray();

    public char @NotNull [] toHexChars(final byte @NotNull [] bytes, final int off, final int len) {
        val result = new char[len << 1];

        for (int i = 0, j = 0; i < len; i++) {
            val v = bytes[off + i] & 0xFF;
            result[j++] = HEX_CHAR_MAP[v >>> 4];
            result[j++] = HEX_CHAR_MAP[v & 0x0F];
        }

        return result;
    }

    public @NotNull String toHex(final byte @NotNull [] bytes) {
        return toHex(bytes, 0, bytes.length);
    }

    public @NotNull String toHex(final byte @NotNull [] bytes, final int off, final int len) {
        try (val buffered = Buffering.getStringBuilder()) {
            val result = buffered.get();

            for (int i = 0; i < len; i++) {
                val v = bytes[off + i] & 0xFF;
                result.append(HEX_CHAR_MAP[v >>> 4]);
                result.append(HEX_CHAR_MAP[v & 0x0F]);
            }

            return result.toString();
        }
    }

}
