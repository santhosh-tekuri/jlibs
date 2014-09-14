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

package jlibs.nio.http.msg.parser;

import jlibs.nio.http.msg.Message;
import jlibs.nio.http.msg.Method;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Status;

import java.nio.ByteBuffer;

import static jlibs.nio.http.util.USAscii.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public class RequestParser extends MessageParser{
    private static final int BEGIN   = 0;
    private static final int METHOD  = 1;
    private static final int URI = 2;
    private static final int VERSION = 3;
    private static final int EOL     = 4;
    private static final int HEADERS = 5;

    private final long maxURISize;
    public RequestParser(long maxURISize){
        this.maxURISize = maxURISize;
    }

    private int state = BEGIN;

    @Override
    public boolean parse(ByteBuffer buffer, boolean eof){
        int bpos = buffer.position();
        char ch;
        while(buffer.hasRemaining()){
            switch(state){
                case METHOD:
                    while(true){
                        if(!buffer.hasRemaining())
                            return false;
                        ch = (char)buffer.get();
                        if(ch==SP){
                            if(builder.length()==0)
                                throw Status.BAD_REQUEST.with("Method Missing");
                            request.method = Method.valueOf(builder);
                            builder.setLength(0);
                            beforeURI = consumed+buffer.position()-bpos;
                            state = URI;
                            break;
                        }else{
                            if(!TOKEN[ch])
                                throw Status.BAD_REQUEST.with("Bad Method");
                            builder.append(ch);
                        }
                    }
                    break;
                case BEGIN:
                    if(buffer.remaining()>3 && buffer.get(bpos)=='G' && buffer.get(bpos+1)=='E' && buffer.get(bpos+2)=='T' && buffer.get(bpos+3)==' '){
                        request.method = Method.GET;
                        buffer.position(buffer.position()+4);
                        beforeURI = consumed + buffer.position()-bpos;
                        state = URI;
                    }else{
                        state = METHOD;
                        break;
                    }
                case URI:
                    while(true){
                        if(!buffer.hasRemaining())
                            return false;
                        ch = (char)buffer.get();
                        if(ch==SP){
                            if(builder.length()==0)
                                throw Status.BAD_REQUEST.with("Path Missing");
                            request.uri = builder.toString();
                            builder.setLength(0);
                            if(maxURISize>0){
                                long uriSize = (consumed+buffer.position()-bpos)-beforeURI-1;
                                if(uriSize>maxURISize)
                                    throw Status.REQUEST_URI_TOO_LONG;
                            }
                            state = VERSION;
                            break;
                        }else
                            builder.append(ch);
                    }
                case VERSION:
                    if(parseVersion(buffer)){
                        state = EOL;
                        if(!buffer.hasRemaining())
                            return false;
                    }else
                        return false;
                case EOL:
                    ch = (char)buffer.get();
                    if(ch==CR){
                        if(buffer.hasRemaining())
                            ch = (char)buffer.get();
                        else{
                            buffer.position(buffer.position()-1);
                            return false;
                        }
                    }
                    if(ch!=LF)
                        throw Status.BAD_REQUEST.with("Bad Version");
                    state = HEADERS;
                    builder.setLength(0);
                case HEADERS:
                    return super.parse(buffer, eof);
            }
        }
        return false;
    }

    private long beforeURI;
    private Request request;
    public void reset(Message request){
        super.reset(request);
        this.request = (Request)request;
        state = BEGIN;
    }
}
