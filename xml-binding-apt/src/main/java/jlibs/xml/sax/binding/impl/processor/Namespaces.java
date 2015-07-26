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
