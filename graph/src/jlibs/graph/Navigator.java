package jlibs.graph;

/**
 * @author Santhosh Kumar T
 */
public interface Navigator<E>{
    public Sequence<? extends E> children(E elem);
}
