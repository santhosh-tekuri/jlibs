package jlibs.graph.sequences;

/**
 * @author Santhosh Kumar T
 */
public class TOCSequence extends AbstractSequence<String>{
    private int number;
    private int count;

    public TOCSequence(int number, int count){
        this.number = number;
        this.count = count;
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private int index;

    @Override
    protected String findNext(){
        index++;
        return index<=count ? number+"."+index : null;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/

    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        index = 0;
    }

    @Override
    public TOCSequence copy(){
        return new TOCSequence(number, count);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        return count;
    }
}
