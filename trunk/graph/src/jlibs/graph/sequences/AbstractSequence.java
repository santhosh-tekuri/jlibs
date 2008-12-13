package jlibs.graph.sequences;

import jlibs.graph.Sequence;

/**
 * @author Santhosh Kumar T
 */
public abstract class AbstractSequence<E> implements Sequence<E>{
    protected AbstractSequence(){
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/
    
    private boolean advanced = false;
    private E next;

    @Override
    public boolean hasNext(){
        if(finished)
            return false;
        else if(!advanced){
            next = findNext();
            advanced = true;
        }
        return next!=null;
    }

    protected boolean finished;

    @Override
    public final E next(){
        if(finished)
            return null;
        else if(advanced){
            advanced = false;
            current = next;
            next = null;
        }else
            current = findNext();

        if(current==null)
            finished = true;

        return current;
    }

    protected abstract E findNext();

    /*-------------------------------------------------[ Query ]---------------------------------------------------*/
    
    protected E current;

    @Override
    public final E current(){
        return current;
    }

    @Override
    public int length(){
        Sequence<E> seq = copy();

        int len = 0;
        while(seq.next()!=null)
            len++;

        return len;
    }

    /*-------------------------------------------------[ Reset ]---------------------------------------------------*/

    private void _reset(){
        current = next = null;
        advanced = finished = false;
    }

    public void reset(){
        _reset();
    }    
}
