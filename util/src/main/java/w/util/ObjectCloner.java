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
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.unsafe.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * @author whilein
 */
public interface ObjectCloner {

    @NotNull ObjectCloner INSTANCE = Unsafe.isUnsafeAvailable()
            ? new UnsafeObjectCloner()
            : new SafeObjectCloner();

    @NotNull Object clone(@NotNull Object object);

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class SafeObjectCloner implements ObjectCloner {

        @Override
        @SneakyThrows
        public @NotNull Object clone(final @NotNull Object object) {
            val method = object.getClass().getDeclaredMethod("clone");
            return method.invoke(object);
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class UnsafeObjectCloner implements ObjectCloner {
        private static final MethodHandle CLONE;

        static {
            try {
                val lookup = Unsafe.getUnsafe().trustedLookupIn(Object.class);

                CLONE = lookup.findVirtual(Object.class, "clone", MethodType.methodType(Object.class));
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        @SneakyThrows
        public @NotNull Object clone(final @NotNull Object object) {
            return CLONE.invokeExact(object);
        }
    }

}
