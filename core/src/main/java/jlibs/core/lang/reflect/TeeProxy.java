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
