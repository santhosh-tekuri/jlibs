package jlibs.graph.visitors;

import jlibs.graph.Sequence;
import jlibs.graph.WalkerUtil;
import jlibs.graph.Navigator;
import jlibs.graph.Filter;
import jlibs.graph.filters.NotFilter;
import jlibs.graph.sequences.FilteredSequence;
import jlibs.graph.sequences.CollectionSequence;
import jlibs.graph.sequences.ConcatSequence;

import java.util.List;
import java.util.Collection;

/**
 * @author Santhosh Kumar T
 */
public class ClassSorter{
    private static List<Class<?>> sort(final Sequence<Class<?>> classes){
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

    public static Sequence<Class<?>> sort(Collection<Class<?>> collection){
        Filter<Class<?>> interfaceFilter = new Filter<Class<?>>(){
            @Override
            public boolean select(Class<?> clazz){
                return clazz.isInterface();
            }
        };
        List<Class<?>> classes = sort(new FilteredSequence<Class<?>>(new CollectionSequence<Class<?>>(collection), new NotFilter<Class<?>>(interfaceFilter)));
        List<Class<?>> interfaces = sort(new FilteredSequence<Class<?>>(new CollectionSequence<Class<?>>(collection), interfaceFilter));
        return new ConcatSequence<Class<?>>(new CollectionSequence<Class<?>>(classes), new CollectionSequence<Class<?>>(interfaces));
    }
}
