/*
 *    Copyright 2023 Whilein
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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * @author whilein
 */
public interface ByteSlice {

    static @NotNull ByteSlice wrap(final byte[] array) {
        return new Default(array, 0, array.length);
    }

    static @NotNull ByteSlice wrap(final byte[] array, final int off, final int len) {
        return new Default(array, off, len);
    }

    int getOffset();

    int getLength();

    byte get(int position);

    @NotNull ByteSlice slice(int off, int len);

    byte @NotNull [] getSlice();

    byte @NotNull [] getArray();

    @NotNull String toString();

    @NotNull BigInteger toBigInt();

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class Default implements ByteSlice {

        byte[] array;
        int offset;
        int length;

        @Override
        public byte get(final int position) {
            return array[position + offset];
        }

        @Override
        public @NotNull ByteSlice slice(final int off, final int len) {
            return new Default(array, offset + off, len);
        }

        @Override
        public byte @NotNull [] getSlice() {
            return Arrays.copyOfRange(array, offset, offset + length);
        }

        @Override
        public @NotNull String toString() {
            return new String(array, offset, length);
        }

        @Override
        public @NotNull BigInteger toBigInt() {
            return new BigInteger(1, array, offset, length);
        }

    }

}
