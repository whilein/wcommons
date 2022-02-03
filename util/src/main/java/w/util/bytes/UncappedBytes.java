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
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class UncappedBytes extends AbstractBytes {

    int length;

    private UncappedBytes(final byte[] array) {
        super(array);
    }

    public static @NotNull Bytes create(final int initialCap) {
        return new UncappedBytes(ByteAllocator.INSTANCE.allocate(initialCap));
    }

    public static @NotNull Bytes create() {
        return create(8192);
    }

    @Override
    protected void ensure(final int count) {
        val newPosition = position + count;

        if (newPosition > array.length) {
            array = Arrays.copyOf(array, Math.max(newPosition, array.length * 2));
        }

        length = Math.max(length, newPosition);
    }

    @Override
    public void setLength(final int length) {
        this.length = length;
        this.position = Math.min(length, position);
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public @NotNull String toString() {
        return new String(array, 0, length);
    }

    @Override
    public byte @NotNull [] toByteArray() {
        return Arrays.copyOf(array, length);
    }
}
