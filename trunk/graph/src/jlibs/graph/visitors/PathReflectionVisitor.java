package jlibs.graph.visitors;

import jlibs.graph.Path;

/**
 * @author Santhosh Kumar T
 */
public abstract class PathReflectionVisitor<E, R> extends ReflectionVisitor<E, R>{
    protected Path path;

    @Override
    @SuppressWarnings({"unchecked"})
    public final R visit(E elem){
        if(elem instanceof Path){
            path = (Path)elem;
            elem = (E)path.getElement();
        }
        try{
            return _visit(elem);
        }finally{
            path = null;
        }
    }

    protected R _visit(E elem){
        return super.visit(elem);
    }
}
