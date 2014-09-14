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

import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;

/**
 * @author Santhosh Kumar Tekuri
 */
public class GZIPOutput extends DeflaterOutput{
    public GZIPOutput(Output peer){
        super(new Deflater(Deflater.DEFAULT_COMPRESSION, true), peer);
    }

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

    @Override
    protected void addHeader(ByteBuffer buffer){
        buffer.put(HEADER_BYTES);
    }

    private CRC32 crc = new CRC32();
    @Override
    protected void setInput(byte[] bytes, int offset, int length){
        super.setInput(bytes, offset, length);
        crc.update(bytes, offset, length);
    }

    @Override
    protected void addTrailer(ByteBuffer buffer){
        int checksum = (int) crc.getValue();
        buffer.put((byte)(checksum&0xFF));
        buffer.put((byte)((checksum>>8)&0xFF));
        buffer.put((byte)((checksum>>16)&0xFF));
        buffer.put((byte)((checksum>>24)&0xFF));

        int total = deflater.getTotalIn();
        buffer.put((byte)(total&0xFF));
        buffer.put((byte)((total>>8)&0xFF));
        buffer.put((byte)((total>>16)&0xFF));
        buffer.put((byte)((total>>24)&0xFF));
    }

}
