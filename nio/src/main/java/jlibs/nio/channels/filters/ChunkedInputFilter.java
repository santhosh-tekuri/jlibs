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

import jlibs.core.io.IOUtil;
import jlibs.nio.Reactor;
import jlibs.nio.channels.impl.filters.AbstractInputFilterChannel;
import jlibs.nio.util.BytePattern;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ChunkedInputFilter extends AbstractInputFilterChannel{
    private static final int STATE_CHUNK_BEGIN = 0;
    private static final int STATE_CHUNK_CONTENT = 1;
    private static final int STATE_CHUNK_END = 2;
    private static final int STATE_TRAILER = 3;
    private static final int STATE_FINISHED = 4;

    private int state = STATE_CHUNK_BEGIN;
    private ByteBuffer buffer = Reactor.current().bufferPool.borrow(100);
    private int readPos = 0;
    private int chunkLength = -1;
    private BytePattern.Matcher matcher;

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
                        if(chunkLength==-1){
                            if(!fillBuffer())
                                return 0;
                        }else
                            chunkLength = -1;
                        int semicolon = -1;
                        byte array[] = buffer.array();
                        for(int i=readPos+2; i<buffer.position(); i++){
                            if(semicolon==-1 && array[i-2]==';')
                                semicolon = i-2;
                            if(array[i-1]=='\r' && array[i]=='\n'){
                                try{
                                    chunkLength = Integer.parseInt(new String(array, readPos, (semicolon==-1 ? i-1 : semicolon)-readPos, IOUtil.US_ASCII), 16);
                                }catch(NumberFormatException ex){
                                    throw new ChunkException("invalid chunk length", ex);
                                }
                                if(chunkLength<0)
                                    throw new ChunkException("negative chunk length");
                                readPos = i+1;
                                break;
                            }
                        }
                    }while(chunkLength<0);
                    if(chunkLength==0){
                        state = STATE_CHUNK_END;
                        break;
                    }else
                        state = STATE_CHUNK_CONTENT;
                case STATE_CHUNK_CONTENT:
                    int read = 0;
                    if(readPos!=buffer.position()){
                        read = Math.min(chunkLength, Math.min(buffer.position()-readPos, dst.remaining()));
                        dst.put(buffer.array(), readPos, read);
                        readPos += read;
                        chunkLength -= read;
                    }
                    if(chunkLength>0 && dst.hasRemaining()){
                        int userLimit = dst.limit();
                        dst.limit(dst.position()+Math.min(chunkLength, dst.remaining()));
                        int peerRead;
                        try{
                            peerRead = peerInput.read(dst);
                        }finally{
                            dst.limit(userLimit);
                        }
                        if(peerRead==-1)
                            throw new EOFException(chunkLength+" more bytes expected");
                        chunkLength -= peerRead;
                        read += peerRead;
                    }
                    if(chunkLength==0){
                        chunkLength = -2; // not last chunk
                        state = STATE_CHUNK_END;
                    }
                    return read;
                case STATE_CHUNK_END:
                    while(buffer.position()-readPos<2){
                        if(!fillBuffer())
                            return 0;
                    }
                    if(buffer.get(readPos)=='\r' && buffer.get(readPos+1)=='\n'){
                        readPos += 2;
                        if(chunkLength==0){
                            unreadBuffer();
                            state = STATE_FINISHED;
                            return -1;
                        }else{
                            state = STATE_CHUNK_BEGIN;
                            break;
                        }
                    }else{
                        if(chunkLength==0){
                            chunkLength = -2; // ready only if necessary
                            matcher = BytePattern.CRLFCRLF.new Matcher();
                            state = STATE_TRAILER;
                        }else
                            throw new ChunkException("chunk should end with '\\r\\n'");
                    }
                case STATE_TRAILER:
                    while(true){
                        int pos;
                        if(chunkLength==-1){
                            pos = buffer.position();
                            if(!fillBuffer())
                                return 0;
                        }else{
                            pos = readPos;
                            chunkLength = -1;
                        }

                        byte array[] = buffer.array();
                        while(pos<buffer.position()){
                            if(matcher.matches(array[pos])){
                                readPos = pos+1;
                                unreadBuffer();
                                state = STATE_FINISHED;
                                return -1;
                            }else
                                ++pos;
                        }
                    }
                case STATE_FINISHED:
                    return -1;
            }
        }
    }

    private boolean fillBuffer() throws IOException{
        if(readPos==buffer.position()){
            buffer.clear();
            readPos = 0;
        }else if(!buffer.hasRemaining()){
            if(readPos==0){
                int newCapacity = buffer.capacity()+100;
                buffer = ByteBuffer.wrap(Arrays.copyOf(buffer.array(), newCapacity), buffer.position(), newCapacity-buffer.capacity());
            }else{
                System.arraycopy(buffer.array(), readPos, buffer.array(), 0, buffer.position()-readPos);
                buffer.position(buffer.position()-readPos);
                readPos = 0;
            }
        }
        int read = peerInput.read(buffer);
        if(read==-1)
            throw new EOFException("unexpected end of stream");
        return read!=0;
    }

    private void unreadBuffer(){
        if(readPos!=buffer.position()){
            buffer.limit(buffer.position());
            buffer.position(readPos);
            peerInput.unread(buffer);
        }else{
            if(buffer.capacity()==100)
                Reactor.current().bufferPool.returnBack(buffer);
        }
    }

    @Override
    protected boolean isReadReady(){
        return state==STATE_FINISHED ||
                (state==STATE_CHUNK_CONTENT && readPos!=buffer.position()) ||
                (state==STATE_CHUNK_END && buffer.position()-readPos>=2);
    }
}
