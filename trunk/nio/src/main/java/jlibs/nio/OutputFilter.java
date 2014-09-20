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

package jlibs.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;

import static jlibs.nio.Debugger.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class OutputFilter implements Output{
    protected final Output peer;
    protected Socket transport;

    public OutputFilter(Output peer){
        this.peer = peer;
        transport = peer.channel().transport;
        transport.peekOut = this;
    }

    @Override
    public NBStream channel(){
        return transport.channel();
    }

    public Output peer(){
        return peer;
    }

    @Override
    public Listener getOutputListener(){
        return transport.getOutputListener();
    }

    @Override
    public final void setOutputListener(Output.Listener listener){
        transport.setOutputListener(listener);
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException{
        return write(srcs, 0, srcs.length);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException{
        while(length>0){
            if(srcs[offset].hasRemaining())
                return write(srcs[offset]);
            ++offset;
            --length;
        }
        return 0;
    }

    @Override
    public long transferFrom(FileChannel src, long position, long count) throws IOException{
        return src.transferTo(position, count, this);
    }

    @Override
    public final void addWriteInterest(){
        if(transport.peekOut==this)
            transport.peekOutInterested = true;
        if(writeReady())
            wakeupWriter();
        else
            peer.addWriteInterest();
    }

    @Override
    public void wakeupWriter(){
        transport.wakeupWriter();
    }

    protected boolean writeReady(){
        return false;
    }

    private boolean open = true;

    @Override
    public boolean isOpen(){
        return open;
    }

    @Override
    public final void close() throws IOException{
        if(open){
            if(DEBUG)
                enter(getClass().getSimpleName()+".close");
            open = false;
            _close();
            if(DEBUG)
                exit();
        }
    }

    protected void _close() throws IOException{}

    protected abstract boolean _flush() throws IOException;
    public final boolean flush() throws IOException{
        if(DEBUG)
            enter(getClass().getSimpleName()+".flush");
        boolean flushed = _flush() && peer.flush();
        if(DEBUG)
            exit();
        return flushed;
    }

    protected void detached(){}

    @Override
    public Output detachOutput(){
        if(DEBUG)
            println(getClass().getSimpleName()+".detachOutput");
        if(transport.peekOut ==this){
            detached();
            return transport.peekOut = peer;
        }else
            return this;
    }

    protected final void ensureOpen() throws IOException{
        if(!isOpen())
            throw new ClosedChannelException();
    }

    @Override
    public String toString(){
        String name = getClass().getSimpleName();
        if(name.endsWith("Output"))
            name = name.substring(0, name.length()-"Output".length());
        return name+'_'+peer;
    }
}
