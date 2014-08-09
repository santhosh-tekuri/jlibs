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

package jlibs.nio.channels.filters;

import jlibs.nio.Reactor;
import jlibs.nio.channels.impl.filters.AbstractOutputFilterChannel;
import jlibs.nio.util.Bytes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;

import static java.nio.channels.SelectionKey.OP_WRITE;

/**
 * @author Santhosh Kumar Tekuri
 */
public class DeflaterOutputFilter extends AbstractOutputFilterChannel{
    protected Deflater deflater;
    private ByteBuffer buffer;

    public DeflaterOutputFilter(){
        this(new Deflater());
    }

    protected DeflaterOutputFilter(Deflater deflater){
        this.deflater = deflater;
        buffer = Reactor.current().bufferPool.borrow(Bytes.CHUNK_SIZE);
        buffer.position(buffer.limit());
    }

    @Override
    protected void _process() throws IOException{
        while(true){
            while(buffer.hasRemaining()){
                if(peerOutput.write(buffer)==0){
                    selfInterestOps |= OP_WRITE;
                    return;
                }
            }
            if(isOpen()){
                if(deflater.needsInput())
                    return;
            }else if(deflater.finished()){
                deflater.end();
                deflater = null;
                Reactor.current().bufferPool.returnBack(buffer);
                buffer = null;
                return;
            }
            int compressed = deflater.deflate(buffer.array(), 0, buffer.capacity());
            buffer.position(0);
            buffer.limit(compressed);
        }
    }

    @Override
    protected int _write(ByteBuffer src) throws IOException{
        if(src.hasRemaining() && deflater.needsInput()){
            int wrote = src.remaining();
            deflater.setInput(src.array(), src.arrayOffset()+src.position(), wrote);
            src.position(src.limit());
            _process();
            return wrote;
        }else
            return 0;
    }

    @Override
    protected void _close() throws IOException{
        deflater.finish();
        _process();
    }

    @Override
    public void dispose(){
        if(deflater!=null){
            deflater.end();
            deflater = null;
            Reactor.current().bufferPool.returnBack(buffer);
            buffer = null;
        }
    }
}
