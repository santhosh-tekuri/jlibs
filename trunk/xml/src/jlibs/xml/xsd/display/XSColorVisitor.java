package jlibs.xml.xsd.display;

import jlibs.graph.visitors.PathReflectionVisitor;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSWildcard;

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

    private static final Color COLOR_OTHER = Color.lightGray;
    private static final Color COLOR_ELEMENT = new Color(0, 0, 128);
    private static final Color COLOR_ATTRIBUTE = new Color(0, 128, 0);
    private static final Color COLOR_OPTIONAL = new Color(0, 128, 0);

    @SuppressWarnings({"UnusedDeclaration"})
    protected Color process(XSModelGroup modelGroup){
        if(!filter.select(path.getParentPath()))
            return COLOR_OPTIONAL;
        else
            return null;
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
