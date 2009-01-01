package jlibs.xml.sax;

import org.xml.sax.SAXException;

/**
 * @author Santhosh Kumar T
 */
public interface SAXProducer{
    public void writeInto(ObjectInputSource xml) throws SAXException;
}
