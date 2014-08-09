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

import jlibs.nio.channels.ListenerUtil;
import jlibs.nio.channels.OutputChannel;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Santhos Kumar Tekuri
 */
public class WriteBuffer extends OutputTask{
    private ByteBuffer buffer;

    public WriteBuffer(ByteBuffer buffer){
        this.buffer = buffer;
    }

    public void start(OutputChannel out, ExecutionContext context){
        super.start(out, context);
        if(buffer.hasRemaining()){
            try{
                ready(out);
            }catch(Throwable thr){
                ListenerUtil.resume(detach(out), thr, false);
            }
        }else
            ListenerUtil.resume(detach(out), null, false);
    }

    @Override
    public void ready(OutputChannel out) throws IOException{
        while(buffer.hasRemaining()){
            if(out.write(buffer)==0){
                out.addWriteInterest();
                return;
            }
        }
        if(out.isFlushed())
            ListenerUtil.resume(detach(out), null, false);
        else
            out.addWriteInterest();
    }
}
