package w.config.transformer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractTransformer<T> implements Transformer<T> {

    Class<T> type;

    protected abstract T doTransform(Object o);

    private T transform0(final Object o) {
        if (type.isAssignableFrom(o.getClass())) {
            return type.cast(o);
        }

        return doTransform(o);
    }


    @Override
    public T transformOrNull(final Object o) {
        return o == null ? null : transform0(o);
    }

    @Override
    public T transform(final Object o) {
        if (o == null) {
            return null;
        }

        val result = transform0(o);

        if (result == null) {
            throw new IllegalStateException("Cannot transform " + o + " to " + type.getSimpleName());
        }

        return result;
    }
}
