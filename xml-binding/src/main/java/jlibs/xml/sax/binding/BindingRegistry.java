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

package jlibs.xml.sax.binding;

import jlibs.xml.sax.binding.impl.Registry;
import jlibs.xml.sax.binding.impl.Relation;

import javax.xml.namespace.QName;

/**
 * @author Santhosh Kumar T
 */
@SuppressWarnings({"unchecked"})
public class BindingRegistry{
    Registry registry = new Registry();

    public BindingRegistry(Class... classes){
        for(Class clazz: classes)
            register(clazz);
    }

    public BindingRegistry(QName qname, Class clazz){
        register(qname, clazz);
    }

    public void register(Class clazz){
        register(null, clazz);
    }

    public void register(QName qname, Class clazz){
        try{
            String implQName = "${package}.${class}Impl".replace("${package}", clazz.getPackage()!=null?clazz.getPackage().getName():"")
                    .replace("${class}", clazz.getSimpleName());
            if(implQName.startsWith(".")) // default package
                implQName = implQName.substring(1);
            Class implClass = clazz.getClassLoader().loadClass(implQName);
            if(qname==null)
                qname = (QName)implClass.getDeclaredField("ELEMENT").get(null);
            if(qname==null)
                throw new IllegalArgumentException("can't find qname for: "+implClass);

            jlibs.xml.sax.binding.impl.Binding binding = (jlibs.xml.sax.binding.impl.Binding)implClass.getDeclaredField("INSTANCE").get(null);
            registry.register(qname, 0, binding, 0, Relation.DO_NOTHING);
        }catch(ClassNotFoundException ex){
            throw new RuntimeException(ex);
        } catch(NoSuchFieldException ex){
            throw new RuntimeException(ex);
        } catch(IllegalAccessException ex){
            throw new RuntimeException(ex);
        }
    }
}
