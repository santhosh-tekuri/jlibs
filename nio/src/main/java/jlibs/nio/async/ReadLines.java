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

import jlibs.nio.Reactor;
import jlibs.nio.channels.InputChannel;
import jlibs.nio.util.Bytes;
import jlibs.nio.util.Line;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ReadLines extends InputTask{
    private final Line line;
    protected final Line.Consumer consumer;

    public ReadLines(Line line, Line.Consumer consumer){
        this.line = line;
        this.consumer = consumer;
    }

    private boolean ignoreEOF;
    public ReadLines ignoreEOF(boolean ignoreEOF){
        this.ignoreEOF = ignoreEOF;
        return this;
    }

    public void start(InputChannel in, ExecutionContext context){
        super.start(in, context);
        try{
            ready(in);
        }catch(Throwable thr){
            error(in, thr);
        }
    }

    private ByteBuffer buffer = Reactor.current().bufferPool.borrow(Bytes.CHUNK_SIZE);

    @Override
    protected void cleanup(){
        if(buffer!=null){
            Reactor.current().bufferPool.returnBack(buffer);
            buffer = null;
        }
    }

    @Override
    public void ready(InputChannel in) throws IOException{
        while(true){
            buffer.clear();
            int read = in.read(buffer);
            buffer.flip();
            if(read==0){
                in.addReadInterest();
                return;
            }else if(read==-1){
                boolean ignore = ignoreEOF && line.lineCount()==0 && line.length()==0;
                resume(in, ignore ? IgnorableEOFException.INSTANCE : new EOFException(), false);
                return;
            }
            while(buffer.hasRemaining()){
                if(line.consume(buffer)){
                    consumer.consume(line);
                    if(line.length()==0){
                        if(buffer.hasRemaining()){
                            in.unread(buffer);
                            buffer = null;
                        }
                        resume(in, null, false);
                        return;
                    }else
                        line.reset();
                }
            }
        }
    }

    @Override
    public void error(InputChannel in, Throwable thr){
        if(ignoreEOF && line.lineCount()==0 && line.length()==0)
            thr = IgnorableEOFException.INSTANCE;
        super.error(in, thr);
    }
}
