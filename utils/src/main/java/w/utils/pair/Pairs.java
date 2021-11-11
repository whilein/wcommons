package w.utils.pair;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;

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

        // TODO implement .equals and .hashCode for array left/right

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof Pair)) return false;

            val that = (Pair<?, ?>) obj;

            return Objects.equals(getLeft(), that.getLeft())
                    && Objects.equals(getRight(), that.getRight());
        }

        @Override
        public int hashCode() {
            int hash = 1;
            hash = 31 * hash + Objects.hashCode(getLeft());
            hash = 31 * hash + Objects.hashCode(getRight());

            return hash;
        }

        @Override
        public String toString() {
            return "{left=" + getLeft() + ", right=" + getRight() + "}";
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
        public @NotNull MutablePair<L, R> copy() {
            return new MutablePairImpl<>(left, right);
        }

    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class PairImpl<L, R> extends AbstractPair<L, R> {

        L left;
        R right;

        @Override
        public <L1> @NotNull Pair<L1, R> withLeft(final L1 newValue) {
            return new PairImpl<>(newValue, right);
        }

        @Override
        public <R1> @NotNull Pair<L, R1> withRight(final R1 newValue) {
            return new PairImpl<>(left, newValue);
        }

        @Override
        public @NotNull Pair<L, R> copy() {
            return new PairImpl<>(left, right);
        }

    }

}
