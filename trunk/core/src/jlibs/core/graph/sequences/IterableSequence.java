package jlibs.core.graph.sequences;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Santhosh Kumar T
 */
public class IterableSequence<E> extends AbstractSequence<E>{
    private Iterable<E> iterable;

    public IterableSequence(Iterable<E> iterable){
        this.iterable = iterable;
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
        iter = iterable.iterator();
    }

    @Override
    public IterableSequence<E> copy(){
        return new IterableSequence<E>(iterable);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        if(iterable instanceof Collection)
            return ((Collection<?>) iterable).size();
        else
            return super.length();
    }
}
