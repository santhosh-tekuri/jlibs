package jlibs.core.graph.visitors;

import jlibs.core.graph.Sequence;
import jlibs.core.graph.WalkerUtil;
import jlibs.core.graph.sequences.FilteredSequence;
import jlibs.core.graph.sequences.CollectionSequence;
import jlibs.core.graph.Filter;
import jlibs.core.graph.Navigator;

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
