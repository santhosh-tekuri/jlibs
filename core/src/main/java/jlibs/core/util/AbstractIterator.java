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
import java.util.NoSuchElementException;

/**
 * Abstract Implementation of Iterator<T>. Subclass need to override
 * only <code>computeNext()</code> method.
 *
 * @author Santhosh Kumar T
 */
public abstract class AbstractIterator<T> implements Iterator<T>{
    protected static final Object NO_MORE_ELEMENTS = new Object();

    private Object next;

    protected abstract Object computeNext();

    protected AbstractIterator<T> reset(){
        next = null;
        return this;
    }

    @Override
    public final boolean hasNext(){
        if(next==NO_MORE_ELEMENTS)
            return false;
        if(next==null)
            next = computeNext();
        return next!=NO_MORE_ELEMENTS;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public final T next(){
        if(hasNext()){
            T current = (T)next;
            next = null;
            return current;
        }else
            throw new NoSuchElementException();
    }

    @Override
    public void remove(){
        throw new UnsupportedOperationException();
    }
}
