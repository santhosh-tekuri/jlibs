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
import jlibs.nio.channels.InputChannel;
import jlibs.nio.util.Bytes;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ReadBytes extends ClosingInputTask{
    private BufferPool pool = Reactor.current().bufferPool;
    private Bytes bytes;
    private ByteBuffer buffer;

    public ReadBytes(Bytes bytes){
        this.bytes = bytes;
        buffer = pool.borrow(bytes.chunkSize);
    }

    public Bytes getBytes(){
        return bytes;
    }

    public void start(InputChannel in, ExecutionContext context){
        super.start(in, context);
        try{
            ready(in);
        }catch(Throwable thr){
            error(in, thr);
        }
    }

    @Override
    protected void cleanup(){
        if(buffer!=null){
            if(buffer.position()==0)
                pool.returnBack(buffer);
            else{
                buffer.flip();
                bytes.append(buffer);
            }
            buffer = null;
        }
    }

    @Override
    public void ready(InputChannel in) throws IOException{
        while(true){
            int read = in.read(buffer);
            if(read==0){
                in.addReadInterest();
                return;
            }else if(read==-1){
                in.close();
                return;
            }else if(!buffer.hasRemaining()){
                buffer.flip();
                bytes.append(buffer);
                buffer = pool.borrow(bytes.chunkSize);
            }
        }
    }
}
