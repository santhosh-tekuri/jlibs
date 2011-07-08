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

import jlibs.xml.sax.helpers.MyNamespaceSupport;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSObject;

import javax.xml.namespace.QName;

/**
 * @author Santhosh Kumar T
 */
public class XSUtil{
    public static MyNamespaceSupport createNamespaceSupport(XSModel model){
        MyNamespaceSupport nsSupport = new MyNamespaceSupport();
        StringList list = model.getNamespaces();
        for(int i=0; i<list.getLength(); i++){
            if(list.item(i)!=null) // default namespace is null
                nsSupport.declarePrefix(list.item(i));
        }
        return nsSupport;
    }

    public static QName getQName(XSObject obj, MyNamespaceSupport nsSupport){
        if(obj instanceof XSAttributeUse)
            obj = ((XSAttributeUse)obj).getAttrDeclaration();
        
        if(obj.getName()==null)
            return new QName("");
        String ns = obj.getNamespace()==null ? "" : obj.getNamespace();
        String prefix = nsSupport.findPrefix(ns);
        return new QName(ns, obj.getName(), prefix);
    }
}
