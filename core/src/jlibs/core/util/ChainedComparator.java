package jlibs.core.util;

import java.util.Comparator;

/**
 * @author Santhosh Kumar T
 */
public class ChainedComparator<T> implements Comparator<T>{
    private Comparator<T> delegates[];

    public ChainedComparator(Comparator<T>... delegates){
        this.delegates = delegates;
    }

    @Override
    public int compare(T o1, T o2){
        for(Comparator<T> delegate: delegates){
            int result = delegate.compare(o1, o2);
            if(result!=0)
                return result;
        }
        return 0;
    }
}
