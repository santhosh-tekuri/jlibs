package jlibs.xml.wsdl.crawl;

import jlibs.xml.sax.crawl.AttributeLink;
import jlibs.xml.Namespaces;

/**
 * @author Santhosh Kumar T
 */
public class WSDLImport extends AttributeLink{
    public WSDLImport(){
        super("location", "wsdl", "xsd");
        pushElement(Namespaces.URI_WSDL, "definitions");
        pushElement(Namespaces.URI_WSDL, "import");
    }
}
