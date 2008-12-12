package jlibs.graph;

/**
 * @author Santhosh Kumar T
 */
public interface Sequence<E>{
    public E next();
    public E current();
    public void reset();
    public Sequence<E> copy();
}
