package jlibs.xml.sax;

import org.xml.sax.SAXException;

import javax.xml.namespace.QName;

/**
 * @author Santhosh Kumar T
 */
public interface SAXProducer{
    public QName getRootElement();
    public void writeAttributes(ObjectInputSource xml) throws SAXException;
    public void writeContent(ObjectInputSource xml) throws SAXException;
}
