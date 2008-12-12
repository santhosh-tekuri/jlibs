package jlibs.graph;

/**
 * @author Santhosh Kumar T
 */
public interface Navigator<E>{
    public Sequence<E> children(E elem);
}
