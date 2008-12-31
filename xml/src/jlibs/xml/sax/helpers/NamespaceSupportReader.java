package jlibs.xml.sax.helpers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;

import jlibs.xml.sax.SAXUtil;

import javax.xml.parsers.ParserConfigurationException;

/**
 * @author Santhosh Kumar T
 */
public class NamespaceSupportReader extends XMLFilterImpl{
    private MyNamespaceSupport nsSupport = new MyNamespaceSupport();

    public NamespaceSupportReader(boolean nsPrefixes) throws ParserConfigurationException, SAXException{
        this(SAXUtil.newSAXParser(true, nsPrefixes).getXMLReader());
    }
    
    public NamespaceSupportReader(XMLReader parent){
        super(parent);
    }

    public MyNamespaceSupport getNamespaceSupport(){
        return nsSupport;
    }

    private boolean needNewContext;
    @Override
    public void startDocument() throws SAXException{
        nsSupport.reset();
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

    public void setDefaultHandler(DefaultHandler handler){
        if(handler instanceof SAXHandler)
            ((SAXHandler)handler).nsSupport = nsSupport;
        
        setContentHandler(handler);
        setEntityResolver(handler);
        setErrorHandler(handler);
        setDTDHandler(handler);
    }

    /*-------------------------------------------------[ Parsing ]---------------------------------------------------*/
    
    public void parse(InputSource is, DefaultHandler handler) throws IOException, SAXException{
        setDefaultHandler(handler);
        parse(is);
    }

    public void parse(String systemId, DefaultHandler handler) throws IOException, SAXException{
        setDefaultHandler(handler);
        parse(systemId);
    }
}
