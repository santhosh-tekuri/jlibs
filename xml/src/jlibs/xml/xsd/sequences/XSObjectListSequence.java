package jlibs.xml.xsd.sequences;

import jlibs.graph.sequences.AbstractSequence;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;

/**
 * @author Santhosh Kumar T
 */
public class XSObjectListSequence extends AbstractSequence<XSObject>{
    private XSObjectList list;

    public XSObjectListSequence(XSObjectList list){
        this.list = list;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private int i;

    @Override
    protected XSObject findNext(){
        return list.item(++i);
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/

    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        i = 0;
    }

    @Override
    public XSObjectListSequence copy(){
        return new XSObjectListSequence(list);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        return list.getLength();
    }
}

