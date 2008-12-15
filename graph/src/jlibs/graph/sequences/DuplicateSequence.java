package jlibs.graph.sequences;

import org.jetbrains.annotations.NotNull;

/**
 * @author Santhosh Kumar T
 */
public class DuplicateSequence<E> extends AbstractSequence<E>{
    private E elem;
    private int count;

    public DuplicateSequence(@NotNull E elem){
        this(elem, 1);
    }

    public DuplicateSequence(@NotNull E elem, int count){
        //noinspection ConstantConditions
        if(elem==null)
            throw new IllegalArgumentException("elem can't be null");
        if(count<0)
            throw new IllegalArgumentException(String.format("can't duplicate %d times", count));
        
        this.elem = elem;
        this.count = count;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private int pos;

    @Override
    protected E findNext(){
        pos++;
        return pos<=count ? elem : null;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    
    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        pos = 0;
    }

    @Override
    public DuplicateSequence<E> copy(){
        return new DuplicateSequence<E>(elem, count);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        return count;
    }
}
