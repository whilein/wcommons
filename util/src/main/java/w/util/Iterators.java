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

package w.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.ListIterator;

/**
 * @author whilein
 */
@UtilityClass
public class Iterators {

    public <E> @NotNull ListIterator<E> forArray(final E @NotNull [] array) {
        return forArray(array, 0, array.length);
    }

    public <E> @NotNull ListIterator<E> forArray(final E @NotNull [] array, final int offset, final int length) {
        return new OverArray<>(array, offset, length, offset);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class OverArray<E> implements ListIterator<E> {

        E[] array;

        int off;

        int len;

        @NonFinal
        int index;

        @Override
        public boolean hasNext() {
            return index - off != len;
        }

        @Override
        public E next() {
            return array[index++];
        }

        @Override
        public boolean hasPrevious() {
            return index != off;
        }

        @Override
        public E previous() {
            return array[--index];
        }

        @Override
        public int nextIndex() {
            return index - off == len ? len : index + 1;
        }

        @Override
        public int previousIndex() {
            return index == off ? -1 : index - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(final E e) {
            this.array[index] = e;
        }

        @Override
        public void add(final E e) {
            throw new UnsupportedOperationException();
        }
    }

}
