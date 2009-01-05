package jlibs.xml.xsd.crawl;

import jlibs.core.io.FileUtil;
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
public class XSCrawler extends XMLCrawler{
    public XSCrawler() throws ParserConfigurationException, SAXException{
        addLink(new XSImport());
        addLink(new XSInclude());
    }

    public File crawlInto(InputSource document, File dir) throws TransformerException, IOException{
        return crawlInto(document, dir, "xsd");
    }

    public static void main(String[] args) throws Exception{
        String dir = "xml/xsds/crawl";
        String xsd = "xml/xsds/a.xsd";
        InputSource document = new InputSource(xsd);
        FileUtil.delete(new File(dir));
        new XSCrawler().crawlInto(document, new File(dir));
    }
}
