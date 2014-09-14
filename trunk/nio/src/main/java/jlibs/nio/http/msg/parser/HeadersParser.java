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

import jlibs.nio.http.msg.Headers;
import jlibs.nio.http.msg.Message;
import jlibs.nio.http.msg.Status;
import jlibs.nio.http.msg.AsciiString;
import jlibs.nio.util.Parser;

import java.nio.ByteBuffer;

import static jlibs.nio.http.util.USAscii.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HeadersParser implements Parser{
    protected final StringBuilder builder = new StringBuilder();

    private static final int LINE_BEGIN  = 0;
    private static final int NAME        = 1;
    private static final int VALUE_BEGIN = 2;
    private static final int VALUE       = 3;

    private int state = LINE_BEGIN;
    private AsciiString name;

    @Override
    public boolean parse(ByteBuffer buffer, boolean eof){
        char ch;
        while(buffer.hasRemaining()){
            switch(state){
                case LINE_BEGIN:
                    ch = (char)buffer.get();
                    if(ch==COLON)
                        throw errorStatus.with("Empty Header Name");

                    if(ch==CR){
                        if(buffer.hasRemaining()){
                            if(buffer.get()!=LF)
                                throw errorStatus.with("Bad EOL");
                            if(name!=null)
                                addHeader();
                            return true;
                        }else{
                            buffer.position(buffer.position()-1);
                            return false;
                        }
                    }else if(ch==LF){
                        if(name!=null)
                            addHeader();
                        return true;
                    }

                    if(name!=null){
                        if(WS[ch]){
                            builder.append(SP);
                            state = VALUE;
                            if(buffer.hasRemaining())
                                break;
                            else
                                return false;
                        }else
                            addHeader();
                    }
                    builder.append(ch);
                    state = NAME;
                    if(!buffer.hasRemaining())
                        return false;
                case NAME:
                    while(buffer.hasRemaining()){
                        ch = (char)buffer.get();
                        if(ch==COLON){
                            name = AsciiString.valueOf(builder);
                            builder.setLength(0);
                            state = VALUE_BEGIN;
                            break;
                        }else{
                            if(!TOKEN[ch])
                                throw errorStatus.with("Bad Header Name");
                            builder.append(ch);
                        }
                    }
                    if(!buffer.hasRemaining())
                        return false;
                case VALUE_BEGIN:
                    while(buffer.hasRemaining()){
                        if(!WS[buffer.get()]){
                            buffer.position(buffer.position()-1);
                            state = VALUE;
                            break;
                        }
                    }
                    if(!buffer.hasRemaining())
                        return false;
                case VALUE:
                    while(buffer.hasRemaining()){
                        ch = (char)buffer.get();
                        if(ch==CR){
                            if(buffer.hasRemaining()){
                                if(buffer.get()!=LF)
                                    throw errorStatus.with("Bad EOL");
                                state = LINE_BEGIN;
                                break;
                            }else{
                                buffer.position(buffer.position()-1);
                                return false;
                            }
                        }else if(ch==LF){
                            state = LINE_BEGIN;
                            break;
                        }else
                            builder.append(ch);
                    }
                    if(!buffer.hasRemaining())
                        return false;
            }
        }
        return false;
    }

    private void addHeader(){
        int end = builder.length();
        while(end>0 && WS[builder.charAt(end-1)])
            --end;
        if(headers==null)
            headers = message.trailers = new Headers();
        headers.add(name, builder.substring(0, end));
        name = null;
        builder.setLength(0);
    }

    private Status errorStatus;
    private Headers headers;
    private Message message;
    public void reset(Headers headers, Status errorStatus){
        this.headers = headers;
        this.errorStatus = errorStatus;
        builder.setLength(0);
        name = null;
        state = LINE_BEGIN;
    }

    public void resetForTrailers(Message message){
        reset(message.trailers, message.badMessageStatus());
        this.message = message;
    }

    public Headers getHeaders(){
        return headers;
    }
}
