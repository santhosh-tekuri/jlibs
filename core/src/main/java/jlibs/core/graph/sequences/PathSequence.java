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

import jlibs.core.graph.Path;
import jlibs.core.graph.Sequence;

/**
 * @author Santhosh Kumar T
 */
public class PathSequence<E> extends AbstractSequence<Path>{
    private Path path;
    private Sequence<? extends E> delegate;

    public PathSequence(Path path, Sequence<? extends E> delegate){
        this.path = path;
        this.delegate = delegate;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    @Override
    protected Path findNext(){
        E elem = delegate.next();
        return elem==null ? null : path.append(elem, index()+1);
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
    public Sequence<Path> copy(){
        return new PathSequence<E>(path, delegate.copy());
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        return delegate.length();
    }
}
