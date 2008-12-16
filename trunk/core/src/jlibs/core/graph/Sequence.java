package jlibs.core.graph;

/**
 * @author Santhosh Kumar T
 */
public interface Sequence<E>{
    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/
    public boolean hasNext();
    public E next();
    public E next(int count);

    /*-------------------------------------------------[ Query ]---------------------------------------------------*/
    public int index();
    public E current();
    public int length();

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    public void reset();
    public Sequence<E> copy();
}
