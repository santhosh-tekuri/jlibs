package jlibs.xml.sax;

import jlibs.core.lang.ImpossibleException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * @author Santhosh Kumar T
 */
public class SAXUtil{
    public static SAXParser newSAXParser(boolean namespaces, boolean nsPrefixes) throws ParserConfigurationException, SAXException{
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(namespaces);
        try{
            if(nsPrefixes)
            factory.setFeature(SAXFeatures.NAMESPACE_PREFIXES, true);
        }catch(Exception ex){
            throw new ImpossibleException(factory+" is not SAX-Compliant as it doesn't support namespace-prefixes feature");
        }
        return factory.newSAXParser();
    }
}
