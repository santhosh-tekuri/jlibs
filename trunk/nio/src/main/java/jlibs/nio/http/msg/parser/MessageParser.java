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
import jlibs.nio.http.msg.Version;

import java.nio.ByteBuffer;

import static jlibs.nio.http.util.USAscii.DIGIT;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class MessageParser extends HeadersParser{
    private static final byte VERSION_PREFIX[] = { 'H', 'T', 'T', 'P', '/' };

    private static final int STATE_PREFIX      = 0;
    private static final int STATE_MAJOR_BEGIN = 1;
    private static final int STATE_MAJOR       = 2;
    private static final int STATE_MINOR_BEGIN = 3;
    private static final int STATE_MINOR       = 4;

    private int vstate = STATE_PREFIX;
    private int vi;
    private int major, minor;
    protected final boolean parseVersion(ByteBuffer buffer){
        char ch;
        switch(vstate){
            case STATE_PREFIX:
                while(buffer.hasRemaining()){
                    if(buffer.get()!=VERSION_PREFIX[vi])
                        throw message.badMessage("Bad Version");
                    if(++vi==5){
                        if(buffer.remaining()>3){
                            int bpos = buffer.position();
                            if(!DIGIT[buffer.get(bpos+3)]){
                                if(buffer.get(bpos)=='1' && buffer.get(bpos+1)=='.'){
                                    char t = (char)buffer.get(bpos+2);
                                    if(t=='0'){
                                        message.version = Version.HTTP_1_0;
                                        buffer.position(bpos+3);
                                        return true;
                                    }else if(t=='1'){
                                        message.version = Version.HTTP_1_1;
                                        buffer.position(bpos+3);
                                        return true;
                                    }
                                }
                            }
                        }
                        vstate = STATE_MAJOR_BEGIN;
                        break;
                    }
                }
                if(!buffer.hasRemaining())
                    return false;
            case STATE_MAJOR_BEGIN:
                ch = (char)buffer.get();
                if(!DIGIT[ch])
                    throw message.badMessage("Bad Major Version");
                major = ch-'0';
                vstate = STATE_MAJOR;
                if(!buffer.hasRemaining())
                    return false;
            case STATE_MAJOR:
                while(buffer.hasRemaining()){
                    ch = (char)buffer.get();
                    if(ch=='.'){
                        vstate = STATE_MINOR_BEGIN;
                        break;
                    }
                    if(!DIGIT[ch])
                        throw message.badMessage("Bad Major Version");
                    major = major*10+(ch-'0');
                }
                if(!buffer.hasRemaining())
                    return false;
            case STATE_MINOR_BEGIN:
                ch = (char)buffer.get();
                if(!DIGIT[ch])
                    throw message.badMessage("Bad Minor Version");
                minor = ch-'0';
                vstate = STATE_MINOR;
            case STATE_MINOR:
                while(buffer.hasRemaining()){
                    ch = (char)buffer.get();
                    if(DIGIT[ch])
                        minor = minor*10+(ch-'0');
                    else{
                        buffer.position(buffer.position()-1);
                        message.version = Version.valueOf(major, minor);
                        return true;
                    }
                }
                return false;
        }
        return false;
    }

    protected Message message;
    public long consumed;
    public <T extends Message> void reset(T message){
        reset(message.headers, message.badMessageStatus());
        this.message = message;
        vstate = STATE_PREFIX;
        vi = 0;
        major = 0;
        minor = 0;
        consumed = 0;
    }
}
