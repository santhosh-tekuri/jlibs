package jlibs.xml.sax.helpers;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;

import java.io.CharArrayWriter;

/**
 * @author Santhosh Kumar T
 */
public class SAXHandler extends DefaultHandler{
    protected MyNamespaceSupport nsSupport;

    protected CharArrayWriter contents;

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException{
        contents.write(ch, start, length);
    }
}
