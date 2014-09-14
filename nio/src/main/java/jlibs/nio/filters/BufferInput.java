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

package jlibs.nio.filters;

import jlibs.nio.Input;
import jlibs.nio.InputFilter;
import jlibs.nio.util.NIOUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static jlibs.nio.Debugger.IO;
import static jlibs.nio.Debugger.println;

/**
 * @author Santhosh Kumar Tekuri
 */
public class BufferInput extends InputFilter{
    private ByteBuffer src;
    public BufferInput(Input peer, ByteBuffer src){
        super(peer);
        if(IO)
            println(peer+".unread("+src.remaining()+")");
        this.src = src;
    }

    public static Input getOriginal(Input input){
        while(input instanceof BufferInput)
            input = ((BufferInput)input).peer;
        return input;
    }

    @Override
    protected boolean readReady(){
        return src!=null;
    }

    @Override
    public long available(){
        return peer.available() + (src==null ? 0 : src.remaining());
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException{
        if(src!=null){
            int read = NIOUtil.copy(src, dsts, offset, length);
            if(!src.hasRemaining()){
                channel().reactor.allocator.free(src);
                src = null;
            }
            return read;
        }
        long read = peer.read(dsts, offset, length);
        eof = read==-1;
        return read;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException{
        if(src!=null){
            int read = NIOUtil.copy(src, dst);
            if(!src.hasRemaining()){
                channel().reactor.allocator.free(src);
                src = null;
            }
            return read;
        }
        int read = peer.read(dst);
        eof = read==-1;
        return read;
    }

    @Override
    public long transferTo(long position, long count, FileChannel target) throws IOException{
        if(src!=null){
            int wrote = NIOUtil.transfer(src, target, position, count);
            if(!src.hasRemaining()){
                channel().reactor.allocator.free(src);
                src = null;
            }
            return wrote;
        }
        return peer.transferTo(position, count, target);
    }

    public void drainBuffer(){
        if(src!=null){
            channel().reactor.allocator.free(src);
            src = null;
        }
    }

    public boolean canDetach(){
        return src==null;
    }

    @Override
    public String toString(){
        String name = "Buffer";
        if(src!=null)
            name += "["+src.remaining()+"]";
        return name+'_'+peer;
    }
}
