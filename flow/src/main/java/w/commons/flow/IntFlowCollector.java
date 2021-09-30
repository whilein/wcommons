package w.commons.flow;

/**
 * @author whilein
 */
public interface IntFlowCollector<A, R> {

    A init();

    R empty();

    void accumulate(A collection, int value) throws Exception;

    R finish(A collection);

}
