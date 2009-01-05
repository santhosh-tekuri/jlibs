package jlibs.xml.wsdl.crawl;

import jlibs.xml.sax.crawl.XMLCrawler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

/**
 * @author Santhosh Kumar T
 */
public class WSDLCrawler extends XMLCrawler{
    public WSDLCrawler() throws ParserConfigurationException, SAXException{
        addLink(new WSDLImport());
        addLink(new WSDLInclude());
        addLink(new XSImport());
        addLink(new XSInclude());
        addLink(new jlibs.xml.xsd.crawl.XSImport());
        addLink(new jlibs.xml.xsd.crawl.XSInclude());
    }

    public File crawlInto(InputSource document, File dir) throws TransformerException, IOException{
        return crawlInto(document, dir, "wsdl");
    }
}
