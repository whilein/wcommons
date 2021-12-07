/*
 *    Copyright 2021 Whilein
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

package w.util.lazy;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Обёртка {@link Lazy} над {@link ThreadLocal}.
 *
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ThreadLocalLazy<T> implements Lazy<T> {

    ThreadLocal<T> threadLocal;

    public static <T> @NotNull Lazy<T> create(final @NotNull Supplier<T> supplier) {
        return new ThreadLocalLazy<>(ThreadLocal.withInitial(supplier));
    }

    @Override
    public void clear() {
        threadLocal.remove();
    }

    @Override
    public T clearAndGet() {
        threadLocal.remove();
        return threadLocal.get();
    }

    @Override
    public T get() {
        return threadLocal.get();
    }
}
