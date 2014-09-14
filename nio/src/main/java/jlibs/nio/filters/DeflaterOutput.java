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

import jlibs.nio.Output;
import jlibs.nio.OutputFilter;
import jlibs.nio.Reactor;
import jlibs.nio.util.NIOUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;

/**
 * @author Santhosh Kumar Tekuri
 */
public class DeflaterOutput extends OutputFilter{
    protected Deflater deflater;
    private ByteBuffer buffer;
    private ByteBuffer tmpBuffer;

    public DeflaterOutput(Output peer){
        this(new Deflater(), peer);
    }

    public DeflaterOutput(Deflater deflater, Output peer){
        super(peer);
        this.deflater = deflater;
        buffer = Reactor.current().allocator.allocateHeap();
        addHeader(buffer);
        buffer.flip();
    }

    protected void addHeader(ByteBuffer buffer){}

    protected boolean trailerAdded;
    protected void addTrailer(ByteBuffer buffer){}

    protected void setInput(byte bytes[], int offset, int length){
        deflater.setInput(bytes, offset, length);
    }

    @Override
    public int write(ByteBuffer src) throws IOException{
        ensureOpen();
        deflate(false);
        if(src.hasRemaining() && deflater.needsInput()){
            int wrote;
            if(src.hasArray()){
                wrote = src.remaining();
                setInput(src.array(), src.arrayOffset()+src.position(), wrote);
                src.position(src.limit());
            }else{
                if(tmpBuffer==null)
                    tmpBuffer = Reactor.current().allocator.allocateHeap();
                else
                    tmpBuffer.clear();
                wrote = NIOUtil.copy(src, tmpBuffer);
                setInput(tmpBuffer.array(), 0, tmpBuffer.position());
            }
            deflate(false);
            return wrote;
        }else
            return 0;
    }

    private boolean flushBuffer() throws IOException{
        while(buffer.hasRemaining()){
            if(peer.write(buffer)==0)
                return false;
        }
        buffer.position(0);
        buffer.limit(0);
        return true;
    }

    private void deflate(boolean flushCompletely) throws IOException{
        if(buffer.position()!=0 || buffer.remaining()==buffer.capacity()){
            if(!flushBuffer())
                return;
        }

        while(isOpen() ? !deflater.needsInput() : !deflater.finished()){
            int compressed = deflater.deflate(buffer.array(), buffer.limit(), buffer.capacity()-buffer.limit());
            if(compressed>0){
                buffer.limit(buffer.limit()+compressed);
                if(buffer.remaining()==buffer.capacity()){
                    if(!flushBuffer())
                        return;
                }
            }
        }

        if(!isOpen() && deflater.finished()){
            if(!trailerAdded){
                if(buffer.capacity()-buffer.limit()<8){
                    if(!flushBuffer())
                        return;
                }
                assert buffer.position()==0;
                buffer.position(buffer.limit());
                buffer.limit(buffer.capacity());
                addTrailer(buffer);
                buffer.limit(buffer.position());
                buffer.position(0);
                trailerAdded = true;
                deflater.end();
                deflater = null;
            }
        }

        if(flushCompletely)
            flushBuffer();
    }

    protected boolean _flush() throws IOException{
        deflate(true);
        return !buffer.hasRemaining();
    }


    @Override
    protected void _close() throws IOException{
        deflater.finish();
    }

    @Override
    protected void detached(){
        if(deflater!=null){
            deflater.end();
        }
        if(buffer!=null){
            Reactor.current().allocator.free(buffer);
            buffer = null;
        }
        if(tmpBuffer!=null){
            Reactor.current().allocator.free(tmpBuffer);
            tmpBuffer = null;
        }
    }
}
