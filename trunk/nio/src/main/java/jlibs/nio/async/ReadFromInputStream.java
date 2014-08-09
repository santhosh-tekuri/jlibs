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
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ReadFromInputStream extends OutputTask{
    private InputStream in;
    private ByteBuffer buffer;

    public ReadFromInputStream(InputStream in, int chunkSize){
        this.in = in;
        buffer = ByteBuffer.allocate(chunkSize);
        buffer.limit(0);
    }

    public void start(OutputChannel out, ExecutionContext context){
        super.start(out, context);
        try{
            ready(out);
        }catch(Throwable thr){
            ListenerUtil.resume(detach(out), thr, false);
        }
    }

    @Override
    public void ready(OutputChannel out) throws IOException{
        while(true){
            while(buffer.hasRemaining()){
                if(out.write(buffer) == 0){
                    out.addWriteInterest();
                    return;
                }
            }
            if(in==null){
                ListenerUtil.resume(detach(out), null, false);
                return;
            }
            int read = in.read(buffer.array());
            if(read==-1){
                in.close();
                in = null;
                if(out.isFlushed())
                    ListenerUtil.resume(detach(out), null, false);
                else
                    out.addWriteInterest();
                return;
            }else{
                buffer.position(0);
                buffer.limit(read);
            }
        }
    }
}
