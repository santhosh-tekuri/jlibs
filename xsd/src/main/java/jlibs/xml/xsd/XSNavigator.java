/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.xsd;

import jlibs.core.graph.Navigator;
import jlibs.core.graph.Sequence;
import jlibs.core.graph.sequences.ConcatSequence;
import jlibs.core.graph.sequences.DuplicateSequence;
import jlibs.core.graph.sequences.EmptySequence;
import jlibs.core.graph.visitors.ReflectionVisitor;
import jlibs.xml.xsd.sequences.XSNamedMapSequence;
import jlibs.xml.xsd.sequences.XSNamespaceItemListSequence;
import jlibs.xml.xsd.sequences.XSObjectListSequence;
import org.apache.xerces.xs.*;

import static org.apache.xerces.xs.XSConstants.ELEMENT_DECLARATION;

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

    @SuppressWarnings("unchecked")
    protected Sequence<XSObject> process(XSComplexTypeDefinition complexType){
        Sequence<XSObject> sequence = new XSObjectListSequence<XSObject>(complexType.getAttributeUses());
        if(complexType.getAttributeWildcard()!=null)
            sequence = new ConcatSequence<XSObject>(sequence, new DuplicateSequence<XSObject>(complexType.getAttributeWildcard()));
        if(complexType.getParticle()!=null) // simple content
            sequence = new ConcatSequence<XSObject>(sequence, new DuplicateSequence<XSParticle>(complexType.getParticle()));
        return sequence;
    }

    protected Sequence<XSTerm> process(XSParticle particle){
        return new DuplicateSequence<XSTerm>(particle.getTerm());
    }

    protected Sequence<XSParticle> process(XSModelGroup modelGroup){
        return new XSObjectListSequence<XSParticle>(modelGroup.getParticles());
    }
}
