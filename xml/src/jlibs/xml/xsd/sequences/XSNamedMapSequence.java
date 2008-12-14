package jlibs.xml.xsd.sequences;

import jlibs.graph.sequences.AbstractSequence;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObject;

/**
 * @author Santhosh Kumar T
 */
public class XSNamedMapSequence extends AbstractSequence<XSObject>{
    private XSNamedMap map;

    public XSNamedMapSequence(XSNamedMap map){
        this.map = map;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private int i;

    @Override
    protected XSObject findNext(){
        return map.item(++i);
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
    public XSNamedMapSequence copy(){
        return new XSNamedMapSequence(map);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        return map.getLength();
    }
}
