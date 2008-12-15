package jlibs.xml.xsd.display;

import jlibs.graph.visitors.PathReflectionVisitor;
import org.apache.xerces.xs.*;

import java.awt.*;

/**
 * @author Santhosh Kumar T
 */
public class XSFontStyleVisitor extends PathReflectionVisitor<Object, Integer>{
    XSPathDiplayFilter filter;

    public XSFontStyleVisitor(XSPathDiplayFilter filter){
        this.filter = filter;
    }

    private static final Integer STYLE_NONE = Font.PLAIN;
    private static final Integer STYLE_MANDATORY = Font.BOLD;
    private static final Integer STYLE_OPTIONAL = Font.PLAIN;

    @Override
    protected Integer getDefault(Object elem){
        return STYLE_NONE;
    }

    private Integer getStyle(){
        if(path.getParentPath()==null)
            return STYLE_NONE;
        else if(path.getParentPath().getElement() instanceof XSParticle){
            XSParticle particle = (XSParticle)path.getParentPath().getElement();
            if(particle.getMinOccurs()==0 && !filter.select(path.getParentPath()))
                return STYLE_OPTIONAL;
        }
        return STYLE_MANDATORY;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected Integer process(XSModelGroup modelGroup){
        return getStyle();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected Integer process(XSElementDeclaration elem){
        return getStyle();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected Integer process(XSWildcard wildcard){
        return getStyle();
    }

    protected Integer process(XSAttributeUse attrUse){
        if(!attrUse.getRequired())
            return STYLE_OPTIONAL;
        else
            return STYLE_MANDATORY;
    }
}