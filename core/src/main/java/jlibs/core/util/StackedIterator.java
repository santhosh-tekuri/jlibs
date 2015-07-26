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

package jlibs.core.util;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Santhosh Kumar Tekuri
 */
public class StackedIterator<T> implements Iterator<T>{
    private LinkedList<Iterator<T>> list = new LinkedList<Iterator<T>>();

    public StackedIterator(Iterator<T> delegate){
        list.addLast(delegate);
    }

    private Iterator<T> current(){
        while(!list.isEmpty()){
            Iterator<T> current = list.getLast();
            if(current.hasNext())
                return current;
            list.removeLast();
        }
        return EmptyIterator.instance();
    }

    @Override
    public boolean hasNext(){
        return current().hasNext();
    }

    private Iterator<T> current;
    @Override
    public T next(){
        current = current();
        return current.next();
    }

    @Override
    public void remove(){
        current.remove();
    }

    public void push(Iterator<T> iter){
        list.addLast(iter);
    }

    public Iterator<T> pop(){
        return list.isEmpty() ? null : list.removeLast();
    }
}
