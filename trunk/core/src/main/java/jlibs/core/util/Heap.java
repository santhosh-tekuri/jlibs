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

package jlibs.core.util;

import java.util.Arrays;

/**
 * @author Santhosh Kumar T
 */
@SuppressWarnings({"unchecked"})
public abstract class Heap<E>{
    private Object queue[];
    private int size = 0;

    public Heap(int initialCapacity){
        queue = new Object[initialCapacity];
    }

    private void grow(int minCapacity){
        if(minCapacity<0) // overflow
            throw new OutOfMemoryError();
        int oldCapacity = queue.length;

        // Double size if small; else grow by 50%
        int newCapacity = ((oldCapacity<64) ? ((oldCapacity+1)*2): ((oldCapacity/2)*3));
        if (newCapacity < 0) // overflow
            newCapacity = Integer.MAX_VALUE;
        if (newCapacity < minCapacity)
            newCapacity = minCapacity;
        queue = Arrays.copyOf(queue, newCapacity);
    }

    public int size(){
        return size;
    }

    public E root(){
        return size==0 ? null : (E)queue[0];
    }

    public void add(E e){
        if(e==null)
            throw new NullPointerException();
        int i = size;
        if(i>= queue.length)
            grow(i+1);
        size = i+1;
        if(i==0){
            queue[0] = e; setIndex(e, 0);
        }else
            siftUp(i, e);
    }

    private void siftUp(int k, E x){
        while(k>0) {
            int parent = (k-1)>>>1;
            E e = (E)queue[parent];
            if (compare(x, e)>=0)
                break;
            queue[k] = e; setIndex(e, k);
            k = parent;
        }
        queue[k] = x; setIndex(x, k);
    }

    public E get(int i){
        if(i>=size)
            return null;
        return (E)queue[i];
    }

    public E removeAt(int i){
        if(i>=size)
            return null;
        E removed = (E)queue[i];
        int s = --size;
        if(s==i) // removed last element
            queue[i] = null;
        else{
            E moved = (E)queue[s];
            queue[s] = null;
            siftDown(i, moved);
        }
        assert queue[i]!=removed;
        setIndex(removed, -1);
        return removed;
    }

    private void siftDown(int k, E x){
        int half = size >>> 1;
        while(k<half){
            int child = (k<<1)+1;
            E c = (E)queue[child];
            int right = child+1;
            if(right<size && compare(c, (E)queue[right])>0)
                c = (E)queue[child=right];
            if(compare(x, c)<=0)
                break;
            queue[k] = c; setIndex(c, k);
            k = child;
        }
        queue[k] = x; setIndex(x, k);
    }

    protected abstract void setIndex(E elem, int index);
    protected abstract int compare(E elem1, E elem2);
}
