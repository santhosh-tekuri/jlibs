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

import jlibs.nio.Reactor;
import jlibs.nio.http.msg.FormPayload;
import jlibs.nio.util.BytesDecoder;
import jlibs.nio.util.Parser;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static jlibs.nio.http.msg.parser.FormParser.State.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public class FormParser implements Parser{
    public final FormPayload payload = new FormPayload();
    private Charset charset;

    public FormParser(Charset charset){
        this.charset = charset;
    }

    private String name;

    enum State{ NORMAL, HEX1, HEX2 }
    private State state = NORMAL;
    private int hex;

    @Override
    public boolean parse(ByteBuffer buffer, boolean eof) throws IOException{
        char ch;
        while(buffer.hasRemaining()){
            switch(state){
                case NORMAL:
                    while(buffer.hasRemaining()){
                        ch = (char)buffer.get();
                        if(ch=='+')
                            builder().append(' ');
                        else if(ch=='='){
                            if(name==null){
                                name = builder().toString();
                                builder.setLength(0);
                            }else
                                builder().append('=');
                        }else if(ch=='&')
                            addParam();
                        else if(ch=='%'){
                            state = HEX1;
                            break;
                        }else
                            builder().append(ch);
                    }
                    if(!buffer.hasRemaining())
                        break;
                case HEX1:
                    ch = (char)buffer.get();
                    if(ch>='0' && ch<='9')
                        hex = ch-'0';
                    else if(ch>='a' && ch<='f')
                        hex = 10 + (ch-'a');
                    else if(ch>='A' && ch<='F')
                        hex = 10 + (ch-'A');
                    else
                        throw new IOException("Bad Hex Char");
                    hex *= 16;
                    state = HEX2;
                    if(!buffer.hasRemaining())
                        break;
                case HEX2:
                    ch = (char)buffer.get();
                    if(ch>='0' && ch<='9')
                        hex += ch-'0';
                    else if(ch>='a' && ch<='f')
                        hex += 10 + (ch-'a');
                    else if(ch>='A' && ch<='F')
                        hex += 10 + (ch-'A');
                    else
                        throw new IOException("Bad Hex Char");
                    if(hex<0)
                        throw new IOException("Bad Hex Code");
                    if(hexBuffer==null){
                        hexBuffer = Reactor.current().allocator.allocate();
                        decoder = new BytesDecoder(builder, charset.newDecoder(), 100);
                    }else if(!hexBuffer.hasRemaining()){
                        hexBuffer.flip();
                        decoder.write(hexBuffer, false);
                        hexBuffer.compact();
                    }
                    hexBuffer.put((byte)hex);
                    state = NORMAL;
            }
        }

        if(eof){
            if(state!=NORMAL)
                throw new EOFException("Malformed Form Payload");
            addParam();
        }
        return eof;
    }

    private ByteBuffer hexBuffer;
    private BytesDecoder decoder;
    private StringBuilder builder = new StringBuilder();
    private StringBuilder builder() throws IOException{
        if(hexBuffer!=null && hexBuffer.position()>0){
            hexBuffer.flip();
            decoder.write(hexBuffer, true);
            hexBuffer.clear();
        }
        return builder;
    }

    private void addParam() throws IOException{
        String value = builder().toString();
        builder.setLength(0);
        if(name==null)
            payload.addParam(value, "");
        else{
            payload.addParam(name, value);
            name = null;
        }
    }

    @Override
    public void cleanup(){
        if(hexBuffer!=null){
            Reactor.current().allocator.free(hexBuffer);
            hexBuffer = null;
        }
    }
}
