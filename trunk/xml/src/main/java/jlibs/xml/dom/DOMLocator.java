package jlibs.xml.dom;

import org.w3c.dom.Node;
import org.xml.sax.Locator;

/**
 * @author Santhosh Kumar T
 */
public class DOMLocator implements Locator{
    @Override
    public String getPublicId(){
        return null;
    }

    @Override
    public String getSystemId(){
        return null;
    }

    @Override
    public int getLineNumber(){
        return 0;
    }

    @Override
    public int getColumnNumber(){
        return 0;
    }

    Node node;
    public Node getNode(){
        return node;
    }
}
