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

package jlibs.core.graph.sequences;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Santhosh Kumar T
 */
public class IterableSequence<E> extends AbstractSequence<E>{
    private Iterable<E> iterable;

    public IterableSequence(Iterable<E> iterable){
        this.iterable = iterable;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private Iterator<E> iter;

    @Override
    protected E findNext(){
        return iter.hasNext() ? iter.next() : null;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/

    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        iter = iterable.iterator();
    }

    @Override
    public IterableSequence<E> copy(){
        return new IterableSequence<E>(iterable);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        if(iterable instanceof Collection)
            return ((Collection<?>) iterable).size();
        else
            return super.length();
    }
}
