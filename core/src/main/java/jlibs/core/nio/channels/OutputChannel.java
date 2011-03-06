/**
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

package jlibs.core.nio.channels;

import jlibs.core.nio.AttachmentSupport;
import jlibs.core.nio.ClientChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * @author Santhosh Kumar T
 */
public abstract class OutputChannel extends AttachmentSupport implements WritableByteChannel{
    protected final ClientChannel client;

    protected OutputChannel(ClientChannel client){
        this.client = client;
        IOChannelHandler handler = (IOChannelHandler)client.attachment();
        if(handler==null)
            client.attach(handler=new IOChannelHandler());
        handler.output = this;
    }

    public final ClientChannel client(){
        return client;
    }

    private boolean statusInterested;
    public final void addStatusInterest(){
        statusInterested = true;
    }

    public final void removeStatusInterest(){
        statusInterested = false;
    }

    private boolean writeInterested;
    public final void addWriteInterest() throws IOException{
        if(status()!=Status.NEEDS_OUTPUT){
            if(handler!=null){
                try{
                    handler.onWrite(this);
                }catch(Throwable ex){
                    try{
                        handler.onError(this, ex);
                    }catch(Throwable ex1){
                        throw ex1 instanceof IOException ? (IOException)ex1 : new IOException(ex1);
                    }
                }
            }
        }else
            writeInterested = true;
    }

    protected abstract boolean activateInterest();

    public void removeWriteInterest() throws IOException{
        writeInterested = false;
        if(status()!=Status.NEEDS_OUTPUT)
            client.removeInterest(ClientChannel.OP_WRITE);
    }

    protected OutputHandler handler;
    public void setHandler(OutputHandler handler){
        this.handler = handler;
    }

    @Override
    public final int write(ByteBuffer src) throws IOException{
        if(src.remaining()==0 || status()==Status.NEEDS_OUTPUT)
            return 0;
        int wrote = onWrite(src);
        if(wrote>0)
            onWrite();
        return wrote;
    }

    protected abstract int onWrite(ByteBuffer src) throws IOException;

    private boolean closed;

    @Override
    public final boolean isOpen(){
        return !closed;
    }

    @Override
    public final void close() throws IOException{
        closed = true;
        writeInterested = false;
        doClose();
        onWrite();
    }

    protected void doClose() throws IOException{}

    protected final void onWrite() throws IOException{
        Status earlierStatus = status();
        if(earlierStatus==Status.NEEDS_OUTPUT)
            writePending();
        Status curStatus = status();
        if(curStatus==Status.NEEDS_OUTPUT)
            client.addInterest(ClientChannel.OP_WRITE);
        else
            notifyCompleted(earlierStatus, curStatus);
    }

    protected abstract void writePending() throws IOException;
    public abstract Status status();
    protected void notifyCompleted(Status earlierStatus, Status curStatus){
        IOChannelHandler handler = (IOChannelHandler)client.attachment();
        if(handler.output.statusInterested){
            handler.output.statusInterested = false;
            try{
                if(earlierStatus!=curStatus && handler.output.handler!=null)
                    handler.output.handler.onStatus(handler.output);
            }catch(Throwable error){
                try{
                    handler.output.handler.onError(handler.output, error);
                }catch(Throwable error1){
                    error1.printStackTrace();
                }
            }
        }
        if(handler.output.writeInterested){
            handler.output.writeInterested = false;
            try{
                if(handler.output.handler!=null)
                    handler.output.handler.onWrite(handler.output);
            }catch(Throwable error){
                try{
                    handler.output.handler.onError(handler.output, error);
                }catch(Throwable error1){
                    error1.printStackTrace();
                }
            }
        }
    }

    public enum Status{ NEEDS_INPUT, NEEDS_OUTPUT, COMPLETED }
}
