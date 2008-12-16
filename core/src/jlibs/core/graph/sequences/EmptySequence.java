package jlibs.core.graph.sequences;

import jlibs.core.graph.Sequence;

/**
 * @author Santhosh Kumar T
 */
public final class EmptySequence<E> implements Sequence<E>{
    private static final EmptySequence INSTANCE = new EmptySequence();

    @SuppressWarnings("unchecked")
    public static <T> EmptySequence<T> getInstance(){
        return (EmptySequence<T>)INSTANCE;
    }

    private EmptySequence(){}

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    @Override
    public boolean hasNext(){
        return false;
    }

    @Override
    public E next(){
        return null;
    }

    @Override
    public E next(int count){
        return null;
    }

    /*-------------------------------------------------[ Query ]---------------------------------------------------*/

    @Override
    public int index(){
        return 0;
    }

    @Override
    public E current(){
        return null;
    }

    @Override
    public int length(){
        return 0;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    
    @Override
    public void reset(){}

    @Override
    public EmptySequence<E> copy(){
        return this;
    }
}
