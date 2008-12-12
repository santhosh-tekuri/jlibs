package jlibs.graph.visitors;

import jlibs.graph.Visitor;

/**
 * @author Santhosh Kumar T
 */
public class StaticVisitor<E, R> implements Visitor<E, R>{
    private R result;

    public StaticVisitor(R result){
        this.result = result;
    }

    @Override
    public R visit(E elem){
        return result;
    }
}
