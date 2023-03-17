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

package w.util.mutable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.util.ObjectUtils;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * @author whilein
 */
@UtilityClass
public class Mutables {

    public @NotNull MutableLong newLong(final long value) {
        return new MutableLongImpl(value);
    }

    public @NotNull MutableLong newLong() {
        return new MutableLongImpl(0L);
    }

    public @NotNull MutableInt newInt(final int value) {
        return new MutableIntImpl(value);
    }

    public @NotNull MutableInt newInt() {
        return new MutableIntImpl(0);
    }

    public <T> @NotNull MutableReference<T> newReference() {
        return new MutableReferenceImpl<>(null);
    }

    public <T> @NotNull MutableReference<T> newReference(final T value) {
        return new MutableReferenceImpl<>(value);
    }

    public <T> @NotNull MutableOptionalReference<T> newOptionalReference() {
        val empty = ObjectUtils.<T>empty();
        return new MutableOptionalReferenceImpl<>(empty, empty);
    }

    public <T> @NotNull MutableOptionalReference<T> newOptionalReference(final T value) {
        val empty = ObjectUtils.<T>empty();
        return new MutableOptionalReferenceImpl<>(value, empty);
    }

    public @NotNull MutableOptionalLong newOptionalLong() {
        return new MutableOptionalLongImpl(0L, true);
    }

    public @NotNull MutableOptionalLong newOptionalLong(final int value) {
        return new MutableOptionalLongImpl(value, false);
    }

    public @NotNull MutableOptionalInt newOptionalInt() {
        return new MutableOptionalIntImpl(0, true);
    }

    public @NotNull MutableOptionalInt newOptionalInt(final int value) {
        return new MutableOptionalIntImpl(value, false);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class MutableOptionalLongImpl implements MutableOptionalLong {

        long value;

        boolean empty;

        private void ensureNotEmpty() {
            if (empty) {
                throw new IllegalStateException("Value is not present");
            }
        }

        @Override
        public long incrementAndGet() {
            ensureNotEmpty();

            return ++value;
        }

        @Override
        public long getAndIncrement() {
            ensureNotEmpty();

            return value++;
        }

        @Override
        public long decrementAndGet() {
            ensureNotEmpty();

            return --value;
        }

        @Override
        public long getAndDecrement() {
            ensureNotEmpty();

            return value--;
        }

        @Override
        public long get() {
            ensureNotEmpty();

            return value;
        }

        @Override
        public void set(final long value) {
            this.value = value;
            this.empty = false;
        }

        @Override
        public void clear() {
            empty = true;
        }

        @Override
        public <X extends Throwable> long orElseThrow(final @NotNull X cause) throws X {
            if (empty) {
                throw cause;
            }

            return value;
        }

        @Override
        public <X extends Throwable> long orElseThrow(final @NotNull Supplier<X> supplier) throws X {
            if (empty) {
                throw supplier.get();
            }

            return value;
        }

        @Override
        public long orElse(final long value) {
            return empty ? value : this.value;
        }

        @Override
        public long orElseGet(final @NotNull LongSupplier value) {
            return empty ? value.getAsLong() : this.value;
        }

        @Override
        public boolean isEmpty() {
            return empty;
        }

        @Override
        public boolean isPresent() {
            return !empty;
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class MutableOptionalIntImpl implements MutableOptionalInt {

        int value;

        boolean empty;

        private void ensureNotEmpty() {
            if (empty) {
                throw new IllegalStateException("Value is not present");
            }
        }

        @Override
        public int incrementAndGet() {
            ensureNotEmpty();

            return ++value;
        }

        @Override
        public int getAndIncrement() {
            ensureNotEmpty();

            return value++;
        }

        @Override
        public int decrementAndGet() {
            ensureNotEmpty();

            return --value;
        }

        @Override
        public int getAndDecrement() {
            ensureNotEmpty();

            return value--;
        }

        @Override
        public int get() {
            ensureNotEmpty();

            return value;
        }

        @Override
        public void set(final int value) {
            this.value = value;
            this.empty = false;
        }

        @Override
        public void clear() {
            empty = true;
        }

        @Override
        public <X extends Throwable> int orElseThrow(final @NotNull X cause) throws X {
            if (empty) {
                throw cause;
            }

            return value;
        }

        @Override
        public <X extends Throwable> int orElseThrow(final @NotNull Supplier<X> supplier) throws X {
            if (empty) {
                throw supplier.get();
            }

            return value;
        }

        @Override
        public int orElse(final int value) {
            return empty ? value : this.value;
        }

        @Override
        public int orElseGet(final @NotNull IntSupplier value) {
            return empty ? value.getAsInt() : this.value;
        }

        @Override
        public boolean isEmpty() {
            return empty;
        }

        @Override
        public boolean isPresent() {
            return !empty;
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class MutableOptionalReferenceImpl<T> implements MutableOptionalReference<T> {

        @NonFinal
        T value;

        T empty;

        @Override
        public T get() {
            if (value == empty) {
                throw new IllegalStateException("Value is not present");
            }

            return value;
        }

        @Override
        public void set(final T value) {
            this.value = value;
        }

        @Override
        public boolean isNull() {
            return value == null;
        }

        @Override
        public boolean isNotNull() {
            return value != null;
        }

        @Override
        public void clear() {
            value = empty;
        }

        @Override
        public <X extends Throwable> T orElseThrow(final @NotNull X cause) throws X {
            if (value == empty) {
                throw cause;
            }

            return value;
        }

        @Override
        public <X extends Throwable> T orElseThrow(final @NotNull Supplier<X> supplier) throws X {
            if (value == empty) {
                throw supplier.get();
            }

            return value;
        }

        @Override
        public T orElse(final T value) {
            return this.value == empty ? value : this.value;
        }

        @Override
        public T orElseGet(final @NotNull Supplier<T> value) {
            return this.value == empty ? value.get() : this.value;
        }

        @Override
        public boolean isEmpty() {
            return value == empty;
        }

        @Override
        public boolean isPresent() {
            return value != empty;
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class MutableReferenceImpl<T> implements MutableReference<T> {

        T value;

        @Override
        public T get() {
            return value;
        }

        @Override
        public void set(final T value) {
            this.value = value;
        }

        @Override
        public boolean isNull() {
            return value == null;
        }

        @Override
        public boolean isNotNull() {
            return value != null;
        }
    }


    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class MutableLongImpl implements MutableLong {

        long value;

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @Override
        public long incrementAndGet() {
            return ++value;
        }

        @Override
        public long getAndIncrement() {
            return value++;
        }

        @Override
        public long decrementAndGet() {
            return --value;
        }

        @Override
        public long getAndDecrement() {
            return value++;
        }

        @Override
        public long get() {
            return value;
        }

        @Override
        public void set(final long value) {
            this.value = value;
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class MutableIntImpl implements MutableInt {

        int value;

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @Override
        public int incrementAndGet() {
            return ++value;
        }

        @Override
        public int getAndIncrement() {
            return value++;
        }

        @Override
        public int decrementAndGet() {
            return --value;
        }

        @Override
        public int getAndDecrement() {
            return value--;
        }

        @Override
        public int get() {
            return value;
        }

        @Override
        public void set(final int value) {
            this.value = value;
        }

    }

}
