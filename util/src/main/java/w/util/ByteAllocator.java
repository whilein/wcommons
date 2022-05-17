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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import w.unsafe.Unsafe;

/**
 * @author whilein
 */
public interface ByteAllocator {

    @NotNull ByteAllocator INSTANCE = Unsafe.isUnsafeAvailable()
            ? new UnsafeByteAllocator()
            : new SafeByteAllocator();

    byte @NotNull [] allocate(int length);

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class SafeByteAllocator implements ByteAllocator {

        @Override
        public byte @NotNull [] allocate(final int length) {
            return new byte[length];
        }

    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class UnsafeByteAllocator implements ByteAllocator {

        @Override
        public byte @NotNull [] allocate(final int length) {
            return (byte[]) Unsafe.getUnsafe().allocateUninitializedArray(byte.class, length);
        }

    }


}
