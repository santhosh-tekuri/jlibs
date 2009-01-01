package jlibs.xml.sax;

import org.xml.sax.SAXException;

/**
 * @author Santhosh Kumar T
 */
public class SAXInputSource<E extends SAXProducer> extends ObjectInputSource<E>{
    public SAXInputSource(E obj){
        super(obj);
    }

    @Override
    protected void write(E obj) throws SAXException{
        add(obj);
    }
}
