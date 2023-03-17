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

package w.util.io;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import w.util.ByteAllocator;

import java.util.Arrays;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class CappedByteOutput extends AbstractByteOutput {

    int position;

    private CappedByteOutput(final byte[] array) {
        super(array);
    }

    public static @NotNull ByteOutput create(final int cap) {
        return new CappedByteOutput(ByteAllocator.INSTANCE.allocate(cap));
    }

    @Override
    public @NotNull ByteOutput write(final int i) {
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
