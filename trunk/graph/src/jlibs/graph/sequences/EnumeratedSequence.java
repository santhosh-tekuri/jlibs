package jlibs.graph.sequences;

import jlibs.graph.Sequence;

import java.util.Enumeration;

/**
 * @author Santhosh Kumar T
 */
public class EnumeratedSequence<E> extends AbstractSequence<E>{
    private Enumeration<E> enumer;

    public EnumeratedSequence(Enumeration<E> enumer){
        this.enumer = enumer;
    }

    @Override
    protected E findNext(){
        return enumer.hasMoreElements() ? enumer.nextElement() : null;
    }

    @Override
    public void reset(){
        throw new UnsupportedOperationException();
    }

    @Override
    public EnumeratedSequence<E> copy(){
        throw new UnsupportedOperationException();
    }
}
