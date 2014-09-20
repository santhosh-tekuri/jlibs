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

import jlibs.core.lang.Util;
import jlibs.nio.Debugger;
import jlibs.nio.Reactor;
import jlibs.nio.filters.BufferInput;
import jlibs.nio.filters.ChunkedInput;
import jlibs.nio.filters.FixedLengthInput;
import jlibs.nio.http.msg.*;
import jlibs.nio.http.msg.parser.HeadersParser;
import jlibs.nio.http.msg.parser.MessageParser;
import jlibs.nio.http.util.Encoding;
import jlibs.nio.listeners.Task;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static java.nio.channels.SelectionKey.OP_READ;
import static jlibs.nio.Debugger.HTTP;
import static jlibs.nio.Debugger.println;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ReadMessage extends Task{
    private final long maxHeadSize;
    private final MessageParser parser;
    protected ReadMessage(long maxHeadSize, MessageParser parser){
        super(OP_READ);
        this.maxHeadSize = maxHeadSize;
        this.parser = parser;
    }

    @Override
    protected boolean process(int readyOp) throws IOException{
        while(true){
            int read;
            try{
                read = in.read(buffer);
            }catch(IOException ex){
                if(consumed+buffer.position()==0 && message instanceof Request)
                    throw IGNORABLE_EOF_EXCEPTION;
                throw ex;
            }
            if(read==0){
                in.addReadInterest();
                return false;
            }
            if(read==-1){
                if(consumed==0 && message instanceof Request)
                    throw IGNORABLE_EOF_EXCEPTION;
                throw message.badMessage("Unexpected EOF");
            }
            buffer.flip();
            int pos = buffer.position();
            parser.consumed = consumed;
            boolean done = parser.parse(buffer, false);
            consumed += buffer.position()-pos;
            if(maxHeadSize>0 && (done ? consumed>maxHeadSize : consumed>=maxHeadSize)){
                if(message instanceof Request)
                    throw Status.REQUEST_HEADER_FIELDS_TOO_LARGE;
                else
                    throw message.badMessage("Response Header Fields Too Large");
            }
            if(done)
                break;
            else
                buffer.compact();
        }
        if(buffer.hasRemaining()){
            in = new BufferInput(in, buffer); // unread
            buffer = Reactor.current().allocator.allocate();
        }
        if(HTTP){
            println("readMessage(){");
            Debugger.println(message);
        }

        // init payload --------------------------------
        keepAlive = message.isKeepAlive();
        long contentLength = -1;
        List<Encoding> encodings = null;

        if(!emptyPayload){
            if(message instanceof Request){
                Request request = (Request)message;
                if(!request.method.requestPayloadAllowed)
                    emptyPayload = true;
            }else{
                Response response = (Response)message;
                if(response.status.payloadNotAllowed)
                    emptyPayload = true;
            }
        }

        if(!emptyPayload){
            if(message.isChunked()){
                HeadersParser trailersParser = new HeadersParser();
                trailersParser.resetForTrailers(message);
                in = new ChunkedInput(in, trailersParser);
            }else{
                Header clHeader = message.headers.get(Message.CONTENT_LENGTH);
                if(clHeader!=null){
                    if(clHeader.getValue().length()==0)
                        throw message.badMessage("Empty Content-Length");
                    if(clHeader.getValue().charAt(0)=='-')
                        throw message.badMessage("Negative Content-Length");
                    contentLength = Util.parseLong(clHeader.getValue());
                    if(contentLength==0)
                        emptyPayload = true;
                    else
                        in = new FixedLengthInput(in, contentLength);
                }else{
                    encodings = message.getContentEncodings();
                    if(keepAlive || !(message instanceof Response)){
                        if(!encodings.isEmpty())
                            in = encodings.remove(encodings.size()-1).wrap(in);
                        else if(message instanceof Request)
                            emptyPayload = true;
                        else
                            keepAlive = false;
                    }
                }
            }
        }
        if(!emptyPayload){
            if(encodings==null)
                encodings = message.getContentEncodings();
            message.setPayload(new SocketPayload(contentLength,
                    message.headers.value(Message.CONTENT_TYPE),
                    in, encodings));
        }

        if(HTTP){
            if(emptyPayload)
                println("payload = empty");
            else
                println("payload = "+in);
            println("}");
        }
        return true;
    }

    private Message message;
    private ByteBuffer buffer;
    private long consumed = 0;
    private boolean keepAlive;
    private boolean emptyPayload;
    public void reset(Message message, boolean emptyPayload){
        this.message = message;
        if(buffer==null)
            buffer = Reactor.current().allocator.allocate();
        else
            buffer.clear();
        consumed = 0;
        parser.reset(message);
        keepAlive = false;
        this.emptyPayload = emptyPayload;
    }

    public Message getMessage(){
        return message;
    }

    public long consumed(){
        return consumed;
    }

    public boolean keepAlive(){
        return keepAlive;
    }

    public void dispose(){
        if(buffer!=null){
            Reactor.current().allocator.free(buffer);
            buffer = null;
        }
    }

    @Override
    public String toString(){
        return "ReadMessage";
    }

    public static final EOFException IGNORABLE_EOF_EXCEPTION = new EOFException(){
        @Override
        public Throwable fillInStackTrace(){
            return this;
        }

        @Override
        public String toString(){
            return "IGNORABLE_EOF";
        }
    };
}
