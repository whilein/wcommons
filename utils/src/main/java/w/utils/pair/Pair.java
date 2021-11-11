package w.utils.pair;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author whilein
 */
public interface Pair<L, R> {

    static <L, R> @NotNull Pair<L, R> of(final L left, final R right) {
        return Pairs.immutableOf(left, right);
    }

    static <L, R> @NotNull Pair<L, R> of(final Map.Entry<L, R> entry) {
        return Pairs.immutableOf(entry.getKey(), entry.getValue());
    }

    L getLeft();

    R getRight();

    <L1> @NotNull Pair<L1, R> withLeft(L1 newValue);

    <R1> @NotNull Pair<L, R1> withRight(R1 newValue);

    @NotNull Pair<L, R> copy();
    // todo deepCopy();

}
