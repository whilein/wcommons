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
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 * @author whilein
 */
public interface ComponentPredicate<T> extends Predicate<T> {

    void addAnd(@NotNull Predicate<T> predicate);

    void addOr(@NotNull Predicate<T> predicate);

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final class Simple<T> implements ComponentPredicate<T> {

        Predicate<T> predicate;

        public static <T> @NotNull ComponentPredicate<T> create() {
            return new Simple<>(null);
        }

        public static <T> @NotNull ComponentPredicate<T> create(final @NotNull Predicate<T> initial) {
            return new Simple<>(initial);
        }

        @Override
        public void addAnd(final @NotNull Predicate<T> predicate) {
            val oldPredicate = this.predicate;
            this.predicate = oldPredicate == null ? predicate : oldPredicate.and(this.predicate);
        }

        @Override
        public void addOr(final @NotNull Predicate<T> predicate) {
            val oldPredicate = this.predicate;
            this.predicate = oldPredicate == null ? predicate : oldPredicate.or(this.predicate);
        }

        @Override
        public boolean test(final T t) {
            return predicate != null && predicate.test(t);
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class Atomic<T> implements ComponentPredicate<T> {

        AtomicReference<Predicate<T>> predicate;

        public static <T> @NotNull ComponentPredicate<T> create() {
            return new Atomic<>(new AtomicReference<>());
        }

        public static <T> @NotNull ComponentPredicate<T> create(final @NotNull Predicate<T> initial) {
            return new Atomic<>(new AtomicReference<>(initial));
        }

        @Override
        public void addAnd(final @NotNull Predicate<T> predicate) {
            this.predicate.getAndUpdate(oldPredicate -> oldPredicate == null ? predicate : oldPredicate.and(predicate));
        }

        @Override
        public void addOr(final @NotNull Predicate<T> predicate) {
            this.predicate.getAndUpdate(oldPredicate -> oldPredicate == null ? predicate : oldPredicate.and(predicate));
        }

        @Override
        public boolean test(final T t) {
            val consumer = this.predicate.get();
            return consumer != null && consumer.test(t);
        }
    }

}
