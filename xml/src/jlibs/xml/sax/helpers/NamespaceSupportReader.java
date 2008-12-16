package jlibs.xml.sax.helpers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @author Santhosh Kumar T
 */
public class NamespaceSupportReader extends XMLFilterImpl{
    private MyNamespaceSupport nsSupport;

    public MyNamespaceSupport getNamespaceSupport(){
        return nsSupport;
    }

    private boolean needNewContext;
    @Override
    public void startDocument() throws SAXException{
        nsSupport = new MyNamespaceSupport();
        needNewContext = true;
        super.startDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException{
        if(needNewContext){
            nsSupport.pushContext();
            needNewContext = false;
        }
        nsSupport.declarePrefix(prefix, uri);
        
        super.startPrefixMapping(prefix, uri);
    }

    public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts) throws SAXException{
        if(needNewContext)
            nsSupport.pushContext();
        needNewContext = true;
        super.startElement(namespaceURI, localName, qualifiedName, atts);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException{
        nsSupport.popContext();
        super.endElement(uri, localName, qName);
    }
}
