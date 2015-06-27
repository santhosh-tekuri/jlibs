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

/**
 * A Sequence that can repeat given sequence specified number of
 * times
 *
 * @author Santhosh Kumar T
 */
public class RepeatSequence<E> extends AbstractSequence<E>{
    private Sequence<E> sequence;
    private int count;

    public RepeatSequence(Sequence<E> sequence, int count){
        if(count<0)
            throw new IllegalArgumentException(String.format("can't repeat %d times", count));
        this.sequence = sequence;
        this.count = count;
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private int pos;

    @Override
    protected E findNext(){
        if(pos==count)
            return null;
        while(true){
            E next = sequence.next();
            if(next==null){
                pos++;
                if(pos==count)
                    return null;
                sequence = sequence.copy();
            }else
                return next;
        }
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/

    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        sequence = sequence.copy();
        pos = 0;
    }

    @Override
    public RepeatSequence<E> copy(){
        return new RepeatSequence<E>(sequence.copy(), count);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        return sequence.length() * count;
    }
}
