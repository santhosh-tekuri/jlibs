package jlibs.graph.filters;

import jlibs.graph.Filter;

/**
 * @author Santhosh Kumar T
 */
public class InstanceFilter<E> implements Filter<E>{
    private Class<?> clazz;

    public InstanceFilter(Class<?> clazz){
        this.clazz = clazz;
    }

    @Override
    public boolean select(E elem){
        return clazz.isInstance(elem);
    }
}
