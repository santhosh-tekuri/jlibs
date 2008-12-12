package jlibs.graph.sequences;

import jlibs.graph.Sequence;

import java.util.Iterator;
import java.util.Collection;

/**
 * @author Santhosh Kumar T
 */
public class CollectionSequence<E> extends AbstractSequence<E>{
    private Collection<E> collection;
    private Iterator<E> iter;

    public CollectionSequence(Collection<E> collection){
        this.collection = collection;
        _reset();
    }

    @Override
    protected E findNext(){
        return iter.hasNext() ? iter.next() : null;
    }

    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        iter = collection.iterator();
    }

    @Override
    public CollectionSequence<E> copy(){
        return new CollectionSequence<E>(collection);
    }
}
