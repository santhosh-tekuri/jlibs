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

import jlibs.core.lang.model.ModelUtil;
import jlibs.xml.sax.binding.NamespaceContext;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.xml.XMLConstants;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Santhosh Kumar T
 */
public class Namespaces{
    private static final Map<TypeElement, Properties> registry = new HashMap<TypeElement, Properties>();

    @SuppressWarnings({"unchecked"})
    public static Properties get(Element element){
        TypeElement clazz = element instanceof TypeElement ? (TypeElement)element : ModelUtil.parent(element, TypeElement.class);
        
        Properties map = registry.get(clazz);
        if(map==null){
            registry.put(clazz, map=new Properties());
            map.put(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
            map.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
            map.put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);

            AnnotationMirror mirror = ModelUtil.getAnnotationMirror(clazz, NamespaceContext.class);
            if(mirror!=null){
                for(AnnotationValue entry: (Collection<AnnotationValue>)ModelUtil.getAnnotationValue(clazz, mirror, "value")){
                    AnnotationMirror entryMirror = (AnnotationMirror)entry.getValue();
                    map.put(ModelUtil.getAnnotationValue(clazz, entryMirror, "prefix"), ModelUtil.getAnnotationValue(clazz, entryMirror, "uri"));
                }
            }
        }
        return map;
    }
}
