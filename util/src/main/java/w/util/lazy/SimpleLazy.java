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
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Простейшая реализация {@link Lazy}.
 * <p>
 * Если {@link Lazy#get()} выполняется в разных потоках, то
 * следует использовать {@link ConcurrentLazy}.
 *
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleLazy<T> implements Lazy<T> {

    private static final Object EMPTY = new Object[0];

    Supplier<T> supplier;

    T empty;

    @NonFinal
    T value;

    @SuppressWarnings("unchecked")
    public static <T> @NotNull Lazy<T> create(final @NotNull Supplier<T> supplier) {
        return new SimpleLazy<>(supplier, (T) EMPTY, (T) EMPTY);
    }

    @Override
    public void clear() {
        value = empty;
    }

    @Override
    public T clearAndGet() {
        return value = supplier.get();
    }

    @Override
    public T get() {
        T result = value;

        if (result == empty) {
            value = result = supplier.get();
        }

        return result;
    }
}
