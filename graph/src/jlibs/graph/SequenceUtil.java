package jlibs.graph;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class SequenceUtil{
    public static <E, C extends Collection<E>> C addAll(C collection, Sequence<? extends E> seq){
        for(E elem; (elem=seq.next())!=null;)
            collection.add(elem);
        return collection;
    }

    @SuppressWarnings("unchecked")
    public static <E extends C, C> C[] toArray(Class<C> clazz, Sequence<E> seq){
        List<E> list = addAll(new LinkedList<E>(), seq);
        return list.toArray((C[])Array.newInstance(clazz, list.size()));
    }
}
