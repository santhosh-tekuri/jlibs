package jlibs.xml.xsl.crawl;

import jlibs.xml.sax.crawl.AttributeLink;
import jlibs.xml.Namespaces;

/**
 * @author Santhosh Kumar T
 */
public class XSLInclude extends AttributeLink{
    public XSLInclude(){
        super("href", "xsl");
        pushElement(Namespaces.URI_XSL, "stylesheetschema");
        pushElement(Namespaces.URI_XSL, "include");
    }
}