package jlibs.xml.xsd.display;

import jlibs.graph.Filter;
import jlibs.graph.visitors.ReflectionVisitor;
import jlibs.xml.Namespaces;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSNamespaceItem;

/**
 * @author Santhosh Kumar T
 */
public class XSDisplayFilter extends ReflectionVisitor<Object, Boolean> implements Filter{
    @Override
    public boolean select(Object elem){
        return visit(elem);
    }

    @Override
    protected Boolean getDefault(Object elem){
        return true;
    }

    protected boolean process(XSNamespaceItem nsItem){
        return !Namespaces.URI_XSD.equals(nsItem.getSchemaNamespace());
    }

    protected boolean process(XSParticle particle){
        return !(!particle.getMaxOccursUnbounded() && particle.getMinOccurs() == 1 && particle.getMaxOccurs() == 1);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected boolean process(XSTypeDefinition type){
        return false;
    }

    protected boolean process(XSModelGroup modelGroup){
        return modelGroup.getCompositor()!=XSModelGroup.COMPOSITOR_SEQUENCE;
    }
}
