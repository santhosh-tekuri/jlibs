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
import jlibs.nio.Client;
import jlibs.nio.Reactor;
import jlibs.nio.channels.InputChannel;
import jlibs.nio.channels.ListenerUtil;
import jlibs.nio.channels.OutputChannel;
import jlibs.nio.channels.filters.ChunkedOutputFilter;
import jlibs.nio.util.Bytes;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Pump implements InputChannel.Listener, OutputChannel.Listener{
    private BufferPool pool = Reactor.current().bufferPool;
    private InputChannel in;
    private OutputChannel out;

    private boolean readMode = true;
    private ByteBuffer buffer;

    public Pump(InputChannel in, OutputChannel out){
        this(in, out, Bytes.CHUNK_SIZE);
    }

    private Bytes bytes;
    private Client activeClient;
    public Pump(InputChannel in, OutputChannel out, Bytes backup){
        this(in, out, backup.chunkSize);
        this.bytes = backup;
    }

    public Pump(InputChannel in, OutputChannel out, int bufferSize){
        this.in = in;
        this.out = out;
        buffer = pool.borrow(bufferSize);
        activeClient = Reactor.current().getActiveClient();
    }

    private ExecutionContext context;
    public void start(ExecutionContext context){
        this.context = context;
        in.setInputListener(this);
        out.setOutputListener(this);
        try{
            ready();
        }catch(Throwable thr){
            if(readMode)
                error(in, thr);
            else
                error(out, thr);
        }
    }

    private Throwable thr;
    private boolean timeout;
    private void resume(){
        if(context==null)
            return;
        assert !in.isOpen();
        assert !out.isOpen();
        in.setInputListener(null);
        out.setOutputListener(null);
        activeClient.makeActive();
        ListenerUtil.resume(context, thr, timeout);
        context = null;
    }

    private int begin = 0;
    private void flip(){
        if(bytes==null)
            buffer.flip();
        else{
            buffer.limit(buffer.position());
            buffer.position(begin);
        }
    }

    private void clear(){
        if(bytes==null)
            buffer.clear();
        else{
            if(buffer.limit()==buffer.capacity()){
                begin = 0;
                buffer.flip();
                bytes.append(buffer);
                buffer = pool.borrow(bytes.chunkSize);
            }else{
                begin = buffer.limit();
                buffer.position(begin);
                buffer.limit(buffer.capacity());
            }
        }
    }

    private void cleanup(){
        if(buffer==null)
            return;
        if(bytes!=null){
            if(readMode){
                if(buffer.position()!=0){
                    buffer.flip();
                    bytes.append(buffer);
                    buffer = null;
                }
            }else{
                if(buffer.limit()!=0){
                    buffer.position(0);
                    bytes.append(buffer);
                    buffer = null;
                }
            }
        }
        if(buffer!=null){
            pool.returnBack(buffer);
            buffer = null;
        }
    }

    /*-------------------------------------------------[ Action ]---------------------------------------------------*/

    private void ready(){
        try{
            while(true){
                if(readMode){
                    int read = in.read(buffer);
                    if(read==0){
                        in.addReadInterest();
                        return;
                    }else if(read==-1){
                        cleanup();
                        in.close();
                        return;
                    }else{
                        flip();
                        readMode = false;
                    }
                }

                if(out instanceof ChunkedOutputFilter){
                    long available = in.available();
                    if(available>0)
                        ((ChunkedOutputFilter)out).startChunk(buffer.remaining()+available);
                }
                while(buffer.hasRemaining()){
                    if(out.write(buffer)==0){
                        out.addWriteInterest();
                        return;
                    }
                }
                clear();
                readMode = true;
            }
        }catch(Throwable thr){
            if(readMode)
                error(in, thr);
            else
                error(out, thr);
        }
    }

    @Override
    public void ready(InputChannel in){
        assert readMode;
        ready();
    }

    @Override
    public void ready(OutputChannel out){
        assert !readMode;
        ready();
    }

    /*-------------------------------------------------[ closed ]---------------------------------------------------*/

    @Override
    public void closed(OutputChannel out) throws IOException{
        if(in.isOpen())  // out might have closed by another pump
            in.close();
        else if(in.isClosed()){
            cleanup();
            resume();
        }
    }

    @Override
    public void closed(InputChannel in) throws IOException{
        if(out.isOpen()) // in might have closed by another pump
            out.close();
        else if(out.isClosed()){
            cleanup();
            resume();
        }
    }

    /*-------------------------------------------------[ closed ]---------------------------------------------------*/

    private void timeout(boolean inTimeout) throws IOException{
        if(thr==null)
            thr = inTimeout ? new InputChannelException(null) : new OutputChannelException(null);
        timeout = true;
        if(in.isOpen())
            in.close();
        else if(out.isOpen())
            out.close();
        else if(in.isClosed() && out.isClosed())
            resume();
    }

    @Override
    public void timeout(InputChannel in) throws IOException{
        timeout(true);
    }

    @Override
    public void timeout(OutputChannel out) throws IOException{
        timeout(false);
    }

    /*-------------------------------------------------[ closed ]---------------------------------------------------*/

    private void error(boolean inError, Throwable thr){
        if(this.thr==null)
            this.thr = inError ? new InputChannelException(thr) : new OutputChannelException(thr);
        else
            this.thr.addSuppressed(thr);
        if(in.isOpen()){
            try{
                in.close();
            }catch(Throwable ex){
                if(!in.isClosed())
                    error(true, ex);
            }
        }else if(out.isOpen()){
            try{
                out.close();
            }catch(Throwable ex){
                if(!out.isClosed())
                    error(false, ex);
            }
        }else{
            cleanup();
            resume();
        }
    }

    @Override
    public void error(InputChannel in, Throwable thr){
        error(true, thr);
    }

    @Override
    public void error(OutputChannel out, Throwable thr){
        error(false, thr);
    }

    /*-------------------------------------------------[ Utility ]---------------------------------------------------*/

    public static void startTunnel(Client client, Client buddy){
        ExecutionContext context = ExecutionContext.closeClients(client, buddy);
        new Pump(client.in(), buddy.out()).start(context);
        new Pump(buddy.in(), client.out()).start(context);
    }
}
