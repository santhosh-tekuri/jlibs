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
import jlibs.nio.http.msg.Response;
import jlibs.nio.http.msg.Status;

import java.nio.ByteBuffer;

import static jlibs.nio.http.util.USAscii.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ResponseParser extends MessageParser{
    private static final int VERSION = 0;
    private static final int STATUS  = 1;
    private static final int REASON  = 2;
    private static final int HEADERS = 3;

    private int step = VERSION;
    private int statusCode;
    private int digitCount;

    @Override
    public boolean parse(ByteBuffer buffer, boolean eof){
        char ch;
        switch(step){
            case VERSION:
                if(parseVersion(buffer)){
                    ch = (char)buffer.get();
                    if(ch!=SP)
                        throw Status.BAD_GATEWAY.with("Bad Version");
                    step = STATUS;
                    if(!buffer.hasRemaining())
                        return false;
                }
            case STATUS:
                while(buffer.hasRemaining()){
                    ch = (char)buffer.get();
                    if(digitCount==3){
                        if(ch!=SP)
                            throw Status.BAD_GATEWAY.with("Bad Status");
                        step = REASON;
                        if(!buffer.hasRemaining())
                            return false;
                        break;
                    }
                    if(!DIGIT[ch])
                        throw Status.BAD_GATEWAY.with("Bad Status");
                    ++digitCount;
                    statusCode = statusCode*10 + (ch-'0');
                }
            case REASON:
                while(buffer.hasRemaining()){
                    ch = (char)buffer.get();
                    if(ch==CR){
                        if(buffer.hasRemaining()){
                            ch = (char)buffer.get();
                            if(ch!=LF)
                                throw Status.BAD_GATEWAY.with("Bad EOL");
                            response.status = Status.valueOf(statusCode, builder);
                            builder.setLength(0);
                            step = HEADERS;
                            break;
                        }
                    }else if(ch==LF){
                        response.status = Status.valueOf(statusCode, builder);
                        builder.setLength(0);
                        step = HEADERS;
                        break;
                    }else
                        builder.append(ch);
                }
            case HEADERS:
                return super.parse(buffer, eof);
        }
        return false;
    }

    private Response response;
    public void reset(Message response){
        super.reset(response);
        this.response = (Response)response;
        step = VERSION;
        statusCode = 0;
        digitCount = 0;
    }
}
