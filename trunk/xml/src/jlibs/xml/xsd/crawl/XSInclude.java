package jlibs.xml.xsd.crawl;

import jlibs.xml.sax.crawl.AttributeLink;
import jlibs.xml.Namespaces;

/**
 * @author Santhosh Kumar T
 */
public class XSInclude extends AttributeLink{
    public XSInclude(){
        super("schemaLocation", "xsd");
        pushElement(Namespaces.URI_XSD, "schema");
        pushElement(Namespaces.URI_XSD, "include");
    }
}