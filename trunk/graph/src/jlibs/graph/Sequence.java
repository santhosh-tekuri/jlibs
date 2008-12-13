package jlibs.graph;

/**
 * @author Santhosh Kumar T
 */
public interface Sequence<E>{
    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/
    public boolean hasNext();
    public E next();

    /*-------------------------------------------------[ Query ]---------------------------------------------------*/
    public E current();
    public int length();

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    public void reset();
    public Sequence<E> copy();
}
