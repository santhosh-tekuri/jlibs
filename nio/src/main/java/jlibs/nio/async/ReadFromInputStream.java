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
import jlibs.nio.util.Bytes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ReadFromInputStream extends OutputTask{
    private BufferPool pool = Reactor.current().bufferPool;
    private InputStream in;
    private ByteBuffer buffer;
    private Bytes backup;

    public ReadFromInputStream(InputStream in, Bytes backup){
        this(in, backup.chunkSize);
        this.backup = backup;
    }

    public ReadFromInputStream(InputStream in, int chunkSize){
        this.in = in;
        buffer = pool.borrow(chunkSize);
        buffer.limit(0);
    }

    public void start(OutputChannel out, ExecutionContext context){
        super.start(out, context);
        try{
            ready(out);
        }catch(Throwable thr){
            resume(out, thr, false);
        }
    }

    private void resume(OutputChannel out, Throwable thr, boolean timeout){
        if(buffer!=null){
            if(backup==null || buffer.limit()==0)
                pool.returnBack(buffer);
            else{
                buffer.position(0);
                backup.append(buffer);
            }
            buffer = null;
        }
        try{
            if(in!=null)
                in.close();
        }catch(IOException ex){
            if(thr!=null)
                thr.addSuppressed(ex);
            else
                Reactor.current().handleException(ex);
        }
        ListenerUtil.resume(detach(out), thr, timeout);
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
                resume(out, null, false);
                return;
            }
            if(backup==null){
                buffer.clear();
                buffer.limit(0);
            }else if(buffer.limit()==buffer.array().length){
                buffer.clear();
                backup.append(buffer);
                buffer = pool.borrow(backup.chunkSize);
                buffer.limit(0);
            }
            int read = in.read(buffer.array(), buffer.limit(), buffer.array().length-buffer.limit());
            if(read==-1){
                InputStream is = this.in;
                in = null;
                try{
                    is.close();
                }catch(IOException ex){
                    Reactor.current().handleException(ex);
                }
                if(out.isFlushed())
                    resume(out, null, false);
                else
                    out.addWriteInterest();
                return;
            }else{
                buffer.position(buffer.limit());
                buffer.limit(buffer.limit()+read);
            }
        }
    }

    @Override
    public void timeout(OutputChannel out) throws IOException{
        resume(out, null, true);
    }

    @Override
    public void error(OutputChannel out, Throwable thr){
        resume(out, thr, false);
    }

    @Override
    public void closed(OutputChannel out) throws IOException{
        resume(out, null, false);
    }
}
