package w.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * @author whilein
 */
@UtilityClass
public class Env {

    public @NotNull String getString(final @NonNull String key) {
        return findString(key).orElseThrow(() -> new IllegalStateException("Cannot find environment entry: " + key));
    }

    @Contract("_, !null -> !null")
    public @Nullable String getString(final @NonNull String key, final @Nullable String defaultValue) {
        return findString(key).orElse(defaultValue);
    }

    public @NotNull Optional<@NotNull String> findString(final @NonNull String key) {
        return Optional.ofNullable(System.getenv(key));
    }

    public int getInt(final @NonNull String key, final int defaultValue) {
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

    public @Unmodifiable @NotNull List<@NotNull String> getStringList(
        final @NonNull String key,
        final @NonNull String delimiter
    ) {
        return getStringList(key, delimiter, Collections.emptyList());
    }

    @Contract("_, _, !null -> !null")
    public @Unmodifiable @Nullable List<String> getStringList(
        final @NonNull String key,
        final @NonNull String delimiter,
        final @Nullable List<String> defaultValue
    ) {
        val value = System.getenv(key);

        if (value == null) {
            return defaultValue;
        }

        return List.of(value.split(delimiter));
    }

    public @Unmodifiable @NotNull List<@NotNull Integer> getIntList(
        final @NonNull String key,
        final @NonNull String delimiter
    ) {
        return getIntList(key, delimiter, Collections.emptyList());
    }

    @Contract("_, _, !null -> !null")
    public @Unmodifiable @Nullable List<@NotNull Integer> getIntList(
        final @NonNull String key,
        final @NonNull String delimiter,
        final @Nullable List<Integer> defaultValue
    ) {
        val value = System.getenv(key);

        if (value == null) {
            return defaultValue;
        }

        return Arrays.stream(value.split(delimiter))
          .map(Integer::valueOf)
          .toList();
    }
}
