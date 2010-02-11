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

package jlibs.core.lang.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Santhosh Kumar T
 */
public class TeeProxy implements InvocationHandler{
    private Object delegates[];

    public TeeProxy(Object... delegates){
        this.delegates = delegates;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable{
        Object result = method.invoke(delegates[0], args);
        for(int i=1; i<delegates.length; i++)
            method.invoke(delegates[i], args);
        return result;
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T create(Class<T> interfase, Object... delegates){
        TeeProxy handler = new TeeProxy(delegates);
        delegates[0].getClass().getInterfaces();
        return (T)Proxy.newProxyInstance(interfase.getClassLoader(), new Class[]{ interfase }, handler);
    }
}
