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

package w.util.concurrent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Упрощенная версия Future, для максимальной производительности
 *
 * @author whilein
 */
public interface Async<T> extends Supplier<T>, Consumer<T> {

    static <T> @NotNull Async<T> completed(final T value) {
        return new Completed<>(value);
    }

    @SuppressWarnings("unchecked")
    static <T> @NotNull Async<T> uncompleted() {
        return new Uncompleted<>(new Object[0], new AtomicReference<>(), (T) Uncompleted.EMPTY);
    }

    void accept(@NotNull Consumer<T> listener);

    T get(long millis, int nanos);

    T get(long millis);

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final class Uncompleted<T> implements Async<T> {

        private static final Object EMPTY = new Object[0];

        Object mutex;

        AtomicReference<Consumer<T>> listener;

        @NonFinal
        volatile T value;

        @Override
        public void accept(final T value) {
            if (this.value != EMPTY) {
                throw new IllegalStateException("Async is already completed");
            }

            this.value = value;

            synchronized (mutex) {
                mutex.notifyAll();
            }

            val listener = this.listener.get();

            if (listener != null) {
                listener.accept(value);
            }
        }

        @Override
        public T get() {
            return get(0L, 0);
        }

        @Override
        public T get(final long millis) {
            return get(millis, 0);
        }

        @Override
        public void accept(final @NotNull Consumer<T> listener) {
            this.listener.updateAndGet(oldListener -> oldListener == null ? listener : oldListener.andThen(listener));
        }

        @Override
        public T get(final long millis, final int nanos) {
            if (this.value != EMPTY) {
                return this.value;
            }

            try {
                synchronized (mutex) {
                    mutex.wait(millis, nanos);
                }
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }

            return value;
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class Completed<T> implements Async<T> {

        T value;

        @Override
        public void accept(final T value) {
            throw new IllegalStateException("Async is already completed");
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public void accept(final @NotNull Consumer<T> listener) {
            listener.accept(value);
        }

        @Override
        public T get(final long millis, final int nanos) {
            return value;
        }

        @Override
        public T get(final long millis) {
            return value;
        }
    }

}
