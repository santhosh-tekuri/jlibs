package jlibs.xml.xsd;

import jlibs.core.graph.Sequence;
import jlibs.core.graph.Navigator;
import jlibs.core.graph.sequences.EmptySequence;
import jlibs.core.graph.sequences.DuplicateSequence;
import jlibs.core.graph.sequences.ConcatSequence;
import jlibs.core.graph.visitors.ReflectionVisitor;
import jlibs.xml.xsd.sequences.XSNamespaceItemListSequence;
import jlibs.xml.xsd.sequences.XSNamedMapSequence;
import jlibs.xml.xsd.sequences.XSObjectListSequence;
import static org.apache.xerces.xs.XSConstants.ELEMENT_DECLARATION;
import org.apache.xerces.xs.*;

/**
 * @author Santhosh Kumar T
 */
public class XSNavigator extends ReflectionVisitor<Object, Sequence> implements Navigator{
    @Override
    public Sequence children(Object elem){
        return visit(elem);
    }

    @Override
    protected Sequence getDefault(Object elem){
        return EmptySequence.getInstance();
    }

    protected Sequence<XSNamespaceItem> process(XSModel model){
        return new XSNamespaceItemListSequence(model.getNamespaceItems());
    }

    protected Sequence<XSElementDeclaration> process(XSNamespaceItem nsItem){
        return new XSNamedMapSequence<XSElementDeclaration>(nsItem.getComponents(ELEMENT_DECLARATION));
    }

    protected Sequence<XSTypeDefinition> process(XSElementDeclaration elem){
        return new DuplicateSequence<XSTypeDefinition>(elem.getTypeDefinition());
    }

    protected Sequence<XSObject> process(XSComplexTypeDefinition complexType){
        XSObjectListSequence<XSObject> attributes = new XSObjectListSequence<XSObject>(complexType.getAttributeUses());
        if(complexType.getParticle()!=null) // simple content
            return new ConcatSequence<XSObject>(attributes, new DuplicateSequence<XSParticle>(complexType.getParticle()));
        return attributes;
    }

    protected Sequence<XSTerm> process(XSParticle particle){
        return new DuplicateSequence<XSTerm>(particle.getTerm());
    }

    protected Sequence<XSParticle> process(XSModelGroup modelGroup){
        return new XSObjectListSequence<XSParticle>(modelGroup.getParticles());
    }
}
