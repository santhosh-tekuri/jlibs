/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
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
