/**
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

package jlibs.core.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;

/**
 * @author Santhosh Kumar T
 */
public class GZIPOutputChannel extends DeflaterOutputChannel{
    public GZIPOutputChannel(OutputChannel delegate){
        super(delegate, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
    }

    private final static byte[] HEADER_BYTES = {
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


    private CRC32 crc = new CRC32();

    @Override
    protected int onWrite(ByteBuffer src) throws IOException{
        int wrote = super.onWrite(src);
        crc.update(src.array(), src.arrayOffset() + src.position() - wrote, wrote);
        return wrote;
    }

    private ByteBuffer header = ByteBuffer.wrap(HEADER_BYTES);
    private ByteBuffer trailer;

    @Override
    protected void doWritePending() throws IOException{
        if(header!=null){
            delegate.write(header);
            if(header.hasRemaining())
                return;
            header = null;
        }
        super.doWritePending();
        if(trailer!=null && super.selfStatus()==Status.COMPLETED){
            delegate.write(trailer);
            if(!trailer.hasRemaining())
                trailer = null;
        }
    }

    @Override
    protected Status selfStatus(){
        if(header!=null && header.position()==0)
            return super.selfStatus();
        else if(trailer!=null)
            return Status.NEEDS_OUTPUT;
        else
            return super.selfStatus();
    }

    @Override
    protected void doClose() throws IOException{
        trailer = ByteBuffer.allocate(8);
        writeInt((int)crc.getValue()); // CRC-32 of uncompr. data
        writeInt(deflater.getTotalIn()); // Number of uncompr. bytes
        trailer.flip();
        super.doClose();
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
        trailer.put((byte)(s & 0xff));
        trailer.put((byte)((s>>8) & 0xff));
    }
}
