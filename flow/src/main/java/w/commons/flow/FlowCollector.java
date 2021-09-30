package w.commons.flow;

/**
 * @author whilein
 */
public interface FlowCollector<T, A, R> {

    A init();

    R empty();

    void accumulate(A container, T value) throws Exception;

    R finish(A container);

}
