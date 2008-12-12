package jlibs.graph.sequences;

import jlibs.graph.Sequence;
import jlibs.graph.Filter;

/**
 * @author Santhosh Kumar T
 */
public class FilteredSequence<E> implements Sequence<E>{
    private Sequence<E> delegate;
    private Filter<E> filter;

    public FilteredSequence(Sequence<E> delegate, Filter<E> filter){
        this.delegate = delegate;
        this.filter = filter;
    }

    @Override
    public E next(){
        while(true){
            E elem = delegate.next();
            if(elem==null || filter.select(elem))
                return elem;
        }
    }

    @Override
    public E current(){
        return delegate.current();
    }

    @Override
    public void reset(){
        delegate.reset();
    }

    @Override
    public Sequence<E> copy(){
        return new FilteredSequence<E>(delegate.copy(), filter);
    }
}
