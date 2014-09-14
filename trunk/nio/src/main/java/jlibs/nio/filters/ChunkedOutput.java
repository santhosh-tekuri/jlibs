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

package jlibs.nio.filters;

import jlibs.nio.Output;
import jlibs.nio.OutputFilter;
import jlibs.nio.Reactor;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ChunkedOutput extends OutputFilter{
    private static final int MAX_LEN = Long.toString(Long.MAX_VALUE, 16).length()+2;
    private static final byte CHUNK_END[] = { (byte)'\r', (byte)'\n' };
    private static final byte LAST_CHUNK[] = { (byte)'0', (byte)'\r', (byte)'\n', (byte)'\r', (byte)'\n' };

    private ByteBuffer chunkBegin;
    private long chunkLength;
    private ByteBuffer chunkEnd;
    private ByteBuffer buffers[] = new ByteBuffer[4];

    public ChunkedOutput(Output peer){
        super(peer);
        chunkBegin = Reactor.current().allocator.allocate(MAX_LEN);
        chunkEnd = ByteBuffer.wrap(CHUNK_END);
        buffers = new ByteBuffer[]{ chunkBegin, null, chunkEnd, ByteBuffer.wrap(LAST_CHUNK)};
        chunkEnd.position(chunkEnd.limit());
    }

    private static final byte digits[] = {
            '0' , '1' , '2' , '3' , '4' , '5' ,
            '6' , '7' , '8' , '9' , 'a' , 'b' ,
            'c' , 'd' , 'e' , 'f'
    };
    private static final int shift = 4;
    private static final int mask = (1<<shift)-1;
    public void startChunk(long length){
        if(length>0 && isOpen() && chunkLength==0 && !chunkEnd.hasRemaining()){
            chunkLength = length;
            chunkBegin.clear();

            // convert long to hex: borrowed from Long.toHexString(long)
            final int mag = Long.SIZE - Long.numberOfLeadingZeros(length);
            final int chars = Math.max(((mag + (shift-1))/shift), 1);
            int charPos = chars;
            do{
                chunkBegin.put(--charPos, digits[((int)length)&mask]);
                length >>>= shift;
            }while(length!=0 && charPos>0);
            chunkBegin.position(chars);

            chunkBegin.put((byte)'\r');
            chunkBegin.put((byte)'\n');
            chunkBegin.flip();
            chunkEnd.clear();
        }
    }

    private boolean canUserWrite() throws IOException{
        ensureOpen();
        if(chunkLength==0){
            while(chunkEnd.hasRemaining()){
                if(peer.write(chunkEnd)==0)
                    return false;
            }
        }
        return true;
    }

    @Override
    public int write(ByteBuffer src) throws IOException{
        if(!canUserWrite() || !src.hasRemaining())
            return 0;

        int pos = src.position();
        if(chunkLength==0)
            startChunk(src.remaining());

        assert chunkLength!=0;
        int userLimit = src.limit();
        int min = (int)Math.min(chunkLength, src.remaining());
        src.limit(min);

        buffers[1] = src;
        int offset = chunkBegin.hasRemaining() ? 0 : 1;
        int length = chunkBegin.hasRemaining() ? 2 : 1;
        if(min==chunkLength)
            ++length;
        int _pos = src.position();
        try{
            while(length>0){
                if(peer.write(buffers, offset, length)==0)
                    break;
                while(!buffers[offset].hasRemaining()){
                    ++offset;
                    --length;
                }
            }
        }finally{
            src.limit(userLimit);
            buffers[1] = null;
        }
        chunkLength -= src.position()-_pos;
        return src.position()-pos;
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException{
        if(length==0)
            return write(srcs[offset]);
        if(!canUserWrite())
            return 0;

        if(chunkLength==0){
            long remaining = 0;
            for(int i=0; i<length; i++)
                remaining += srcs[offset+i].remaining();
            startChunk(remaining);
            ByteBuffer buffers[] = new ByteBuffer[length+2];
            buffers[0] = chunkBegin;
            System.arraycopy(srcs, offset, buffers, 1, length);
            buffers[buffers.length-1] = chunkEnd;
            int chunkBeginRemaining = chunkBegin.remaining();
            long wrote = peer.write(buffers, 0, buffers.length) - chunkBeginRemaining;
            return wrote<0 ? 0 : wrote;
        }else{
            int len = 0;
            ByteBuffer candidate = null;
            int candidateLimit = 0;

            long remaining = 0;
            for(; len<length; len++){
                remaining += srcs[offset+len].remaining();
                if(remaining==0)
                    break;
                else if(remaining>chunkLength){
                    candidate = srcs[offset+len];
                    candidateLimit = candidate.limit();
                    candidate.limit((int)(remaining-chunkLength));
                    break;
                }
            }
            try{
                return peer.write(srcs, 0, len);
            }finally{
                if(candidate!=null)
                    candidate.limit(candidateLimit);
            }
        }
    }

    @Override
    protected boolean _flush() throws IOException{
        int offset, length;
        if(chunkBegin.hasRemaining()){
            offset = 0;
            length = 1;
        }else if(chunkEnd.hasRemaining()){
            offset = 2;
            length = isOpen() ? 1 : 2;
        }else if(!isOpen()){
            offset = 3;
            length = 1;
        }else
            return peer.flush();

        while(length>0){
            if(peer.write(buffers, offset, length)==0)
                return false;
            if(!buffers[offset].hasRemaining()){
                ++offset;
                --length;
            }
        }
        return true;
    }

    @Override
    protected void _close() throws ChunkException{
        if(chunkLength!=0)
            throw new ChunkException(chunkLength+" more bytes needs to be written");
    }
}
