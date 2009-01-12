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

package jlibs.xml;

import jlibs.core.util.Enumerator;
import jlibs.xml.sax.helpers.MyNamespaceSupport;

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

/**
 * @author Santhosh Kumar T
 */
public class DefaultNamespaceContext implements NamespaceContext{
    private MyNamespaceSupport nsSupport;

    public DefaultNamespaceContext(){
        this(new MyNamespaceSupport());
    }
    
    public DefaultNamespaceContext(MyNamespaceSupport nsSupport){
        this.nsSupport = nsSupport;
    }

    @Override
    public String getNamespaceURI(String prefix){
        String uri = nsSupport.getURI(prefix);
        if(prefix.equals("") && uri==null)
            uri = "";
        return uri;
    }

    @Override
    public String getPrefix(String namespaceURI){
        return nsSupport.findPrefix(namespaceURI);
    }

    @Override
    public Iterator getPrefixes(String namespaceURI){
        return new Enumerator<String>(nsSupport.getPrefixes(namespaceURI));
    }

    public void declarePrefix(String prefix, String uri){
        nsSupport.declarePrefix(prefix, uri);
    }

    public String declarePrefix(String uri){
        return nsSupport.declarePrefix(uri);
    }
}
