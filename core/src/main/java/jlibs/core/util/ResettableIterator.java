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

package jlibs.core.util;

import java.util.Iterator;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class ResettableIterator<E> implements Iterator<E>{
    private List<E> list;
    private int cursor;

    public ResettableIterator(List<E> list){
        this.list = list;
    }
    
    @Override
    public boolean hasNext(){
        return cursor<list.size();
    }

    @Override
    public E next(){
        return list.get(cursor++);
    }

    @Override
    public void remove(){
        list.remove(cursor-1);
    }

    public ResettableIterator<E> reset(){
        cursor = 0;
        return this;
    }
    
    public ResettableIterator<E> reset(List<E> list){
        this.list = list;
        cursor = 0;
        return this;
    }
}
