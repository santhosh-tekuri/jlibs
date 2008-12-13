package jlibs.graph.sequences;

/**
 * @author Santhosh Kumar T
 */
public class DuplicateSequence<E> extends AbstractSequence<E>{
    private E elem;
    private int count;

    public DuplicateSequence(E elem){
        this(elem, 1);
    }

    public DuplicateSequence(E elem, int count){
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
