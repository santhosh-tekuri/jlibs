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

import jlibs.core.graph.Sequence;
import jlibs.core.graph.Visitor;
import jlibs.core.graph.sequences.IterableSequence;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class ClassVisitor<R> implements Visitor<Class<?>, R>{
    private Map<Class<?>, R> map = new HashMap<Class<?>,R>();
    private Sequence<Class<?>> topologicalSequence;
    
    public void map(Class<?> clazz, R returnValue){
        topologicalSequence = null;
        map.put(clazz, returnValue);
    }

    @Override
    public R visit(Class<?> elem){
        if(topologicalSequence==null)
            topologicalSequence = new IterableSequence<Class<?>>(ClassSorter.sort(map.keySet()));

        topologicalSequence.reset();
        for(Class<?> clazz; (clazz=topologicalSequence.next())!=null;){
            if(clazz.isAssignableFrom(elem))
                return map.get(clazz);
        }
        return null;
    }
}
