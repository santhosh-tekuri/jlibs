package jlibs.core.graph.sequences;

/**
 * @author Santhosh Kumar T
 */
public class ArraySequence<E> extends AbstractSequence<E>{
    private E[] array;
    private int start;
    private int end;

    public ArraySequence(E... array){
        this(array, 0, array.length);
    }
    
    public ArraySequence(E[] array, int start, int end){
        if(start<0)
            throw new ArrayIndexOutOfBoundsException(start);
        if(end>array.length)
            throw new ArrayIndexOutOfBoundsException(end);
        if(start>end)
            throw new IllegalArgumentException(String.format("start(%d) must be less than or equal to end(%d)", start, end));
        
        this.array = array;
        this.start = start;
        this.end = end;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private int pos;

    @Override
    protected E findNext(){
        pos++;
        return pos<end ? array[pos] : null;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/

    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        pos = start - 1;
    }

    @Override
    public ArraySequence<E> copy(){
        return new ArraySequence<E>(array, start, end);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        return end-start;
    }
}
