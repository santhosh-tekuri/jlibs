package jlibs.graph.navigators;

import jlibs.graph.Navigator;
import jlibs.graph.Sequence;
import jlibs.graph.Filter;
import jlibs.graph.sequences.EmptySequence;
import jlibs.graph.sequences.FilteredTreeSequence;

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