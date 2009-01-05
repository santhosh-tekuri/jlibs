package jlibs.xml.xsl.crawl;

import jlibs.xml.sax.crawl.XMLCrawler;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

/**
 * @author Santhosh Kumar T
 */
public class XSLCrawler extends XMLCrawler{
    public XSLCrawler() throws ParserConfigurationException, SAXException{
        addLink(new XSLImport());
        addLink(new XSLInclude());
    }

    public File crawlInto(InputSource document, File dir) throws TransformerException, IOException{
        return crawlInto(document, dir, "xsl");
    }
}