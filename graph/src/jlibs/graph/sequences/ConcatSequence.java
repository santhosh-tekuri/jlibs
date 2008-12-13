package jlibs.graph.sequences;

import jlibs.graph.Sequence;

/**
 * @author Santhosh Kumar T
 */
public class ConcatSequence<E> extends AbstractSequence<E>{
    private Sequence<E> sequences[];

    public ConcatSequence(Sequence<E>... sequences){
        this.sequences = sequences;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/
    
    private int curSeq;

    @Override
    protected E findNext(){
        while(curSeq<sequences.length){
            E elem = sequences[curSeq].next();
            if(elem==null)
                curSeq++;
            else
                return elem;
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
        curSeq = 0;
    }

    @Override
    public ConcatSequence<E> copy(){
        return null;
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        int len = 0;
        for(Sequence<E> seq: sequences)
            len += seq.length();
        return len;
    }
}
