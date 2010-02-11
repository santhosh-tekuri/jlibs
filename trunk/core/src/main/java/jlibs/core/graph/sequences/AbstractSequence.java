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
public abstract class AbstractSequence<E> implements Sequence<E>{
    protected AbstractSequence(){
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/
    
    private boolean advanced = false;
    private E next;

    @Override
    public boolean hasNext(){
        if(current.finished())
            return false;
        else if(!advanced){
            next = findNext();
            advanced = true;
        }
        return next!=null;
    }

    @Override
    public final E next(){
        if(current.finished())
            return null;
        else if(advanced){
            advanced = false;
            current.set(next);
            next = null;
        }else
            current.set(findNext());

        return current.get();
    }

    @Override
    public final E next(int count){
        if(current.finished())
            return null;
        
        if(count<=0)
            return current();
        else if(count==1)
            return next();

        if(advanced){
            next();
            return next(count-1);
        }

        Element<E> elem = findNext(count);
        if(elem==null){
            while(count>0){
                if(next()==null)
                    break;
                count--;
            }
        }else
            current = elem;
        
        return current();
    }

    protected abstract E findNext();
    @SuppressWarnings({"UnusedDeclaration"})
    protected Element<E> findNext(int count){
        return null;
    }

    /*-------------------------------------------------[ Query ]---------------------------------------------------*/
    
    protected Element<E> current = new Element<E>();

    @Override
    public int index(){
        return current.index();
    }

    @Override
    public final E current(){
        return current.get();
    }

    @Override
    public int length(){
        Sequence<E> seq = copy();

        int len = 0;
        while(seq.next()!=null)
            len++;

        return len;
    }

    /*-------------------------------------------------[ Reset ]---------------------------------------------------*/

    private void _reset(){
        current.reset();
        next = null;
        advanced = false;
    }

    public void reset(){
        _reset();
    }    
}
