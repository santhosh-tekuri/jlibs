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

package jlibs.xml.sax.binding.impl.processor;

import jlibs.core.annotation.processing.AnnotationError;
import jlibs.xml.sax.binding.impl.Registry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
class Binding{
    int id;
    ExecutableElement startMethod;
    ExecutableElement textMethod;
    ExecutableElement finishMethod;
    TypeElement element;

    Map<QName, BindingRelation> registry = new LinkedHashMap<QName, BindingRelation>();

    Binding getBinding(ExecutableElement method, AnnotationMirror mirror, String xpath){
        if(xpath.length()==0)
            return this;
        else
            return getBindingRelation(method, mirror, xpath).binding;
    }

    Relation getRelation(ExecutableElement method, AnnotationMirror mirror, String xpath){
        return getBindingRelation(method, mirror, xpath).relation;
    }

    BindingRelation getBindingRelation(ExecutableElement method, AnnotationMirror mirror, String xpath){
        Binding binding = this;
        BindingRelation bindingRelation = null;

        StringTokenizer stok = new StringTokenizer(xpath, "/");
        Map<QName, BindingRelation> registry = binding.registry;
        while(stok.hasMoreTokens()){
            String token = stok.nextToken();
            QName qname = toQName(method, mirror, token);
            bindingRelation = registry.get(qname);
            if(bindingRelation==null){
                registry.put(qname, new BindingRelation());
                bindingRelation = registry.get(qname);
            }
            registry = bindingRelation.binding.registry;
        }

        return bindingRelation;
    }

    public void initID(int id){
        initID(id, idMap, new Stack<QName>());
    }

    public Map<Integer, List<QName>> idMap = new HashMap<Integer, List<QName>>();
    private int initID(int id, Map<Integer, List<QName>> idMap, Stack<QName> path){
        this.id = id;
        idMap.put(id, new ArrayList<QName>(path));
        for(Map.Entry<QName, BindingRelation> entry: registry.entrySet()){
            path.push(entry.getKey());
            id = entry.getValue().binding.initID(++id, idMap, path);
            path.pop();
        }
        return id;
    }

    static QName toQName(Element pos1, AnnotationMirror pos2, String token){
        Properties nsContext = Namespaces.get(pos1);

        String prefix, localName;
        int colon = token.indexOf(':');
        if(colon==-1){
            prefix = "";
            localName = token;
        }else{
            prefix = token.substring(0, colon);
            localName = token.substring(colon+1);
        }
        String uri = prefix.equals(Registry.STAR) ? prefix : nsContext.getProperty(prefix);
        if(uri==null)
            throw new AnnotationError(pos1, pos2, "no namespace mapping found for prefix "+prefix);
        return new QName(uri, localName);
    }

    static String toJava(QName qname){
        if(qname==null)
            return null;
        else if(qname.getNamespaceURI().length()==0)
            return "new QName(\""+qname.getLocalPart()+"\")";
        else
            return "new QName(\""+qname.getNamespaceURI()+"\", \""+qname.getLocalPart()+"\")";
    }    
}

class Relation{
    ExecutableElement startedMethod;
    ExecutableElement finishedMethod;
}

class BindingRelation{
    Binding binding;
    Relation relation;

    BindingRelation(){
        binding = new Binding();
        relation = new Relation();
    }
}

