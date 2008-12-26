package jlibs.core.graph.visitors;

import jlibs.core.graph.Sequence;
import jlibs.core.graph.Visitor;
import jlibs.core.graph.sequences.IterableSequence;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class ClassVisitor<R> implements Visitor<Class<?>, R>{
    private Map<Class<?>, R> map = new HashMap<Class<?>,R>();
    private Sequence<Class<?>> topologicalSequence;
    
    public void map(Class<?> clazz, R returnValue){
        topologicalSequence = null;
        map.put(clazz, returnValue);
    }

    @Override
    public R visit(Class<?> elem){
        if(topologicalSequence==null)
            topologicalSequence = new IterableSequence<Class<?>>(ClassSorter.sort(map.keySet()));

        topologicalSequence.reset();
        for(Class<?> clazz; (clazz=topologicalSequence.next())!=null;){
            if(clazz.isAssignableFrom(elem))
                return map.get(clazz);
        }
        return null;
    }
}
