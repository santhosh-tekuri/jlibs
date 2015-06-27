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

package jlibs.core.graph.visitors;

import jlibs.core.graph.Sequence;
import jlibs.core.graph.Visitor;
import jlibs.core.graph.sequences.IterableSequence;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public abstract class ReflectionVisitor<E, R> implements Visitor<E, R>{
    private Map<Class<?>, Method> methodMap = new HashMap<Class<?>, Method>();
    private Sequence<Class<?>> seq;

    private void sort(){
        Class clazz = getClass();
        do{
            for(Method method: clazz.getDeclaredMethods()){
                if(method.getName().equals("process")){
                    Class<?>[] params = method.getParameterTypes();
                    if(params.length==1 && !methodMap.containsKey(params[0])){
                        methodMap.put(params[0], method);
                        method.setAccessible(true);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }while(clazz!=null);

        seq = new IterableSequence<Class<?>>(ClassSorter.sort(methodMap.keySet()));
    }

    public void generateCode(){
        if(seq==null)
            sort();
        else
            seq.reset();
        
        for(Class<?> clazz; (clazz=seq.next())!=null;){
            System.out.print("        ");
            if(seq.index()!=0)
                System.out.print("else ");
            System.out.format("if(elem instanceof %s)%n", clazz.getSimpleName());
            System.out.format("            return process((%s)elem);%n", clazz.getSimpleName());
        }
        System.out.println("        else");
        System.out.println("           return getDefault(elem);");
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public R visit(E elem){
        if(elem!=null){
            if(seq==null)
                sort();
            else
                seq.reset();

            for(Class<?> clazz; (clazz=seq.next())!=null;){
                if(clazz.isAssignableFrom(elem.getClass())){
                    try{
                        return (R)methodMap.get(clazz).invoke(this, elem);
                    }catch(Exception ex){
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        return getDefault(elem);
    }

    protected abstract R getDefault(Object elem);
}
