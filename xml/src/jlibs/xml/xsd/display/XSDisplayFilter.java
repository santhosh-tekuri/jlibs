/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
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
