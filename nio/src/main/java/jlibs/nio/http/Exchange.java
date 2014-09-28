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

import jlibs.nio.*;
import jlibs.nio.http.expr.Bean;
import jlibs.nio.http.expr.UnresolvedException;
import jlibs.nio.http.msg.AsciiString;
import jlibs.nio.http.msg.Method;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Response;
import jlibs.nio.http.msg.parser.MessageParser;
import jlibs.nio.http.util.Cookie;
import jlibs.nio.listeners.Task;

import java.io.Closeable;
import java.nio.channels.SelectionKey;
import java.util.IdentityHashMap;
import java.util.Map;

import static java.nio.channels.SelectionKey.OP_READ;
import static jlibs.nio.Debugger.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class Exchange extends Task implements Bean, Closeable{
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
        attachments = null;
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

    public int getRequestCount(){
        return in==null ? 0 : in.channel().getTaskID();
    }

    @Override
    public String toString(){
        return getClass().getSimpleName()+"["+getEndpoint()+"]";
    }

    /*-------------------------------------------------[ Attachments ]---------------------------------------------------*/

    private Map<Key, Object> attachments;

    @SuppressWarnings("unchecked")
    public <T> T attachment(Key<T> key){
        if(attachments ==null)
            return key.defaultValue;
        else{
            T value = (T)attachments.get(key);
            return value==null ? key.defaultValue : value;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T attach(Key<T> key, T value){
        if(attachments ==null)
            attachments = new IdentityHashMap<>();
        T oldValue = (T)attachments.put(key, value);
        return oldValue==null ? key.defaultValue : value;
    }

    @SuppressWarnings("unchecked")
    public <T> T detach(Key<T> key){
        if(attachments ==null)
            return key.defaultValue;
        T value = (T)attachments.remove(key);
        return value==null ? key.defaultValue : value;
    }

    /*-------------------------------------------------[ Bean ]---------------------------------------------------*/

    @Override
    @SuppressWarnings("StringEquality")
    public Object getField(String name) throws UnresolvedException{
        if(name=="request")
            return getRequest();
        else if(name=="response")
            return getResponse();
        else if(name=="scheme"){
            TCPEndpoint endpoint = getEndpoint();
            return endpoint==null ? null : endpoint.sslContext==null ? "http" : "https";
        }else if(name=="host"){
            TCPEndpoint endpoint = getEndpoint();
            return endpoint==null ? null : endpoint.host;
        }else if(name=="port"){
            TCPEndpoint endpoint = getEndpoint();
            return endpoint==null ? null : endpoint.port;
        }else if(name=="connection_status")
            return connectionStatus;
        else if(name=="request_count")
            return getRequestCount();
        else if(name=="id")
            return in==null ? null : in.channel().getExecutionID();
        else if(name=="ssl_session"){
            if(in==null)
                return null;
            Input in = this.in;
            while(in instanceof InputFilter)
                in = ((InputFilter)in).peer();
            return in instanceof SSLSocket ? in : null;
        }else
            throw new UnresolvedException(name);
    }
}
