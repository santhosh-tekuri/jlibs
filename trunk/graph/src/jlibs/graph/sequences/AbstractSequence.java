package jlibs.graph.sequences;

import jlibs.graph.Sequence;

/**
 * @author Santhosh Kumar T
 */
public abstract class AbstractSequence<E> implements Sequence<E>{
    protected E current;
    protected boolean finished;

    protected AbstractSequence(){
        _reset();
    }

    private void _reset(){
        current = null;
        finished = false;
    }

    public void reset(){
        _reset();
    }

    protected abstract E findNext();

    @Override
    public E next(){
        if(!finished){
            current = findNext();
            if(current==null)
                finished = true;
        }
        return current;
    }

    @Override
    public E current(){
        return current;
    }
}
