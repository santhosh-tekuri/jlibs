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

package jlibs.xml.sax.binding.impl;

import jlibs.xml.QNameFake;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class Registry{
    public static final String STAR = "*";
    public static final QName ANY = new QName(STAR, STAR);
    
    public Map<QName, BindingRelation> registry;

    public Registry register(QName qname, int bindingState, Binding binding, int relationState, Relation relation){
        if(registry ==null)
            registry = new HashMap<QName, BindingRelation>();
        BindingRelation bindingRelation = new BindingRelation(qname, bindingState, binding, relationState, relation);
        registry.put(qname, bindingRelation);
        return bindingRelation.binding.registry;
    }

    public void register(QName qname, int bindingState, Binding binding){
        register(qname, bindingState, binding, 0, TempRelation.PUT);
    }

    public void register(QName qname){
        register(qname, 0, TextBinding.INSTANCE);
    }

    public void register(QName qname, int relationState, Relation relation){
        register(qname, 0, TextBinding.INSTANCE, relationState, relation);
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public BindingRelation get(QNameFake qname){
        if(registry==null)
            return null;
        else{
            BindingRelation br = registry.get(qname);
            String namespaceURI = qname.namespaceURI;
            String localPart = qname.localPart;
            if(br==null)
                br = registry.get(qname.set(STAR, localPart));
            if(br==null)
                br = registry.get(qname.set(namespaceURI, STAR));
            if(br==null)
                br = registry.get(ANY);
            return br;
        }
    }
}
