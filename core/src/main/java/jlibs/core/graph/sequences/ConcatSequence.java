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

import jlibs.core.graph.Sequence;

/**
 * @author Santhosh Kumar T
 */
public class ConcatSequence<E> extends AbstractSequence<E>{
    private Sequence<? extends E> sequences[];

    public ConcatSequence(Sequence<? extends E>... sequences){
        this.sequences = sequences;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/
    
    private int curSeq;

    @Override
    protected E findNext(){
        while(curSeq<sequences.length){
            E elem = sequences[curSeq].next();
            if(elem==null)
                curSeq++;
            else
                return elem;
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
        curSeq = 0;
        for(Sequence<? extends E> seq: sequences)
            seq.reset();
    }

    @Override
    public ConcatSequence<E> copy(){
        @SuppressWarnings({"unchecked"})
        Sequence<? extends E> sequences[] = new Sequence[this.sequences.length];
        for(int i=0; i<sequences.length; i++)
            sequences[i] = this.sequences[i].copy();
        return new ConcatSequence<E>(sequences);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        int len = 0;
        for(Sequence<? extends E> seq: sequences)
            len += seq.length();
        return len;
    }
}
