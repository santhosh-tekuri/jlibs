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

package jlibs.nio.util;

import jlibs.core.util.LongTreeMap;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;

import static jlibs.nio.Debugger.DEBUG;
import static jlibs.nio.Debugger.println;

/**
 * @author Santhosh Kumar Tekuri
 */
public class PooledBufferAllocator implements BufferAllocator{
    private boolean directPreferred;
    public PooledBufferAllocator(boolean directPreferred){
        this.directPreferred = directPreferred;
    }

    @SuppressWarnings("unchecked")
    private LongTreeMap<SoftReference<Buffers>> map[] = new LongTreeMap[]{ new LongTreeMap<>(), new LongTreeMap<>() };

    private Buffers get(LongTreeMap<SoftReference<Buffers>> map, int size, boolean create){
        LongTreeMap.Entry<SoftReference<Buffers>> entry = map.getEntry(size);
        Buffers bytes = null;
        if(entry!=null){
            bytes = entry.value.get();
            if(bytes==null){
                if(create)
                    entry.value = new SoftReference<>(bytes=new Buffers());
                else
                    map.deleteEntry(entry);
            }
        }else if(create)
            map.put(size, new SoftReference<>(bytes=new Buffers()));

        return bytes;
    }

    @Override
    public boolean directPreferred(){
        return directPreferred;
    }

    @Override
    public ByteBuffer allocateHeap(int size){
        Buffers buffers = get(map[0], size, false);
        if(buffers==null || buffers.length==0){
            if(DEBUG)
                println("pool.allocate("+size+")");
            return ByteBuffer.allocate(size);
        }else{
            return buffers.removeLast();
        }
    }

    @Override
    public ByteBuffer allocateDirect(int size){
        Buffers buffers = get(map[1], size, false);
        if(buffers==null || buffers.length==0){
            return ByteBuffer.allocateDirect(size);
        }else
            return buffers.removeLast();
    }

    public void free(ByteBuffer buffer){
        buffer.clear();
        get(map[buffer.isDirect() ? 1 : 0], buffer.capacity(), true).append(buffer);
    }
}
