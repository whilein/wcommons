package w.utils.pair;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author whilein
 */
public interface MutablePair<L, R> extends Pair<L, R> {

    static <L, R> @NotNull MutablePair<L, R> of(final L left, final R right) {
        return Pairs.mutableOf(left, right);
    }

    static <L, R> @NotNull MutablePair<L, R> of(final Map.Entry<L, R> entry) {
        return Pairs.mutableOf(entry.getKey(), entry.getValue());
    }

    void setLeft(L value);

    void setRight(R value);

    <L1> @NotNull MutablePair<L1, R> withLeft(L1 newValue);

    <R1> @NotNull MutablePair<L, R1> withRight(R1 newValue);

    @NotNull MutablePair<L, R> copy();

}
