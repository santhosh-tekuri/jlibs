/*
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

package jlibs.nio;

import jlibs.core.util.LongTreeMap;
import jlibs.nio.util.Bytes;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class BufferPool{
    private LongTreeMap<SoftReference<Bytes>> map = new LongTreeMap<>();

    private Bytes get(int size, boolean create){
        LongTreeMap.Entry<SoftReference<Bytes>> entry = map.getEntry(size);
        Bytes bytes = null;
        if(entry!=null){
            bytes = entry.value.get();
            if(bytes==null){
                if(create)
                    entry.value = new SoftReference<>(bytes=new Bytes(size));
                else
                    map.deleteEntry(entry);
            }
        }else if(create)
            map.put(size, new SoftReference<>(bytes=new Bytes(size)));

        return bytes;
    }

    public ByteBuffer borrow(){
        return borrow(Bytes.CHUNK_SIZE);
    }

    public ByteBuffer borrow(int size){
        Bytes bytes = get(size, false);
        if(bytes==null || bytes.isEmpty())
            return ByteBuffer.allocate(size);
        else
            return bytes.remove();
    }

    public void returnBack(ByteBuffer buffer){
        buffer.clear();
        get(buffer.capacity(), true).append(buffer);
    }

    public void returnBack(Bytes bytes){
        while(!bytes.isEmpty())
            returnBack(bytes.remove());
    }
}
