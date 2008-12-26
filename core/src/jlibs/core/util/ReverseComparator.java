package jlibs.core.util;

import java.util.Comparator;

/**
 * @author Santhosh Kumar T
 */
public class ReverseComparator<T> implements Comparator<T>{
    private Comparator<T> delegate;

    public ReverseComparator(){
        this(new DefaultComparator<T>());
    }
    
    public ReverseComparator(Comparator<T> delegate){
        this.delegate = delegate;
    }

    public int compare(T o1, T o2){
        return -delegate.compare(o1, o2);
    }
}
