package jlibs.xml.xsd.sequences;

import jlibs.core.graph.sequences.AbstractSequence;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObject;

/**
 * @author Santhosh Kumar T
 */
public class XSNamedMapSequence<E extends XSObject> extends AbstractSequence<E>{
    private XSNamedMap map;

    public XSNamedMapSequence(XSNamedMap map){
        this.map = map;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private int i;

    @Override
    @SuppressWarnings({"unchecked"})
    protected E findNext(){
        return (E)map.item(++i);
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
    public XSNamedMapSequence<E> copy(){
        return new XSNamedMapSequence<E>(map);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        return map.getLength();
    }
}
