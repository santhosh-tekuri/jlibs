package jlibs.core.graph.sequences;

import java.util.Enumeration;

/**
 * @author Santhosh Kumar T
 */
public class EnumeratedSequence<E> extends AbstractSequence<E>{
    private Enumeration<E> enumer;

    public EnumeratedSequence(Enumeration<E> enumer){
        this.enumer = enumer;
    }

    /*-------------------------------------------------[ Advaning ]---------------------------------------------------*/
    
    @Override
    protected E findNext(){
        return enumer.hasMoreElements() ? enumer.nextElement() : null;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    
    @Override
    public void reset(){
        throw new UnsupportedOperationException();
    }

    @Override
    public EnumeratedSequence<E> copy(){
        throw new UnsupportedOperationException();
    }
}
