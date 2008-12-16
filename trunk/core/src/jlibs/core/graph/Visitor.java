package jlibs.core.graph;

/**
 * @author Santhosh Kumar T
 */
public interface Visitor<E, R>{
    public R visit(E elem); 
}
