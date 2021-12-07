package w.util.message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author whilein
 */
public interface Message {

    @NotNull String format();

    @NotNull String format(@Nullable Object @Nullable ... parameters);

    @NotNull List<@NotNull String> formatAsList();

    @NotNull List<@NotNull String> formatAsList(@Nullable Object @Nullable ... parameters);

}
