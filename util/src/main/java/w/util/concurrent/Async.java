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

package w.util.concurrent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.util.function.ComponentConsumer;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
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
        return new Uncompleted<>(new Object[0], ComponentConsumer.Atomic.create(), (T) Uncompleted.EMPTY);
    }

    static <T> @NotNull Async<T> supply(final @NotNull Supplier<T> supplier) {
        return supply(supplier, ForkJoinPool.commonPool());
    }

    static <T> @NotNull Async<T> supply(final @NotNull Supplier<T> supplier, final @NotNull Executor executor) {
        val async = Async.<T>uncompleted();

        executor.execute(() -> async.accept(supplier.get()));

        return async;
    }

    void addListener(@NotNull Consumer<T> listener);

    void complete(T value);

    default void accept(final T value) {
        complete(value);
    }

    T get(long millis, int nanos);

    T get(long millis);

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final class Uncompleted<T> implements Async<T> {

        private static final Object EMPTY = new Object[0];

        Object mutex;

        ComponentConsumer<T> listener;

        @NonFinal
        volatile T value;

        @Override
        public void complete(final T value) {
            if (this.value != EMPTY) {
                throw new IllegalStateException("Async is already completed");
            }

            this.value = value;

            synchronized (mutex) {
                mutex.notifyAll();
            }

            this.listener.accept(value);
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
        public void addListener(final @NotNull Consumer<T> listener) {
            this.listener.add(listener);
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
        public void complete(final T value) {
            throw new IllegalStateException("Async is already completed");
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public void addListener(final @NotNull Consumer<T> listener) {
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
