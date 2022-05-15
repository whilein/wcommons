package w.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * @author whilein
 */
@UtilityClass
public class Env {

    public @NotNull String getString(final @NotNull String key, final @NotNull String defaultValue) {
        return Optional.ofNullable(System.getenv(key)).orElse(defaultValue);
    }

    public int getInt(final @NotNull String key, final  int defaultValue) {
        val value = System.getenv(key);

        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

}
