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

package jlibs.xml.xsd;

import jlibs.xml.sax.helpers.MyNamespaceSupport;
import jlibs.core.lang.StringUtil;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSAttributeUse;

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

    public static String getQName(XSObject obj, MyNamespaceSupport nsSupport){
        if(obj instanceof XSAttributeUse)
            obj = ((XSAttributeUse)obj).getAttrDeclaration();
        
        if(obj.getName()==null)
            return "";
        String ns = obj.getNamespace();
        String prefix = nsSupport.findPrefix(ns==null ? "" : ns);
        return StringUtil.isEmpty(prefix) ? obj.getName() : prefix+':'+obj.getName();
    }
}
