package jlibs.xml.wsdl.crawl;

import jlibs.xml.sax.crawl.XMLCrawler;
import jlibs.core.io.FileUtil;
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

    public static void main(String[] args) throws Exception{
        String dir = "xml/xsds/crawl";
        String wsdl = "https://fps.amazonaws.com/doc/2007-01-08/AmazonFPS.wsdl";
        InputSource document = new InputSource(wsdl);
        FileUtil.delete(new File(dir));
        new WSDLCrawler().crawlInto(document, new File(dir));
    }
}
