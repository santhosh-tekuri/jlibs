package jlibs.xml.sax.sniff;

/**
 * @author Santhosh Kumar T
 */
public interface Match{
    public boolean matchesStartElement(String uri, String name, int pos);
}
