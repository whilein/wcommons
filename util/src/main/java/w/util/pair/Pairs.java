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

package w.util.pair;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import w.util.ObjectCloner;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author whilein
 */
@UtilityClass
public class Pairs {

    public <L, R> @NotNull Pair<L, R> immutableOf(final L left, final R right) {
        return new PairImpl<>(left, right);
    }

    public <L, R> @NotNull MutablePair<L, R> mutableOf(final L left, final R right) {
        return new MutablePairImpl<>(left, right);
    }

    public <L, R> @NotNull UnorderedPair<L, R> unorderedOf(final L left, final R right) {
        val hashLeft = Objects.hashCode(left);
        val hashRight = Objects.hashCode(right);

        final int order;
        final int hash;

        if (hashLeft > hashRight) {
            order = UnorderedPairImpl.LEFT_GREATER;
            hash = 1 + hashLeft * 31 + hashRight;
        } else {
            order = UnorderedPairImpl.RIGHT_GREATER;
            hash = 1 + hashRight * 31 + hashLeft;
        }

        return new UnorderedPairImpl<>(left, right, hash, order);
    }

    public <K, V> @NotNull Predicate<Pair<K, V>> isNull() {
        return pair -> pair.getLeft() == null && pair.getRight() == null;
    }

    public <K, V> @NotNull Predicate<Pair<K, V>> isNotNull() {
        return pair -> pair.getLeft() != null && pair.getRight() != null;
    }

    public <K, V> @NotNull Predicate<Pair<K, V>> isLeftNull() {
        return pair -> pair.getLeft() == null;
    }

    public <K, V> @NotNull Predicate<Pair<K, V>> isRightNull() {
        return pair -> pair.getRight() == null;
    }

    public <K, V> @NotNull Predicate<Pair<K, V>> isLeftNotNull() {
        return pair -> pair.getLeft() != null;
    }

    public <K, V> @NotNull Predicate<Pair<K, V>> isRightNotNull() {
        return pair -> pair.getRight() != null;
    }

    private static abstract class AbstractPair<L, R> implements Pair<L, R> {

        @Override
        public String toString() {
            return "{left=" + getLeft() + ", right=" + getRight() + "}";
        }

        @Override
        @SneakyThrows
        @SuppressWarnings("unchecked")
        public @NotNull Pair<L, R> clone() {
            return (Pair<L, R>) super.clone();
        }

        @Override
        public boolean equals(final Object obj) {
            return obj == this || obj instanceof Pair<?, ?> that && equals(that);
        }

        @Override
        public boolean equals(final @NotNull Pair<?, ?> pair) {
            return Objects.equals(getLeft(), pair.getLeft())
                   && Objects.equals(getRight(), pair.getRight());
        }

        @Override
        public int hashCode() {
            int hash = 1;
            hash = 31 * hash + Objects.hashCode(getLeft());
            hash = 31 * hash + Objects.hashCode(getRight());

            return hash;
        }
    }

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class MutablePairImpl<L, R> extends AbstractPair<L, R> implements MutablePair<L, R> {

        L left;
        R right;

        @Override
        public <L1> @NotNull MutablePair<L1, R> withLeft(final L1 newValue) {
            return new MutablePairImpl<>(newValue, right);
        }

        @Override
        public <R1> @NotNull MutablePair<L, R1> withRight(final R1 newValue) {
            return new MutablePairImpl<>(left, newValue);
        }

        @Override
        public @NotNull MutablePair<L, R> clone() {
            return (MutablePair<L, R>) super.clone();
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull MutablePair<L, R> deepClone() {
            val objectCloner = ObjectCloner.INSTANCE;

            val newLeft = left instanceof Cloneable ? (L) objectCloner.clone(left) : left;
            val newRight = right instanceof Cloneable ? (R) objectCloner.clone(right) : right;

            return new MutablePairImpl<>(newLeft, newRight);
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class UnorderedPairImpl<L, R> extends AbstractPair<L, R> implements UnorderedPair<L, R> {

        @Getter
        L left;

        @Getter
        R right;

        int hash;

        @Getter
        int order;

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final @NotNull Pair<?, ?> pair) {
            if (pair instanceof UnorderedPair<?, ?> that && order != that.getOrder()) {
                return Objects.equals(getLeft(), pair.getRight())
                       && Objects.equals(getRight(), pair.getLeft());
            }

            return super.equals(pair);
        }

        @Override
        public @NotNull UnorderedPair<R, L> reverse() {
            return new UnorderedPairImpl<>(right, left, hash,
                    order == LEFT_GREATER
                            ? RIGHT_GREATER
                            : LEFT_GREATER);
        }

        @Override
        public @NotNull Object getGreater() {
            return order == LEFT_GREATER ? left : right;
        }

        @Override
        public @NotNull Object getLower() {
            return order == LEFT_GREATER ? right : left;
        }

        @Override
        public <L1> @NotNull UnorderedPair<L1, R> withLeft(final L1 newValue) {
            return unorderedOf(newValue, right);
        }

        @Override
        public <R1> @NotNull UnorderedPair<L, R1> withRight(final R1 newValue) {
            return unorderedOf(left, newValue);
        }

        @Override
        public @NotNull UnorderedPair<L, R> deepClone() {
            val objectCloner = ObjectCloner.INSTANCE;

            val newLeft = left instanceof Cloneable ? (L) objectCloner.clone(left) : left;
            val newRight = right instanceof Cloneable ? (R) objectCloner.clone(right) : right;

            // не уверен, что тут может измениться hashCode, но на всякий случай..
            return unorderedOf(newLeft, newRight);
        }

        @Override
        public @NotNull UnorderedPair<L, R> clone() {
            return (UnorderedPair<L, R>) super.clone();
        }
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class PairImpl<L, R> extends AbstractPair<L, R> {

        L left;
        R right;

        @NonFinal
        int hash;

        @Override
        public int hashCode() {
            int hash = this.hash;

            if (hash == 0) {
                hash = this.hash = super.hashCode();
            }

            return hash;
        }

        @Override
        public <L1> @NotNull Pair<L1, R> withLeft(final L1 newValue) {
            return new PairImpl<>(newValue, right);
        }

        @Override
        public <R1> @NotNull Pair<L, R1> withRight(final R1 newValue) {
            return new PairImpl<>(left, newValue);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull Pair<L, R> deepClone() {
            val objectCloner = ObjectCloner.INSTANCE;

            val newLeft = left instanceof Cloneable ? (L) objectCloner.clone(left) : left;
            val newRight = right instanceof Cloneable ? (R) objectCloner.clone(right) : right;

            return new PairImpl<>(newLeft, newRight);
        }

    }

}
