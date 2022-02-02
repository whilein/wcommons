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

package w.util.buffering;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.util.bytes.Bytes;
import w.util.bytes.UncappedBytes;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author whilein
 */
@UtilityClass
public class Buffering {

    private final ThreadLocal<Buffers> BUFFERS = ThreadLocal.withInitial(Buffers::init);

    public @NotNull Buffered<@NotNull StringBuilder> getStringBuilder() {
        return BUFFERS.get().getStringBuilderBuffer().pop();
    }

    public @NotNull Buffered<@NotNull Bytes> getBytes() {
        return BUFFERS.get().getBytesBuffer().pop();
    }

    @FieldDefaults(makeFinal = true)
    @AllArgsConstructor
    private static final class Node<T> {

        T value;

        @NonFinal
        Node<T> next;

    }

    @FieldDefaults(makeFinal = true)
    @AllArgsConstructor
    private static final class BufferedImpl<T> implements Buffered<T> {

        Buffer<T> buffer;

        @NonFinal
        T value;

        @Override
        public T get() {
            return value;
        }

        @Override
        public void release() {
            buffer.push(value);
            value = null;
        }
    }

    @FieldDefaults(makeFinal = true)
    @RequiredArgsConstructor
    private static final class Buffer<T> {

        @NonFinal
        Node<T> root;

        Supplier<T> factory;
        Consumer<T> release;

        public void push(final T value) {
            release.accept(value);

            root = new Node<>(value, root);
        }

        public Buffered<T> pop() {
            final T result;

            if (root != null) {
                val value = root.value;
                root = root.next;
                result = value;
            } else {
                result = factory.get();
            }

            return new BufferedImpl<>(this, result);
        }

    }

    @Getter
    @FieldDefaults(makeFinal = true)
    @RequiredArgsConstructor
    private static final class Buffers {

        Buffer<StringBuilder> stringBuilderBuffer;
        Buffer<Bytes> bytesBuffer;

        public static Buffers init() {
            return new Buffers(
                    new Buffer<>(StringBuilder::new, builder -> builder.setLength(0)),
                    new Buffer<>(UncappedBytes::create, bytes -> bytes.setLength(0)));
        }

    }

}
