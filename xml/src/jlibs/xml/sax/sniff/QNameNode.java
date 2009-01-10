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

package jlibs.xml.sax.sniff;

import jlibs.core.lang.Util;

import javax.xml.namespace.QName;

/**
 * @author Santhosh Kumar T
 */
public class QNameNode extends Node{
    private QName qname;
    private String namespace;

    protected QNameNode(Node parent, QName qname, String namespace){
        super(parent);
        this.qname = qname;
        if(qname==null)
            this.namespace = namespace;
    }

    /*-------------------------------------------------[ Matching ]---------------------------------------------------*/

    @SuppressWarnings({"SimplifiableIfStatement"})
    protected boolean matchesQName(String uri, String name){
        if(qname==null){
            if(namespace!=null)
                return namespace.equals(uri);
            else
                return true;
        }else
            return qname.getNamespaceURI().equals(uri) && qname.getLocalPart().equals(name);
    }

    @Override
    protected String getStep(){
        if(qname==null){
            if(namespace!=null)
                return root.nsContext.getPrefix(namespace)+":*";
            else
                return "*";
        }else{
            if(qname.getPrefix().length()>0)
                return qname.getPrefix()+':'+qname.getLocalPart();
            else
                return qname.getLocalPart();
        }
    }

    @Override
    protected boolean canMerge(Node node){
        if(node.getClass()==getClass()){
            QNameNode that = (QNameNode)node;
            return Util.equals(this.qname, that.qname) && Util.equals(this.namespace, that.namespace);
        }
        return false;
    }
}
