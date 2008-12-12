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

    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        curVisitor = 0;
    }

    private int curVisitor;
    
    @Override
    protected E findNext(){
        while(curVisitor<sequences.length){
            E elem = sequences[curVisitor].next();
            if(elem==null)
                curVisitor++;
            else
                return elem;
        }
        return null;
    }

    @Override
    public ConcatSequence<E> copy(){
        return null;
    }
}
