package jlibs.core.graph.navigators;

import jlibs.core.graph.Navigator;
import jlibs.core.graph.Sequence;
import jlibs.core.graph.Filter;
import jlibs.core.graph.sequences.FilteredSequence;

/**
 * @author Santhosh Kumar T
 */
public class FilteredNavigator<E> implements Navigator<E>{
    private Navigator<E> navigator;
    private Filter<E> filter;

    public FilteredNavigator(Navigator<E> navigator, Filter<E> filter){
        this.navigator = navigator;
        this.filter = filter;
    }

    @Override
    public Sequence<? extends E> children(E elem){
        return new FilteredSequence<E>(navigator.children(elem), filter);
    }
}
