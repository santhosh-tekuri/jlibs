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
import jlibs.nio.TCPConnection;
import jlibs.nio.TCPEndpoint;
import jlibs.nio.http.msg.*;
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

    /*-------------------------------------------------[ Attributes ]---------------------------------------------------*/

    public static Attribute<Integer> REQUEST_COUNT = new Attribute<Integer>(null, Request.class, false){
        @Override
        public Integer getValue(Exchange exchange){
            return exchange.getRequestCount();
        }

        @Override
        public String toString(){
            return "request_count";
        }
    };

    public static Attribute<String> SCHEME = new Attribute<String>(null, Request.class, false){
        @Override
        public String getValue(Exchange exchange){
            TCPEndpoint endpoint = exchange.getEndpoint();
            return endpoint==null ? null : endpoint.sslContext==null ? "http" : "https";
        }

        @Override
        public String toString(){
            return "scheme";
        }
    };

    public static Attribute<String> HOST = new Attribute<String>(null, Request.class, false){
        @Override
        public String getValue(Exchange exchange){
            TCPEndpoint endpoint = exchange.getEndpoint();
            return endpoint==null ? null : endpoint.host;
        }

        @Override
        public String toString(){
            return "host";
        }
    };

    public static Attribute<Integer> PORT = new Attribute<Integer>(null, Request.class, false){
        @Override
        public Integer getValue(Exchange exchange){
            TCPEndpoint endpoint = exchange.getEndpoint();
            return endpoint==null ? null : endpoint.port;
        }

        @Override
        public String toString(){
            return "port";
        }
    };

    public static Attribute<ConnectionStatus> CONNECTION_STATUS = new Attribute<ConnectionStatus>(null, Response.class, true){
        @Override
        public ConnectionStatus getValue(Exchange exchange){
            return exchange.connectionStatus;
        }

        @Override
        public String getValueAsString(Exchange exchange){
            ConnectionStatus conStatus = getValue(exchange);
            if(conStatus!=null){
                switch(conStatus){
                    case OPEN: return "+";
                    case CLOSED: return "-";
                    case ABORTED: return "X";
                }
            }
            return null;
        }

        @Override
        public String toString(){
            return "connection_status";
        }
    };

    public static Attribute<Method> REQUEST_METHOD = new Attribute<Method>(null, Request.class, false){
        @Override
        public Method getValue(Exchange exchange){
            Request request = exchange.getRequest();
            return request==null ? null : request.method;
        }

        @Override
        public String toString(){
            return "request_method";
        }
    };

    public static Attribute<String> REQUEST_LINE = new Attribute<String>(null, Request.class, false){
        @Override
        public String getValue(Exchange exchange){
            Request request = exchange.getRequest();
            return request==null ? null : request.method+" "+request.uri+' '+request.version;
        }

        @Override
        public String toString(){
            return "request_line";
        }
    };

    public static Attribute<String> QUERY_STRING = new Attribute<String>(null, Request.class, false){
        @Override
        public String getValue(Exchange exchange){
            Request request = exchange.getRequest();
            if(request==null || request.uri==null)
                return null;
            int question = request.uri.indexOf('?');
            return question==-1 ? "" : request.uri.substring(question+1);
        }

        @Override
        public String toString(){
            return "query_string";
        }
    };

    public static class RequestHeader extends Attribute<String>{
        public final AsciiString headerName;
        public RequestHeader(String headerName){
            this(AsciiString.valueOf(headerName));
        }

        public RequestHeader(AsciiString headerName){
            super(null, Request.class, false);
            this.headerName = headerName;
        }

        @Override
        public String getValue(Exchange exchange){
            Request request = exchange.getRequest();
            return request==null ? null : request.headers.value(headerName);
        }

        @Override
        public String toString(){
            return "request_header("+headerName+")";
        }
    }

    public static class RequestCookie extends Attribute<Cookie>{
        public final String cookieName;
        public RequestCookie(String cookieName){
            super(null, Request.class, false);
            this.cookieName = cookieName;
        }

        @Override
        public Cookie getValue(Exchange exchange){
            Request request = exchange.getRequest();
            return request==null ? null : request.getCookies().get(cookieName);
        }

        @Override
        public String getValueAsString(Exchange exchange){
            Cookie cookie = getValue(exchange);
            return cookie==null ? null : cookie.value;
        }

        @Override
        public String toString(){
            return "request_cookie("+cookieName+")";
        }
    }

    public static Attribute<Integer> RESPONSE_STATUS = new Attribute<Integer>(null, Response.class, false){
        @Override
        public Integer getValue(Exchange exchange){
            Response response = exchange.getResponse();
            return response==null || response.status==null ? null : response.status.code;
        }

        @Override
        public String toString(){
            return "response_status";
        }
    };

    public static class ResponseHeader extends Attribute<String>{
        public final AsciiString headerName;
        public ResponseHeader(String headerName){
            this(AsciiString.valueOf(headerName));
        }

        public ResponseHeader(AsciiString headerName){
            super(null, Response.class, false);
            this.headerName = headerName;
        }

        @Override
        public String getValue(Exchange exchange){
            Response response = exchange.getResponse();
            return response==null ? null : response.headers.value(headerName);
        }

        @Override
        public String toString(){
            return "response_header("+headerName+")";
        }
    }
}
