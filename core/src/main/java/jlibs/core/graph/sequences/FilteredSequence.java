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

package jlibs.core.graph.sequences;

import jlibs.core.graph.Sequence;
import jlibs.core.graph.Filter;

/**
 * @author Santhosh Kumar T
 */
public class FilteredSequence<E> extends AbstractSequence<E>{
    private Sequence<? extends E> delegate;
    private Filter<E> filter;

    public FilteredSequence(Sequence<? extends E> delegate, Filter<E> filter){
        this.delegate = delegate;
        this.filter = filter;
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/
    
    @Override
    protected E findNext(){
        while(true){
            E elem = delegate.next();
            if(elem==null || filter.select(elem))
                return elem;
        }
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    
    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        delegate.reset();
    }

    @Override
    public Sequence<E> copy(){
        return new FilteredSequence<E>(delegate.copy(), filter);
    }
}
