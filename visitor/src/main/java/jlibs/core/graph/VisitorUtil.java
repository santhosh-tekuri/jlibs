/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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

package jlibs.core.graph;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Santhosh Kumar T
 */
public class VisitorUtil{
    static final String FORMAT = "${package}.${class}Impl";

    private static Class findGeneratedClass(Class clazz) throws ClassNotFoundException{
        String qname = FORMAT.replace("${package}", clazz.getPackage()!=null?clazz.getPackage().getName():"")
                .replace("${class}", clazz.getSimpleName());
        if(qname.startsWith(".")) // default package
            qname = qname.substring(1);
        return clazz.getClassLoader().loadClass(qname);
    }

    @SuppressWarnings({"unchecked"})
    public static <E, R> Visitor<E, R> createVisitor(Object delegate){
        try{
            Class implClass = findGeneratedClass(delegate.getClass());
            return (Visitor<E, R>)implClass.getConstructor(delegate.getClass()).newInstance(delegate);
        }catch(ClassNotFoundException ex){
            throw new RuntimeException(ex);
        } catch(InvocationTargetException ex){
            throw new RuntimeException(ex);
        } catch(NoSuchMethodException ex){
            throw new RuntimeException(ex);
        } catch(InstantiationException ex){
            throw new RuntimeException(ex);
        } catch(IllegalAccessException ex){
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <E, R> Visitor<E, R> createVisitor(Class clazz){
        try{
            return (Visitor<E, R>)findGeneratedClass(clazz).newInstance();
        }catch(ClassNotFoundException ex){
            throw new RuntimeException(ex);
        } catch(InstantiationException ex){
            throw new RuntimeException(ex);
        } catch(IllegalAccessException ex){
            throw new RuntimeException(ex);
        }
    }
}
