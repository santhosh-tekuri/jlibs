package jlibs.graph.sequences;

import jlibs.graph.Sequence;

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

    @Override
    public E next(){
        return null;
    }

    @Override
    public E current(){
        return null;
    }

    @Override
    public void reset(){}

    @Override
    public EmptySequence<E> copy(){
        return this;
    }
}
