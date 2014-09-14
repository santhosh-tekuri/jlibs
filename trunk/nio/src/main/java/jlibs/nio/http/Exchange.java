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

package jlibs.nio.http;

import jlibs.nio.Connection;
import jlibs.nio.TCPEndpoint;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Response;
import jlibs.nio.http.msg.parser.MessageParser;
import jlibs.nio.listeners.Task;

import java.io.Closeable;
import java.nio.channels.SelectionKey;

import static java.nio.channels.SelectionKey.OP_READ;
import static jlibs.nio.Debugger.HTTP;
import static jlibs.nio.Debugger.enter;
import static jlibs.nio.Debugger.exit;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class Exchange extends Task implements Closeable{
    protected final ReadMessage readMessage;
    protected final WriteMessage writeMessage;

    protected Exchange(long maxHeadSize, MessageParser parser, int firstOp){
        super(firstOp);
        readMessage = new ReadMessage(maxHeadSize, parser);
        writeMessage = new WriteMessage();
    }

    protected void reset(){
        keepAlive = false;
        error = null;
        request = null;
        response = null;
    }

    @Override
    protected int childTaskFinished(Task childTask, Throwable thr){
        if(childTask instanceof ReadMessage){
            in = ((jlibs.nio.Readable)in.channel()).in();
            readMessageFinished(thr);
            return OP_READ;
        }else{
            writeMessageFinished(thr);
            return SelectionKey.OP_WRITE;
        }
    }

    protected abstract void readMessageFinished(Throwable thr);
    protected abstract void writeMessageFinished(Throwable thr);

    @Override
    protected void cleanup(Throwable thr){
        readMessage.dispose();
        writeMessage.dispose();
    }

    protected Request request;
    protected Response response;

    public Request getRequest(){ return request; }
    public Response getResponse(){ return response; }

    protected boolean keepAlive;

    public final void resume(){
        resume(null);
    }

    public final void resume(Throwable thr){
        if(HTTP)
            enter("resume("+thr+")");
        if(thr!=null)
            setError(thr);
        in.channel().makeActive();
        listener.process(in);
        if(HTTP)
            exit();
    }

    protected Throwable error = null;
    public Throwable getError(){
        return error;
    }
    protected abstract void setError(Throwable thr);

    @Override
    public void close(){
        if(in!=null){
            in.channel().close();
            connectionStatus = error==null ? ConnectionStatus.CLOSED : ConnectionStatus.ABORTED;
            in = null;
            out = null;
        }
    }

    public abstract TCPEndpoint getEndpoint();
    public abstract Connection stealConnection();

    protected ConnectionStatus connectionStatus;
    public ConnectionStatus getConnectionStatus(){
        return connectionStatus;
    }

    @Override
    public String toString(){
        return getClass().getSimpleName()+"["+getEndpoint()+"]";
    }

    private Object attachment;
    public void attach(Object attachment){
        this.attachment = attachment;
    }

    @SuppressWarnings("unchecked")
    public <A> A attachment(){
        return (A)attachment;
    }
}
