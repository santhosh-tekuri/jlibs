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

    private boolean interested;
    public final void addInterest() throws IOException{
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
            interested = true;
    }

    protected abstract boolean activateInterest();

    public void removeInterest() throws IOException{
        interested = false;
        if(status()!=Status.NEEDS_OUTPUT)
            client.removeInterest(ClientChannel.OP_WRITE);
    }

    protected OutputHandler handler;
    public void setHandler(OutputHandler handler){
        this.handler = handler;
    }

    private boolean closed;

    @Override
    public final boolean isOpen(){
        return !closed;
    }

    @Override
    public final void close() throws IOException{
        closed = true;
        interested = false;
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
        if(handler.output.interested){
            handler.output.interested = false;
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

    enum Status{ NEEDS_INPUT, NEEDS_OUTPUT, COMPLETED }
}
