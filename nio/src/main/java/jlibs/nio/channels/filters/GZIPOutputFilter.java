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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;

import static java.nio.channels.SelectionKey.OP_WRITE;

/**
 * @author Santhosh Kumar Tekuri
 */
public class GZIPOutputFilter extends DeflaterOutputFilter{
    private static final byte[] HEADER_BYTES = {
            (byte) GZIPInputStream.GZIP_MAGIC,       // Magic number (short)
            (byte)(GZIPInputStream.GZIP_MAGIC >> 8), // Magic number (short)
            Deflater.DEFLATED,                       // Compression method (CM)
            0,                                       // Flags (FLG)
            0,                                       // Modification time MTIME (int)
            0,                                       // Modification time MTIME (int)
            0,                                       // Modification time MTIME (int)
            0,                                       // Modification time MTIME (int)
            0,                                       // Extra flags (XFLG)
            0                                        // Operating system (OS)
    };

    private static final int STATE_HEADER = 0;
    private static final int STATE_CONTENT = 1;
    private static final int STATE_TRAILER = 2;
    private static final int STATE_FINISHED = 3;

    public GZIPOutputFilter(){
        super(new Deflater(Deflater.DEFAULT_COMPRESSION, true));
        selfInterestOps |= OP_WRITE;
    }

    private int state = STATE_HEADER;
    private CRC32 crc = new CRC32();
    private ByteBuffer headerBuffer = ByteBuffer.wrap(HEADER_BYTES);
    private ByteBuffer trailerBuffer;

    @Override
    protected int _write(ByteBuffer src) throws IOException{
        assert state==STATE_CONTENT;
        int wrote = super._write(src);
        if(wrote>0)
            crc.update(src.array(), src.arrayOffset()+src.position()-wrote, wrote);
        return wrote;
    }

    @Override
    protected void _process() throws IOException{
        if(state==STATE_HEADER){
            do{
                if(peerOutput.write(headerBuffer)==0){
                    selfInterestOps |= OP_WRITE;
                    return;
                }
            }while(headerBuffer.hasRemaining());
            assert !headerBuffer.hasRemaining();
            state = STATE_CONTENT;
            return;
        }
        if(state==STATE_CONTENT){
            super._process();
            if(!isOpen() && selfInterestOps==0)
                state = STATE_TRAILER;
            else
                return;
        }

        assert state==STATE_TRAILER;
        do{
            if(peerOutput.write(trailerBuffer)==0){
                selfInterestOps |= OP_WRITE;
                return;
            }
        }while(trailerBuffer.hasRemaining());
        assert !trailerBuffer.hasRemaining();
        state = STATE_FINISHED;
    }

    @Override
    protected void _close() throws IOException{
        trailerBuffer = ByteBuffer.allocate(8);
        writeInt((int)crc.getValue()); // CRC-32 of uncompr. data
        writeInt(deflater.getTotalIn()); // Number of uncompr. bytes
        trailerBuffer.flip();
        super._close();
    }

    /*
     * Writes integer in Intel byte order to a byte array, starting at a
     * given offset.
     */
    private void writeInt(int i) throws IOException{
        writeShort(i & 0xffff);
        writeShort((i>>16) & 0xffff);
    }

    /*
     * Writes short integer in Intel byte order to a byte array, starting
     * at a given offset
     */
    private void writeShort(int s) throws IOException{
        trailerBuffer.put((byte)(s & 0xff));
        trailerBuffer.put((byte)((s>>8) & 0xff));
    }
}
