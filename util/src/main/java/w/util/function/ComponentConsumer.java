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

package w.util.function;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author whilein
 */
public interface ComponentConsumer<T> extends Consumer<T> {

    void add(@NotNull Consumer<T> consumer);

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final class Simple<T> implements ComponentConsumer<T> {

        Consumer<T> consumer;

        public static <T> @NotNull ComponentConsumer<T> create() {
            return new Simple<>(null);
        }

        public static <T> @NotNull ComponentConsumer<T> create(final @NotNull Consumer<T> initial) {
            return new Simple<>(initial);
        }

        @Override
        public void add(final @NotNull Consumer<T> consumer) {
            val oldConsumer = this.consumer;

            this.consumer = oldConsumer == null ? consumer : oldConsumer.andThen(consumer);
        }

        @Override
        public void accept(final T t) {
            if (consumer != null) {
                consumer.accept(t);
            }
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final class Atomic<T> implements ComponentConsumer<T> {

        AtomicReference<Consumer<T>> consumer;

        public static <T> @NotNull ComponentConsumer<T> create() {
            return new Atomic<>(new AtomicReference<>());
        }

        public static <T> @NotNull ComponentConsumer<T> create(final @NotNull Consumer<T> initial) {
            return new Atomic<>(new AtomicReference<>(initial));
        }

        @Override
        public void add(final @NotNull Consumer<T> consumer) {
            this.consumer.getAndUpdate(oldConsumer -> oldConsumer == null ? consumer : oldConsumer.andThen(consumer));
        }

        @Override
        public void accept(final T t) {
            val consumer = this.consumer.get();

            if (consumer != null) {
                consumer.accept(t);
            }
        }
    }

}
