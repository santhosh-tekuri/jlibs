package jlibs.graph.visitors;

import jlibs.graph.Sequence;
import jlibs.graph.Visitor;

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
            topologicalSequence = ClassSorter.sort(map.keySet());

        topologicalSequence.reset();
        for(Class<?> clazz; (clazz=topologicalSequence.next())!=null;){
            if(clazz.isAssignableFrom(elem))
                return map.get(clazz);
        }
        return null;
    }
}
