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
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AbstractBytes implements Bytes {

    byte[] array;

    @Getter
    @Setter
    int position;

    protected AbstractBytes(final byte[] array) {
        this.array = array;
    }

    protected abstract void ensure(int count);

    @Override
    public @NotNull Bytes write(final int i) {
        ensure(1);
        this.array[position++] = (byte) i;

        return this;
    }

    @Override
    public @NotNull Bytes write(final @NotNull String text) {
        for (int i = 0, j = text.length(); i < j; i++) {
            val ch = text.charAt(i);

            if (ch < 0x80 && ch != 0) {
                ensure(1);

                this.array[position++] = (byte) ch;
            } else if (ch >= 0x800) {
                ensure(3);

                this.array[position++] = (byte) (0xE0 | ((ch >> 12) & 0x0F));
                this.array[position++] = (byte) (0x80 | ((ch >> 6) & 0x3F));
                this.array[position++] = (byte) (0x80 | (ch & 0x3F));
            } else {
                ensure(2);

                this.array[position++] = (byte) (0xC0 | ((ch >> 6) & 0x1F));
                this.array[position++] = (byte) (0x80 | (ch & 0x3F));
            }
        }

        return this;
    }

    @Override
    public @NotNull Bytes write(final byte @NotNull [] bytes) {
        return write(bytes, 0, bytes.length);
    }

    @Override
    public @NotNull Bytes write(final byte @NotNull [] bytes, final int off, final int len) {
        ensure(len);
        System.arraycopy(bytes, off, array, position, len);
        position += len;

        return this;
    }

    @Override
    public int getCapacity() {
        return array.length;
    }

}
