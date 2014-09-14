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

import jlibs.nio.Reactor;

import java.nio.ByteBuffer;

import static jlibs.nio.util.BufferAllocator.Defaults.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public interface BufferAllocator{
    public static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);

    public default ByteBuffer allocate(){
        return allocate(CHUNK_SIZE);
    }
    public default ByteBuffer allocateHeap(){
        return allocateHeap(CHUNK_SIZE);
    }
    public default ByteBuffer allocateDirect(){
        return allocateDirect(CHUNK_SIZE);
    }
    public default ByteBuffer allocate(int size){
        return directPreferred() ? allocateDirect(size) : allocateHeap(size);
    }
    public boolean directPreferred();
    public ByteBuffer allocateHeap(int size);
    public ByteBuffer allocateDirect(int size);
    public void free(ByteBuffer buffer);

    public default void free(Buffers buffers){
        while(buffers.length>0)
            free(buffers.remove());
    }
    
    public static BufferAllocator current(){
        Reactor reactor = Reactor.current();
        if(reactor==null)
            return USE_DIRECT_BUFFERS ? UnpooledBufferAllocator.DIRECT : UnpooledBufferAllocator.HEAP;
        else
            return reactor.allocator;
    }

    public class Defaults{
        public static int CHUNK_SIZE = 16*1024;
        public static boolean USE_DIRECT_BUFFERS = true;
        public static boolean POOL_BUFFERS = true;
    }
}
