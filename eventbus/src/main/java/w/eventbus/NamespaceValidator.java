package w.eventbus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * @author whilein
 */
public interface NamespaceValidator {

    boolean isValid(@NotNull Object object);

    static @NotNull NamespaceValidator permitAll() {
        return PermitAll.INSTANCE;
    }

    static @NotNull NamespaceValidator permitInstanceOf(final @NotNull Class<?> type) {
        return new PermitInstanceOf(type);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class PermitAll implements NamespaceValidator {

        private static final NamespaceValidator INSTANCE = new PermitAll();

        @Override
        public boolean isValid(final @NotNull Object object) {
            return true;
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class PermitInstanceOf implements NamespaceValidator {
        Class<?> type;

        @Override
        public boolean isValid(final @NotNull Object object) {
            return type.isAssignableFrom(object.getClass());
        }

    }
}
