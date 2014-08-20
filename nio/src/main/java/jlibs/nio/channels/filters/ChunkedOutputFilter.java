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
import jlibs.nio.channels.impl.filters.AbstractOutputFilterChannel;

import java.io.IOException;
import java.nio.ByteBuffer;

import static java.nio.channels.SelectionKey.OP_WRITE;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ChunkedOutputFilter extends AbstractOutputFilterChannel{
    private static final int MAX_LEN = Long.toString(Long.MAX_VALUE, 16).length()+2;
    private static final byte CHUNK_END[] = { (byte)'\r', (byte)'\n' };
    private static final byte LAST_CHUNK[] = { (byte)'0', (byte)'\r', (byte)'\n', (byte)'\r', (byte)'\n' };

    private ByteBuffer chunkBegin;
    private long chunkLength;
    private ByteBuffer chunkEnd;
    private ByteBuffer buffers[] = new ByteBuffer[4];

    public ChunkedOutputFilter(){
        chunkBegin = Reactor.current().bufferPool.borrow(MAX_LEN);
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

    @Override
    protected int _write(ByteBuffer src) throws IOException{
        int pos = src.position();

        while(src.hasRemaining()){
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
                    if(peerOutput.write(buffers, offset, length)==0){
                        if(offset!=1)
                            selfInterestOps |= OP_WRITE;
                        break;
                    }
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
        }

        return src.position()-pos;
    }

    @Override
    protected void _process() throws IOException{
        int offset, length;
        if(chunkBegin.hasRemaining()){
            offset = 0;
            length = 1;
        }else if(chunkEnd.hasRemaining()){
            offset = 2;
            length = isOpen() ? 1 : 2;
        }else{
            offset = 3;
            length = 1;
        }
        while(length>0){
            if(peerOutput.write(buffers, offset, length)==0){
                selfInterestOps |= OP_WRITE;
                return;
            }
            if(!buffers[offset].hasRemaining()){
                ++offset;
                --length;
            }
        }
    }

    @Override
    protected void _close() throws IOException{
        if(chunkLength!=0)
            throw new ChunkException(chunkLength+" more bytes needs to be written");
        _process();
    }
}
