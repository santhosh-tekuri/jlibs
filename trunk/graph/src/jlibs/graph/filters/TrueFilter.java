package jlibs.graph.filters;

import jlibs.graph.Filter;

/**
 * @author Santhosh Kumar T
 */
public class TrueFilter<E> implements Filter<E>{
    public static final TrueFilter INSTANCE = new TrueFilter();

    private TrueFilter(){}
    
    @Override
    public boolean select(E elem){
        return true;
    }
}
