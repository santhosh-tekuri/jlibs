package jlibs.graph.filters;

import jlibs.graph.Filter;

/**
 * @author Santhosh Kumar T
 */
public class NotFilter<E> implements Filter<E>{
    private Filter<E> delegate;

    public NotFilter(Filter<E> delegate){
        this.delegate = delegate;
    }

    @Override
    public boolean select(E elem){
        return !delegate.select(elem);
    }
}
