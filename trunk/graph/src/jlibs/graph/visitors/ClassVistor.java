package jlibs.graph.visitors;

import jlibs.graph.*;
import jlibs.graph.filters.NotFilter;
import jlibs.graph.sequences.CollectionSequence;
import jlibs.graph.sequences.ConcatSequence;
import jlibs.graph.sequences.FilteredSequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class ClassVistor<R> implements Visitor<Class<?>, R>{
    private Map<Class<?>, R> map = new HashMap<Class<?>,R>();
    private Sequence<Class<?>> topologicalSequence;
    
    public void map(Class<?> clazz, R returnValue){
        topologicalSequence = null;
        map.put(clazz, returnValue);
    }

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

    private void sort(){
        Filter<Class<?>> interfaceFilter = new Filter<Class<?>>(){
            @Override
            public boolean select(Class<?> clazz){
                return clazz.isInterface();
            }
        };
        List<Class<?>> classes = sort(new FilteredSequence<Class<?>>(new CollectionSequence<Class<?>>(map.keySet()), new NotFilter<Class<?>>(interfaceFilter)));
        List<Class<?>> interfaces = sort(new FilteredSequence<Class<?>>(new CollectionSequence<Class<?>>(map.keySet()), interfaceFilter));
        topologicalSequence = new ConcatSequence<Class<?>>(new CollectionSequence<Class<?>>(classes), new CollectionSequence<Class<?>>(interfaces));
    }
    
    @Override
    public R visit(Class<?> elem){
        if(topologicalSequence==null)
            sort();

        topologicalSequence.reset();
        for(Class<?> clazz; (clazz=topologicalSequence.next())!=null;){
            if(clazz.isAssignableFrom(elem))
                return map.get(clazz);
        }
        return null;
    }
}
