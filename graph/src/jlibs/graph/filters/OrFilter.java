package jlibs.graph.filters;

import jlibs.graph.Filter;

/**
 * @author Santhosh Kumar T
 */
public class OrFilter<E> implements Filter<E>{
    private Filter<E> filters[];

    public OrFilter(Filter<E>... filters){
        this.filters = filters;
    }

    @Override
    public boolean select(E elem){
        for(Filter<E> filter: filters){
            if(filter.select(elem))
                return true;
        }
        return false;    
    }
}