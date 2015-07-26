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

package jlibs.xml.xsd.display;

import jlibs.core.graph.Filter;
import jlibs.core.graph.visitors.ReflectionVisitor;
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
