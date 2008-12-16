package jlibs.xml.xsd.display;

import jlibs.graph.visitors.PathReflectionVisitor;
import jlibs.xml.sax.helpers.MyNamespaceSupport;
import jlibs.xml.xsd.XSUtil;
import org.apache.xerces.xs.*;

/**
 * @author Santhosh Kumar T
 */
public class XSDisplayValueVisitor extends PathReflectionVisitor<Object, String>{
    private MyNamespaceSupport nsSupport;

    public XSDisplayValueVisitor(MyNamespaceSupport nsSupport){
        this.nsSupport = nsSupport;
    }

    @Override
    protected String getDefault(Object elem){
        return null;
    }

    protected String process(XSNamespaceItem nsItem){
        String ns = nsItem.getSchemaNamespace();
        return nsSupport.findPrefix(ns!=null ? ns : "");
    }

    protected String process(XSElementDeclaration elem){
        XSTypeDefinition type = elem.getTypeDefinition();
        if(type instanceof XSComplexTypeDefinition){
            XSComplexTypeDefinition complexType = (XSComplexTypeDefinition)type;
            if(complexType.getContentType()==XSComplexTypeDefinition.CONTENTTYPE_SIMPLE)
                type = complexType.getBaseType();
        }
        if(type instanceof XSComplexTypeDefinition)
            return null;
        return XSUtil.getQName(type, nsSupport);
//        return type instanceof XSComplexTypeDefinition ? '{'+qname+'}' : qname;
    }

    protected String process(XSAttributeUse attrUse){
        return XSUtil.getQName(attrUse.getAttrDeclaration().getTypeDefinition(), nsSupport);
    }
}
