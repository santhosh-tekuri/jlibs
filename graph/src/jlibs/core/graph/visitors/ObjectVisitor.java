package jlibs.core.graph.visitors;

import jlibs.core.graph.Visitor;

/**
 * @author Santhosh Kumar T
 */
public class ObjectVisitor<E, R> implements Visitor<E, R>{
    private ClassVisitor<Visitor<E, R>> visitor;

    public ObjectVisitor(){
        this(new ClassVisitor<Visitor<E, R>>());
    }

    public ObjectVisitor(ClassVisitor<Visitor<E, R>> visitor){
        this.visitor = visitor;
    }

    @Override
    public R visit(E elem){
        Visitor<E, R> result = visitor.visit(elem.getClass());
        if(result!=null)
            return result.visit(elem);
        else                                        
            return null;
    }

    public void map(Class<?> clazz, Visitor<E, R> returnValue){
        visitor.map(clazz, returnValue);
    }
}
