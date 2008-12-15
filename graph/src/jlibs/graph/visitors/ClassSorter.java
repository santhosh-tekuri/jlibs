package jlibs.graph.visitors;

import jlibs.graph.Filter;
import jlibs.graph.Navigator;
import jlibs.graph.Sequence;
import jlibs.graph.WalkerUtil;
import jlibs.graph.sequences.FilteredSequence;
import jlibs.graph.sequences.CollectionSequence;

import java.util.List;
import java.util.Collection;

/**
 * @author Santhosh Kumar T
 */
public class ClassSorter{
    public static List<Class<?>> sort(final Sequence<Class<?>> classes){
        return WalkerUtil.topologicalSort(classes, new Navigator<Class<?>>(){
            @Override
            public Sequence<Class<?>> children(final Class<?> parent){
                return new FilteredSequence<Class<?>>(classes.copy(), new Filter<Class<?>>(){
                    @Override
                    public boolean select(Class<?> child){
                        return child.isAssignableFrom(parent);
                    }
                });
            }
        });
    }

    public static List<Class<?>> sort(Collection<Class<?>> classes){
        return sort(new CollectionSequence<Class<?>>(classes));
    }
}
