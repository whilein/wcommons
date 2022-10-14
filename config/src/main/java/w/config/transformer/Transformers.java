package w.config.transformer;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author whilein
 */
@UtilityClass
public class Transformers {
    private static final Transformer<String> STRING = new StringTransformer();

    private static final Transformer<Boolean> BOOLEAN = new BooleanTransformer();

    private static final Transformer<Byte> BYTE = new NumberTransformer<>(
            Byte.class,
            Byte::valueOf,
            Number::byteValue
    );

    private static final Transformer<Short> SHORT = new NumberTransformer<>(
            Short.class,
            Short::valueOf,
            Number::shortValue
    );

    private static final Transformer<Integer> INT = new NumberTransformer<>(
            Integer.class,
            Integer::valueOf,
            Number::intValue
    );

    private static final Transformer<Long> LONG = new NumberTransformer<>(
            Long.class,
            Long::valueOf,
            Number::longValue
    );

    private static final Transformer<Double> DOUBLE = new NumberTransformer<>(
            Double.class,
            Double::valueOf,
            Number::doubleValue
    );

    private static final Transformer<Float> FLOAT = new NumberTransformer<>(
            Float.class,
            Float::valueOf,
            Number::floatValue
    );

    public static @NotNull Transformer<String> stringTransformer() {
        return STRING;
    }

    public static @NotNull Transformer<Short> shortTransformer() {
        return SHORT;
    }

    public static @NotNull Transformer<Byte> byteTransformer() {
        return BYTE;
    }

    public static @NotNull Transformer<Integer> intTransformer() {
        return INT;
    }

    public static @NotNull Transformer<Long> longTransformer() {
        return LONG;
    }

    public static @NotNull Transformer<Double> doubleTransformer() {
        return DOUBLE;
    }

    public static @NotNull Transformer<Float> floatTransformer() {
        return FLOAT;
    }

    public static @NotNull Transformer<Boolean> booleanTransformer() {
        return BOOLEAN;
    }

    private static final class NumberTransformer<T> extends AbstractTransformer<T> {

        Function<String, T> fromString;
        Function<Number, T> fromNumber;

        private NumberTransformer(
                final Class<T> type,
                final Function<String, T> fromString,
                final Function<Number, T> fromNumber
        ) {
            super(type);

            this.fromString = fromString;
            this.fromNumber = fromNumber;
        }

        @Override
        protected T doTransform(final Object o) {
            if (o instanceof String s) {
                try {
                    return fromString.apply(s);
                } catch (final NumberFormatException e) {
                    return null;
                }
            }

            if (o instanceof Number n) {
                return fromNumber.apply(n);
            }

            return null;
        }

    }


    private static final class StringTransformer extends AbstractTransformer<String> {

        private StringTransformer() {
            super(String.class);
        }

        @Override
        protected String doTransform(final Object o) {
            return o instanceof List<?> || o instanceof Map<?, ?> ? null : o.toString();
        }

    }

    private static final class BooleanTransformer extends AbstractTransformer<Boolean> {

        public BooleanTransformer() {
            super(Boolean.class);
        }

        @Override
        protected Boolean doTransform(final Object o) {
            if (o instanceof String s) {
                return Boolean.valueOf(s);
            }

            if (o instanceof Number n) {
                return n.intValue() == 1;
            }

            return null;
        }

    }

}
