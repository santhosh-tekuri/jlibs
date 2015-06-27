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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class SequenceUtil{
    public static <E> int indexOf(Sequence<? extends E> seq, E elem){
        if(elem==null)
            return -1;

        for(E item; (item=seq.next())!=null;){
            if(elem.equals(item))
                break;
        }
        
        return seq.index();
    }

    public static <E, C extends Collection<E>> C addAll(C collection, Sequence<? extends E> seq){
        for(E elem; (elem=seq.next())!=null;)
            collection.add(elem);
        return collection;
    }

    @SuppressWarnings("unchecked")
    public static <C, E> C[] toArray(Class<?> clazz, Sequence<E> seq){
        List<E> list = addAll(new LinkedList<E>(), seq);
        return list.toArray((C[])Array.newInstance(clazz, list.size()));
    }
}
