package jlibs.graph.navigators;

import jlibs.graph.Navigator;
import jlibs.graph.Sequence;
import jlibs.graph.Filter;
import jlibs.graph.sequences.FilteredSequence;

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
    public Sequence<E> children(E elem){
        return new FilteredSequence<E>(navigator.children(elem), filter);
    }
}
