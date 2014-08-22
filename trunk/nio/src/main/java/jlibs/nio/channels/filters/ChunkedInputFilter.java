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

package jlibs.nio.channels.filters;

import jlibs.nio.Reactor;
import jlibs.nio.channels.impl.filters.AbstractInputFilterChannel;
import jlibs.nio.util.Line;
import jlibs.nio.util.NIOUtil;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ChunkedInputFilter extends AbstractInputFilterChannel{
    private static final int STATE_CHUNK_BEGIN = 0;
    private static final int STATE_READ_EOL = 1;
    private static final int STATE_CHUNK_CONTENT = 2;
    private static final int STATE_CHUNK_END = 3;
    private static final int STATE_LAST_CHUNK_END = 4;
    private static final int STATE_TRAILER = 5;
    private static final int STATE_FINISHED = 6;

    private static final int HEX_DIGITS[] = new int['f'+1];
    static{
        for(int i='0'; i<='9'; ++i)
            HEX_DIGITS[i] = i-'0';
        for(int i='a'; i<='f'; ++i)
            HEX_DIGITS[i] = i-'a'+10;
        for(int i='A'; i<='F'; ++i)
            HEX_DIGITS[i] = i-'A'+10;
    }

    private int state = STATE_CHUNK_BEGIN;
    private ByteBuffer buffer = Reactor.current().bufferPool.borrow(18);
    private int chunkLength = 0;
    private Line line;

    public ChunkedInputFilter(){
        buffer.flip();
    }

    private Line.Consumer consumer;
    public void setLineConsumer(Line.Consumer consumer){
        this.consumer = consumer;
    }

    @Override
    public long available(){
        return chunkLength<0 ? 0 : chunkLength;
    }

    @Override
    protected int _read(ByteBuffer dst) throws IOException{
        while(true){
            switch(state){
                case STATE_CHUNK_BEGIN:
                    do{
                        if(!buffer.hasRemaining() && !fillBuffer())
                            return 0;
                        do{
                            byte b = buffer.get();
                            if((b>='0' && b<='9') || (b>='a' && b<='f') || (b>='A' && b<='F')){
                                chunkLength <<= 4;
                                chunkLength += HEX_DIGITS[b];
                            }else{
                                if(b=='\n')
                                    state = chunkLength==0 ? STATE_LAST_CHUNK_END : STATE_CHUNK_CONTENT;
                                else
                                    state = STATE_READ_EOL;
                                break;
                            }
                        }while(buffer.hasRemaining());
                    }while(state==STATE_CHUNK_BEGIN);
                    break;
                case STATE_READ_EOL:
                    do{
                        if(!buffer.hasRemaining() && !fillBuffer())
                            return 0;
                        do{
                            if(buffer.get()=='\n'){
                                state = chunkLength==0 ? STATE_LAST_CHUNK_END : STATE_CHUNK_CONTENT;
                                break;
                            }
                        }while(buffer.hasRemaining());
                    }while(state==STATE_READ_EOL);
                    break;
                case STATE_CHUNK_CONTENT:
                    int pos = dst.position();
                    if(buffer.hasRemaining()){
                        int min = Math.min(chunkLength, Math.min(buffer.remaining(), dst.remaining()));
                        int _limit = buffer.limit();
                        buffer.limit(buffer.position()+min);
                        dst.put(buffer);
                        buffer.limit(_limit);
                        chunkLength -= min;
                    }
                    if(chunkLength>0 && dst.hasRemaining()){
                        int _limit = dst.limit();
                        dst.limit(dst.position()+Math.min(chunkLength, dst.remaining()));
                        int peerRead;
                        try{
                            peerRead = peerInput.read(dst);
                        }finally{
                            dst.limit(_limit);
                        }
                        if(peerRead==-1)
                            throw new EOFException(chunkLength+" more bytes expected");
                        chunkLength -= peerRead;
                    }
                    if(chunkLength==0)
                        state = STATE_CHUNK_END;
                    return dst.position()-pos;
                case STATE_CHUNK_END:
                    do{
                        if(!buffer.hasRemaining() && !fillBuffer())
                            return 0;
                        do{
                            if(buffer.get()=='\n'){
                                state = STATE_CHUNK_BEGIN;
                                break;
                            }
                        }while(buffer.hasRemaining());
                    }while(state==STATE_CHUNK_END);
                    break;
                case STATE_LAST_CHUNK_END:
                    while(buffer.remaining()<2){
                        if(!fillBuffer())
                            return 0;
                    }
                    if(buffer.get()=='\r'){
                        buffer.get(); // '\n'
                        unreadBuffer();
                        state = STATE_FINISHED;
                        return -1;
                    }else{
                        buffer.position(buffer.position()-1);
                        state = STATE_TRAILER;
                    }
                case STATE_TRAILER:
                    if(line==null)
                        line = new Line();
                    while(true){
                        if(line.parse(buffer, consumer)){
                            unreadBuffer();
                            state = STATE_FINISHED;
                            return -1;
                        }
                        if(!fillBuffer())
                            return 0;
                    }
                case STATE_FINISHED:
                    return -1;
            }
        }
    }

    private boolean fillBuffer() throws IOException{
        NIOUtil.compact(buffer);
        int read;
        try{
            read = peerInput.read(buffer);
        }finally{
            buffer.flip();
        }
        if(read==-1)
            throw new EOFException("unexpected end of stream");
        return read!=0;
    }

    private void unreadBuffer(){
        if(buffer.hasRemaining())
            peerInput.unread(buffer);
        else
            Reactor.current().bufferPool.returnBack(buffer);
        buffer = null;
    }

    @Override
    protected boolean isReadReady(){
        return state==STATE_FINISHED ||
                (state==STATE_CHUNK_CONTENT && buffer.hasRemaining()) ||
                (state==STATE_CHUNK_END && buffer.remaining()>=2);
    }

    @Override
    public void dispose(){
        if(buffer!=null)
            Reactor.current().bufferPool.returnBack(buffer);
    }
}
