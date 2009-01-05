package jlibs.xml.wsdl.crawl;

import jlibs.xml.sax.crawl.AttributeLink;
import jlibs.xml.Namespaces;

/**
 * @author Santhosh Kumar T
 */
public class WSDLInclude extends AttributeLink{
    public WSDLInclude(){
        super("location", "wsdl", "xsd");
        pushElement(Namespaces.URI_WSDL, "definitions");
        pushElement(Namespaces.URI_WSDL, "include");
    }
}