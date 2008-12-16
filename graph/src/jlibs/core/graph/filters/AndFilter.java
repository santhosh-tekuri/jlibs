package jlibs.core.graph.filters;

import jlibs.core.graph.Filter;

/**
 * @author Santhosh Kumar T
 */
public class AndFilter<E> implements Filter<E>{
    private Filter<E> filters[];

    public AndFilter(Filter<E>... filters){
        this.filters = filters;
    }

    @Override
    public boolean select(E elem){
        for(Filter<E> filter: filters){
            if(!filter.select(elem))
                return false;
        }
        return true;    
    }
}
