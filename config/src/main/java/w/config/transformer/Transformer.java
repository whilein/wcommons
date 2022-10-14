package w.config.transformer;

/**
 * @author whilein
 */
public interface Transformer<T> {

    T transform(Object o);

    T transformOrNull(Object o);

}
