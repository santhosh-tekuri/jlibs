package jlibs.xml.sax;

import jlibs.core.lang.Ansi;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Santhosh Kumar T
 */
public class AnsiHandler extends DefaultHandler{
    private static final Ansi TOKENS = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.CYAN, null);
    private static final Ansi ELEMENT  = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.YELLOW, null);
    private static final Ansi ATTR_NAME  = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.GREEN, null);
    private static final Ansi ATTR_VALUE  = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.MAGENTA, null);

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
        TOKENS.out("<");
        ELEMENT.out(qName);

        for(int i=0; i<attributes.getLength(); i++){
            System.out.print(" ");
            ATTR_NAME.out(attributes.getQName(i));
            TOKENS.out("=\"");
            ATTR_VALUE.out(attributes.getValue(i));
            TOKENS.out("\"");
        }

        TOKENS.out(">");
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        TOKENS.out("</");
        ELEMENT.out(qName);
        TOKENS.out(">");
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException{
        System.out.print(new String(ch, start, length));
    }

    @Override
    public void endDocument() throws SAXException{
        System.out.println();
    }
}
