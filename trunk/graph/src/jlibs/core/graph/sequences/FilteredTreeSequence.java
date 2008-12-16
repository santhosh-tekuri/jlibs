package jlibs.core.graph.sequences;

import jlibs.core.graph.Navigator;
import jlibs.core.graph.Sequence;
import jlibs.core.graph.Filter;

import java.util.Stack;

/**
 * @author Santhosh Kumar T
 */
public class FilteredTreeSequence<E> extends AbstractSequence<E>{
    private Sequence<? extends E> seq;
    private Navigator<E> navigator;
    private Filter<E> filter;

    public FilteredTreeSequence(Sequence<? extends E> seq, Navigator<E> navigator, Filter<E> filter){
        this.seq = seq;
        this.navigator = navigator;
        this.filter = filter;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private Stack<Sequence<? extends E>> stack = new Stack<Sequence<? extends E>>();
    
    @Override
    protected E findNext(){
        while(!stack.isEmpty()){
            E elem = stack.peek().next();
            if(elem==null)
                stack.pop();
            else if(filter.select(elem))
                return elem;
            else
                stack.push(navigator.children(elem));
        }
        return null;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    
    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        stack.clear();
        seq.reset();
        stack.push(seq);
    }

    @Override
    public FilteredTreeSequence<E> copy(){
        return new FilteredTreeSequence<E>(seq.copy(), navigator, filter);
    }
}
