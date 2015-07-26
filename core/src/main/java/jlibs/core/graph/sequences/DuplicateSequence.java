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

/**
 * @author Santhosh Kumar T
 */
public class DuplicateSequence<E> extends AbstractSequence<E>{
    private E elem;
    private int count;

    public DuplicateSequence(E elem){
        this(elem, 1);
    }

    public DuplicateSequence(E elem, int count){
        //noinspection ConstantConditions
        if(elem==null)
            throw new IllegalArgumentException("elem can't be null");
        if(count<0)
            throw new IllegalArgumentException(String.format("can't duplicate %d times", count));
        
        this.elem = elem;
        this.count = count;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private int pos;

    @Override
    protected E findNext(){
        pos++;
        return pos<=count ? elem : null;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    
    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        pos = 0;
    }

    @Override
    public DuplicateSequence<E> copy(){
        return new DuplicateSequence<E>(elem, count);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        return count;
    }
}
