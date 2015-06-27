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

package jlibs.xml.xsd.display;

import jlibs.core.graph.Filter;
import jlibs.core.graph.Navigator;
import jlibs.core.graph.Path;
import jlibs.core.graph.visitors.PathReflectionVisitor;
import org.apache.xerces.xs.XSParticle;

/**
 * @author Santhosh Kumar T
 */
public class XSPathDiplayFilter extends PathReflectionVisitor<Object, Boolean> implements Filter<Path>{
    private Navigator navigator;

    public XSPathDiplayFilter(Navigator navigator){
        this.navigator = navigator;
    }

    @Override
    public boolean select(Path path){
        return visit(path);
    }

    protected Boolean getDefault(Object elem){
        return true;
    }

    @SuppressWarnings({"unchecked"})
    protected boolean process(XSParticle particle){
        return navigator.children(particle).length()!=1;
    }
}
