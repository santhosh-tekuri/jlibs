package jlibs.xml.sax;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author Santhosh Kumar T
 */
public class XMLWriter extends AbstractXMLReader{
    public XMLWriter(){
        supportedFeatures.add(SAXFeatures.NAMESPACES);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void parse(InputSource input) throws IOException, SAXException{
        if(input instanceof ObjectInputSource)
            ((ObjectInputSource)input).writeInto(this);
        else
            throw new IOException("can't parse "+input);
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException{
        throw new UnsupportedOperationException();
    }
}
