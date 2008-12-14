package jlibs.graph.navigators;

import jlibs.graph.Navigator;
import jlibs.graph.Sequence;
import jlibs.graph.Visitor;
import jlibs.graph.Filter;
import jlibs.graph.sequences.EmptySequence;
import jlibs.graph.sequences.FilteredTreeSequence;

/**
 * @author Santhosh Kumar T
 */
public class DefaultNavigator<E> implements Navigator<E>{
    private Visitor<E, Sequence<? extends E>> visitor;
    private Filter<E> filter;

    public DefaultNavigator(Visitor<E, Sequence<? extends E>> visitor){
        this(visitor, null);
    }

    public DefaultNavigator(Visitor<E, Sequence<? extends E>> visitor, Filter<E> filter){
        this.visitor = visitor;
        this.filter = filter;
    }

    @Override
    public Sequence<? extends E> children(E elem){
        Sequence<? extends E> seq = visitor.visit(elem);
        if(seq!=null)
            return filter==null ? seq : new FilteredTreeSequence<E>(seq, this, filter);
        else
            return EmptySequence.getInstance();
    }
}