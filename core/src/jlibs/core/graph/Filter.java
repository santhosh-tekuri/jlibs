package jlibs.core.graph;

/**
 * @author Santhosh Kumar T
 */
public interface Filter<E>{
    public boolean select(E elem);
}
