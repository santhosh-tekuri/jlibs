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

import jlibs.core.graph.visitors.PathReflectionVisitor;
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

    private int getStyle(){
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
    protected int process(XSNamespaceItem nsItem){
        return STYLE_MANDATORY;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected int process(XSModelGroup modelGroup){
        return getStyle();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected int process(XSElementDeclaration elem){
        return getStyle();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected int process(XSWildcard wildcard){
        return getStyle();
    }

    protected int process(XSAttributeUse attrUse){
        if(!attrUse.getRequired())
            return STYLE_OPTIONAL;
        else
            return STYLE_MANDATORY;
    }
}