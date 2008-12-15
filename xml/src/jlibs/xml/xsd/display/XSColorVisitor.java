package jlibs.xml.xsd.display;

import jlibs.graph.visitors.PathReflectionVisitor;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSWildcard;
import org.apache.xerces.xs.XSNamespaceItem;

import java.awt.*;

/**
 * @author Santhosh Kumar T
 */
public class XSColorVisitor extends PathReflectionVisitor<Object, Color>{
    XSPathDiplayFilter filter;

    public XSColorVisitor(XSPathDiplayFilter filter){
        this.filter = filter;
    }

    @Override
    protected Color getDefault(Object elem){
        return COLOR_OTHER;
    }

    private static final Color COLOR_OTHER = Color.GRAY;
    private static final Color COLOR_ELEMENT = new Color(0, 0, 128);
    private static final Color COLOR_ATTRIBUTE = new Color(0, 128, 0);
    private static final Color COLOR_NSITEM = new Color(102, 0, 0);

    @SuppressWarnings({"UnusedDeclaration"})
    protected Color process(XSNamespaceItem nsItem){
        return COLOR_NSITEM;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected Color process(XSElementDeclaration elem){
        return COLOR_ELEMENT;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected Color process(XSWildcard wildcard){
        return COLOR_ELEMENT;
    }
    
    @SuppressWarnings({"UnusedDeclaration"})
    protected Color process(XSAttributeUse attrUse){
        return COLOR_ATTRIBUTE;
    }
}
