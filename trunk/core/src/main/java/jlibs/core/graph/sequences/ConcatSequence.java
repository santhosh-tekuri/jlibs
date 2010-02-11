/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
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
