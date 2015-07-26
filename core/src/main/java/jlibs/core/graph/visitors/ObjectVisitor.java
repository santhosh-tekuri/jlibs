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

package jlibs.core.graph.visitors;

import jlibs.core.graph.Visitor;

/**
 * @author Santhosh Kumar T
 */
public class ObjectVisitor<E, R> implements Visitor<E, R>{
    private ClassVisitor<Visitor<E, R>> visitor;

    public ObjectVisitor(){
        this(new ClassVisitor<Visitor<E, R>>());
    }

    public ObjectVisitor(ClassVisitor<Visitor<E, R>> visitor){
        this.visitor = visitor;
    }

    @Override
    public R visit(E elem){
        Visitor<E, R> result = visitor.visit(elem.getClass());
        if(result!=null)
            return result.visit(elem);
        else                                        
            return null;
    }

    public void map(Class<?> clazz, Visitor<E, R> returnValue){
        visitor.map(clazz, returnValue);
    }
}
