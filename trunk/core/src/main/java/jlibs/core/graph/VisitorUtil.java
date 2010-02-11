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

package jlibs.core.graph;

import jlibs.core.lang.model.ModelUtil;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Santhosh Kumar T
 */
public class VisitorUtil{
    @SuppressWarnings({"unchecked"})
    public static <E, R> Visitor<E, R> createVisitor(Object delegate){
        try{
            Class implClass = ModelUtil.findClass(delegate.getClass(), VisitorAnnotationProcessor.FORMAT);
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
            return (Visitor<E, R>)ModelUtil.findClass(clazz, VisitorAnnotationProcessor.FORMAT).newInstance();
        }catch(ClassNotFoundException ex){
            throw new RuntimeException(ex);
        } catch(InstantiationException ex){
            throw new RuntimeException(ex);
        } catch(IllegalAccessException ex){
            throw new RuntimeException(ex);
        }
    }
}
