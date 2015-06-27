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

/**
 * @author Santhosh Kumar T
 */
public class PredicateConvertor<E> implements Convertor<E, String>{
    protected Navigator2<E> navigator;
    protected Convertor<E, String> delegate;

    public PredicateConvertor(Navigator2<E> navigator, Convertor<E, String> delegate){
        this.navigator = navigator;
        this.delegate = delegate;
    }

    @Override
    public String convert(E source){
        String name = delegate.convert(source);
        E parent = navigator.parent(source);
        if(parent==null)
            return name;
        
        Sequence<? extends E> children = navigator.children(parent);
        int predicate = 1;
        while(children.hasNext()){
            E child = children.next();
            if(child.equals(source))
                break;
            if(name.equals(delegate.convert(child)))
                predicate++;
        }
        return name += "["+predicate+']';
    }
}
