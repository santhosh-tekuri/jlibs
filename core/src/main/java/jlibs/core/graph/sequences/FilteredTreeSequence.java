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

import jlibs.core.graph.Navigator;
import jlibs.core.graph.Sequence;
import jlibs.core.graph.Filter;

import java.util.Stack;

/**
 * @author Santhosh Kumar T
 */
public class FilteredTreeSequence<E> extends AbstractSequence<E>{
    private Sequence<? extends E> seq;
    private Navigator<E> navigator;
    private Filter<E> filter;

    public FilteredTreeSequence(Sequence<? extends E> seq, Navigator<E> navigator, Filter<E> filter){
        this.seq = seq;
        this.navigator = navigator;
        this.filter = filter;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private Stack<Sequence<? extends E>> stack = new Stack<Sequence<? extends E>>();
    
    @Override
    protected E findNext(){
        while(!stack.isEmpty()){
            E elem = stack.peek().next();
            if(elem==null)
                stack.pop();
            else if(filter.select(elem))
                return elem;
            else
                stack.push(navigator.children(elem));
        }
        return null;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    
    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        stack.clear();
        seq.reset();
        stack.push(seq);
    }

    @Override
    public FilteredTreeSequence<E> copy(){
        return new FilteredTreeSequence<E>(seq.copy(), navigator, filter);
    }
}
