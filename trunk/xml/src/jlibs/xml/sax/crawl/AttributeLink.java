package jlibs.xml.sax.crawl;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;

/**
 * @author Santhosh Kumar T
 */
public class AttributeLink extends Link{
    protected QName attribute;

    public AttributeLink(QName attribute, String... extensions){
        super(extensions);
        this.attribute = attribute;
    }

    public AttributeLink(String attributeName, String... extensions){
        this(new QName(attributeName), extensions);
    }

    @Override
    public AttributeLink pushElement(String uri, String name){
        return (AttributeLink)super.pushElement(uri, name);
    }

    public String resolve(Attributes atts) throws MalformedURLException{
        return atts.getValue(attribute.getNamespaceURI(), attribute.getLocalPart());
    }

    public void repair(AttributesImpl atts, String newLocation){
        int index = atts.getIndex(attribute.getNamespaceURI(), attribute.getLocalPart());
        atts.setValue(index, newLocation);
    }
}
