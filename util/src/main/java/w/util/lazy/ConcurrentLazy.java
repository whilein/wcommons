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
 * Потоко-безопасная реализация {@link Lazy}, которая гарантированно инициализирует
 * значение только один раз,
 * <p>
 * Если вы используете {@link Lazy#clear()} и {@link Lazy#get()}, то
 * некоторые потоки могут вернуть старое значение.
 *
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConcurrentLazy<T> implements Lazy<T> {

    private static final Object EMPTY = new Object[0];

    Supplier<T> supplier;

    T empty;

    @NonFinal
    volatile T value;

    Object mutex;

    @SuppressWarnings("unchecked")
    public static <T> @NotNull Lazy<T> create(
            final @NotNull Supplier<T> supplier,
            final @NotNull Object mutex
    ) {
        return new ConcurrentLazy<>(supplier, (T) EMPTY, (T) EMPTY, mutex);
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull Lazy<T> create(
            final @NotNull Supplier<T> supplier
    ) {
        return new ConcurrentLazy<>(supplier, (T) EMPTY, (T) EMPTY, new Object[0]);
    }

    @Override
    public void clear() {
        synchronized (mutex) {
            value = empty;
        }
    }

    @Override
    public T clearAndGet() {
        synchronized (mutex) {
            return value = supplier.get();
        }
    }

    @Override
    public T get() {
        T result = value;

        if (result == empty) {
            synchronized (mutex) {
                result = value;

                if (result == empty) {
                    result = value = supplier.get();
                }
            }
        }

        return result;
    }
}
