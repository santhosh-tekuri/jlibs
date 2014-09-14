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

import java.nio.ByteBuffer;

import static jlibs.nio.Debugger.DEBUG;
import static jlibs.nio.Debugger.println;

/**
 * @author Santhosh Kumar Tekuri
 */
public class UnpooledBufferAllocator implements BufferAllocator{
    public static final UnpooledBufferAllocator HEAP = new UnpooledBufferAllocator(false);
    public static final UnpooledBufferAllocator DIRECT = new UnpooledBufferAllocator(true);

    private boolean directPreferred;
    private UnpooledBufferAllocator(boolean directPreferred){
        this.directPreferred = directPreferred;
    }

    @Override
    public boolean directPreferred(){
        return directPreferred;
    }

    @Override
    public ByteBuffer allocateHeap(int size){
        if(DEBUG)
            println("unpool.allocate("+size+")");
        return ByteBuffer.allocate(size);
    }

    @Override
    public ByteBuffer allocateDirect(int size){
        return ByteBuffer.allocateDirect(size);
    }

    @Override
    public void free(ByteBuffer buffer){
        // do nothing
    }
}
