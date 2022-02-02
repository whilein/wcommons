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

package w.util.bytes;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class CappedBytes extends AbstractBytes {

    int position;

    private CappedBytes(final byte[] array) {
        super(array);
    }

    public static @NotNull Bytes create(final int cap) {
        return new CappedBytes(new byte[cap]);
    }

    @Override
    public @NotNull Bytes write(final int i) {
        ensure(1);

        this.array[position++] = (byte) i;
        return this;
    }

    @Override
    public void setLength(final int length) {
        this.array = Arrays.copyOf(array, length);
    }

    @Override
    public int getLength() {
        return array.length;
    }

    protected void ensure(final int count) {
        if (position + count >= array.length) {
            throw new IllegalStateException("buffer cannot fit " + count + " bytes");
        }
    }

    @Override
    public @NotNull String toString() {
        return new String(array);
    }

    @Override
    public byte @NotNull [] toByteArray() {
        return Arrays.copyOf(array, array.length);
    }
}
