package jlibs.core.graph.sequences;

import jlibs.core.graph.Sequence;
import jlibs.core.graph.Filter;

/**
 * @author Santhosh Kumar T
 */
public class FilteredSequence<E> extends AbstractSequence<E>{
    private Sequence<? extends E> delegate;
    private Filter<E> filter;

    public FilteredSequence(Sequence<? extends E> delegate, Filter<E> filter){
        this.delegate = delegate;
        this.filter = filter;
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/
    
    @Override
    protected E findNext(){
        while(true){
            E elem = delegate.next();
            if(elem==null || filter.select(elem))
                return elem;
        }
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    
    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        delegate.reset();
    }

    @Override
    public Sequence<E> copy(){
        return new FilteredSequence<E>(delegate.copy(), filter);
    }
}
