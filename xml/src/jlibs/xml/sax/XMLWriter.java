package jlibs.xml.sax;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
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
    public void parse(InputSource input) throws SAXException, IOException{
        try{
            if(input instanceof ObjectInputSource)
                ((ObjectInputSource)input).writeInto(this);
            else
                throw new IOException("can't parse "+input);
        }catch(ParserConfigurationException ex){
            throw new SAXException(ex);
        }
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException{
        throw new UnsupportedOperationException();
    }
}
