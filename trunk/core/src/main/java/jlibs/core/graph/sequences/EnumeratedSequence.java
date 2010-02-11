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

import java.util.Enumeration;

/**
 * @author Santhosh Kumar T
 */
public class EnumeratedSequence<E> extends AbstractSequence<E>{
    private Enumeration<E> enumer;

    public EnumeratedSequence(Enumeration<E> enumer){
        this.enumer = enumer;
    }

    /*-------------------------------------------------[ Advaning ]---------------------------------------------------*/
    
    @Override
    protected E findNext(){
        return enumer.hasMoreElements() ? enumer.nextElement() : null;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    
    @Override
    public void reset(){
        throw new UnsupportedOperationException();
    }

    @Override
    public EnumeratedSequence<E> copy(){
        throw new UnsupportedOperationException();
    }
}
