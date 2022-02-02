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

    private final byte[] EMPTY = new byte[0];

    private final char[] HEX_CHAR_MAP = "0123456789abcdef".toCharArray();

    private final int[] HEX_TENS = new int[8];

    static {
        int hex = 1;

        for (int i = 0, j = HEX_TENS.length; i < j; i++) {
            HEX_TENS[i] = hex;
            hex *= 16;
        }
    }

    public char @NotNull [] toHexChars(final @NotNull ByteSlice byteSlice) {
        return toHexChars(byteSlice.getArray(), byteSlice.getOffset(), byteSlice.getLength());
    }

    public char @NotNull [] toHexChars(final byte @NotNull [] bytes) {
        return toHexChars(bytes, 0, bytes.length);
    }

    public char @NotNull [] toHexChars(final byte @NotNull [] bytes, final int off, final int len) {
        val result = new char[len << 1];

        for (int i = 0, j = 0; i < len; i++) {
            val v = bytes[off + i] & 0xFF;
            result[j++] = HEX_CHAR_MAP[v >>> 4];
            result[j++] = HEX_CHAR_MAP[v & 0x0F];
        }

        return result;
    }

    public @NotNull String toHex(final @NotNull ByteSlice byteSlice) {
        return toHex(byteSlice.getArray(), byteSlice.getOffset(), byteSlice.getLength());
    }

    public @NotNull String toHex(final byte @NotNull [] bytes) {
        return toHex(bytes, 0, bytes.length);
    }

    private int parseChar(final char value) {
        if (value >= '0' && value <= '9') {
            return (value - '0');
        } else if (value >= 'A' && value <= 'Z') {
            return (10 + value - 'A');
        } else if (value >= 'a' && value <= 'z') {
            return (10 + value - 'a');
        } else {
            throw new IllegalArgumentException("Illegal character: " + value);
        }
    }

    public int parseHex(final @NotNull String input) {
        int value = 0;

        for (int i = 0, j = input.length(); i < j; i++) {
            val ch = input.charAt(i);

            int parsed = parseChar(ch);

            if (parsed != 0) {
                parsed *= HEX_TENS[j - i - 1];
                value += parsed;
            }
        }

        return value;
    }

    public byte @NotNull [] fromHex(final @NotNull String input) {
        int length = input.length();

        if (length == 0) {
            return EMPTY;
        }

        if (length == 1) {
            return new byte[]{(byte) parseChar(input.charAt(0))};
        }

        if ((length & 1) != 0) {
            throw new IllegalArgumentException("Input length should be even");
        }

        val data = (byte[]) Root.allocateUninitializedArray(byte.class, length >> 1);

        for (int i = 0; i < length; i += 2) {
            data[((i + 1) >> 1)] = (byte) ((parseChar(input.charAt(i)) << 4) + parseChar(input.charAt(i + 1)));
        }

        return data;
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
