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

package jlibs.nio.async;

import jlibs.nio.BufferPool;
import jlibs.nio.Reactor;
import jlibs.nio.channels.ListenerUtil;
import jlibs.nio.channels.OutputChannel;
import jlibs.nio.channels.filters.ChunkedOutputFilter;
import jlibs.nio.util.Bytes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * @author Santhosh Kumar Tekuri
 */
public class WriteBytes extends OutputTask{
    private Bytes bytes;
    private BufferPool pool;
    private Iterator<ByteBuffer> iterator;
    private ByteBuffer buffer;

    public WriteBytes(Bytes bytes, boolean clearBytes){
        this.bytes = bytes;
        if(clearBytes){
            if(bytes.canPool)
                pool = Reactor.current().bufferPool;
        }else
            iterator = bytes.iterator();
    }

    public WriteBytes(Bytes.Encodable encodable) throws IOException{
        this(encodable.encodeTo(new Bytes()), true);
    }

    private ByteBuffer next(){
        if(iterator!=null){
            while(iterator.hasNext()){
                buffer = iterator.next().duplicate();
                if(buffer.hasRemaining())
                    return buffer;
            }
        }else{
            while(!bytes.isEmpty()){
                if(buffer!=null && pool!=null)
                    pool.returnBack(buffer);
                buffer = bytes.remove();
                if(buffer.hasRemaining())
                    return buffer;
            }
        }
        return null;
    }

    public void start(OutputChannel out, ExecutionContext context){
        super.start(out, context);
        try{
            if(out instanceof ChunkedOutputFilter)
                ((ChunkedOutputFilter)out).startChunk(bytes.size());
            buffer = next();
            if(buffer==null)
                ListenerUtil.resume(detach(out), null, false);
            else
                ready(out);
        }catch(Throwable thr){
            ListenerUtil.resume(detach(out), thr, false);
        }
    }

    @Override
    public void ready(OutputChannel out) throws IOException{
        while(true){
            if(out.write(buffer)==0){
                out.addWriteInterest();
                return;
            }
            if(!buffer.hasRemaining()){
                buffer = next();
                if(buffer==null){
                    if(out.isFlushed())
                        ListenerUtil.resume(detach(out), null, false);
                    else
                        out.addWriteInterest();
                    return;
                }
            }
        }
    }
}
