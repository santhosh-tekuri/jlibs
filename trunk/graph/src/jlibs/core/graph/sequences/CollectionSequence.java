package jlibs.core.graph.sequences;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Santhosh Kumar T
 */
public class CollectionSequence<E> extends AbstractSequence<E>{
    private Collection<E> collection;

    public CollectionSequence(Collection<E> collection){
        this.collection = collection;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private Iterator<E> iter;

    @Override
    protected E findNext(){
        return iter.hasNext() ? iter.next() : null;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/

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

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        return collection.size();
    }
}
