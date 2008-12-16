package jlibs.core.graph.navigators;

import jlibs.core.graph.Sequence;
import jlibs.core.graph.sequences.EmptySequence;
import jlibs.core.graph.sequences.FilteredTreeSequence;
import jlibs.core.graph.Filter;
import jlibs.core.graph.Navigator;

/**
 * @author Santhosh Kumar T
 */
public class FilteredTreeNavigator<E> implements Navigator<E>{
    private Navigator<E> delegate;
    private Filter<E> filter;

    public FilteredTreeNavigator(Navigator<E> delegate){
        this(delegate, null);
    }

    public FilteredTreeNavigator(Navigator<E> delegate, Filter<E> filter){
        this.delegate = delegate;
        this.filter = filter;
    }

    @Override
    public Sequence<? extends E> children(E elem){
        Sequence<? extends E> seq = delegate.children(elem);
        if(seq==null)
            return EmptySequence.getInstance();
        if(filter!=null)
            seq = new FilteredTreeSequence<E>(seq, this, filter);
        return seq;
    }
}