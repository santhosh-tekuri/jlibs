package jlibs.xml.xsd.sequences;

import jlibs.graph.sequences.AbstractSequence;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSNamespaceItemList;

/**
 * @author Santhosh Kumar T
 */
public class XSNamespaceItemListSequence extends AbstractSequence<XSNamespaceItem>{
    private XSNamespaceItemList list;

    public XSNamespaceItemListSequence(XSNamespaceItemList list){
        this.list = list;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private int i;

    @Override
    protected XSNamespaceItem findNext(){
        return list.item(++i);
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/

    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        i = -1;
    }

    @Override
    public XSNamespaceItemListSequence copy(){
        return new XSNamespaceItemListSequence(list);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        return list.getLength();
    }
}