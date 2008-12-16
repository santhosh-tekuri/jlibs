package jlibs.xml.sax;

import static jlibs.xml.sax.SAXFeatures.NAMESPACES;
import static jlibs.xml.sax.SAXFeatures.NAMESPACE_PREFIXES;
import static jlibs.xml.sax.SAXProperties.*;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.ext.DeclHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Santhosh Kumar T
 */
public abstract class AbstractXMLReader extends SAXDelegate2 implements XMLReader{

    /*-------------------------------------------------[ Features ]---------------------------------------------------*/
    
    protected final Set<String> supportedFeatures = new HashSet<String>();
    private final Set<String> features = new HashSet<String>();

    protected boolean nsFeature;
    protected boolean nsPrefixesFeature;

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException{
        if(supportedFeatures.contains(name)){
            if(value)
                features.add(name);
            else
                features.remove(name);
            
            if(NAMESPACES.equals(name))
                nsFeature = value;
            else if(NAMESPACE_PREFIXES.equals(name))
                nsPrefixesFeature = value;
        }else
            throw new SAXNotRecognizedException(name);
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException{
        if(supportedFeatures.contains(name))
            return features.contains(name);
        else
            throw new SAXNotRecognizedException(name);
    }

    /*-------------------------------------------------[ Properties ]---------------------------------------------------*/

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException{
        if(LEXICAL_HANDLER.equals(name) || LEXICAL_HANDLER_ALT.equals(name)){
            if(value==null || value instanceof LexicalHandler)
                setLexicalHandler((LexicalHandler)value);
            else
                throw new SAXNotSupportedException("value must implement "+LexicalHandler.class);
        }else if(DECL_HANDLER.equals(name) || DECL_HANDLER_ALT.equals(name)){
            if(value==null || value instanceof DeclHandler)
                setDeclHandler((DeclHandler)value);
            else
                throw new SAXNotSupportedException("value must implement "+DeclHandler.class);
        }else
            throw new SAXNotRecognizedException(name);
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException{
        if(LEXICAL_HANDLER.equals(name) || LEXICAL_HANDLER_ALT.equals(name))
            return getLexicalHandler();
        else if(DECL_HANDLER.equals(name) || DECL_HANDLER_ALT.equals(name))
            return getDeclHandler();
        else
            throw new SAXNotRecognizedException(name);
    }
}
