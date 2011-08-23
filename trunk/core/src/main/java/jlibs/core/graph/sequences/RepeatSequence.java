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
