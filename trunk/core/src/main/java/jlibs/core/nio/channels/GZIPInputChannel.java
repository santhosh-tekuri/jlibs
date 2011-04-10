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

import jlibs.core.lang.ImpossibleException;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.*;

/**
 * @author Santhosh Kumar T
 */
public class GZIPInputChannel extends InflaterInputChannel{
    public GZIPInputChannel(InputChannel delegate){
        super(delegate, new Inflater(true));
    }

    private static final int STATE_GZIP_MAGIC = 0;
    private static final int STATE_COMPRESSION_METHOD = 1;
    private static final int STATE_FLAG = 2;
    private static final int STATE_MTIME_XFL_OS = 3;
    private static final int STATE_SKIP_EXTRA = 4;
    private static final int STATE_SKIP_FNAME = 5;
    private static final int STATE_SKIP_FCOMMENT = 6;
    private static final int STATE_SKIP_FHCRC = 7;
    private static final int STATE_CONTENT = 8;
    private static final int STATE_TRAILER = 9;
    private static final int STATE_FINISHED = 10;

    private int state = STATE_GZIP_MAGIC;
    private int from = 0;
    private int flag;
    private int extraLen = -1;

    @Override
    protected boolean activateInterest(){
        if(super.activateInterest())
            return true;
        else if(state==STATE_FINISHED)
            return false;
        else{
            assert state==STATE_TRAILER;
            return !canRead(UINT+UINT);
        }
    }

    @Override
    protected int doRead(ByteBuffer dst) throws IOException{
        int read = 0;
        if(state==STATE_CONTENT){
            read = super.doRead(dst);
            if(read==-1)
                state = STATE_TRAILER;
            else
                return read;
        }

        if(state==STATE_TRAILER){
            if(!canRead(UINT+UINT)){
                readBuffer.limit(UINT+UINT);
                if(delegate.read(readBuffer)==-1)
                    throw new EOFException();
            }
            if(canRead(UINT+UINT)){
                readUInt();
                if(readUInt()!=isize)
                    throw new ZipException("Corrupt GZIP trailer");
                if(from<readBuffer.position())
                    delegate.unread(readBuffer.array(), from, readBuffer.position()-from, false);
                readBuffer = null;
                state++;
            }else
                return read;
        }

        if(state==STATE_FINISHED)
            return read>0 ? read : -1;

        delegate.read(readBuffer);
        switch(state){
            case STATE_GZIP_MAGIC:
                if(canRead(USHORT)){
                    if(readUShort()!=GZIPInputStream.GZIP_MAGIC)
                        throw new ZipException("Not in GZIP format");
                    state++;
                }else{
                    if(delegate.isEOF())
                        throw new EOFException();
                    return 0;
                }
            case STATE_COMPRESSION_METHOD:
                if(canRead(UBYTE)){
                    if(readUByte()!=Deflater.DEFLATED)
                        throw new ZipException("Unsupported compression method");
                    state++;
                }else{
                    if(delegate.isEOF())
                        throw new EOFException();
                    return 0;
                }
            case STATE_FLAG:
                if(canRead(UBYTE)){
                    flag = readUByte();
                    state++;
                }else{
                    if(delegate.isEOF())
                        throw new EOFException();
                    return 0;
                }
            case STATE_MTIME_XFL_OS:
                if(canRead(6)){
                    from += 6;
                    state++;
                }else{
                    if(delegate.isEOF())
                        throw new EOFException();
                    return 0;
                }
            case STATE_SKIP_EXTRA:
                if((flag&FEXTRA)==FEXTRA){
                    if(extraLen==-1){
                        if(canRead(USHORT))
                            extraLen = readUShort();
                        else{
                            if(delegate.isEOF())
                                throw new EOFException();
                            return 0;
                        }
                    }
                    if(canRead(extraLen)){
                        from += extraLen;
                        state++;
                    }else{
                        if(delegate.isEOF())
                            throw new EOFException();
                        return 0;
                    }
                }else
                    state++;
            case STATE_SKIP_FNAME:
                if((flag&FNAME)==FNAME){
                    while(canRead(UBYTE)){
                        if(readUByte()==0){
                            state++;
                            break;
                        }
                    }
                    if(state==STATE_SKIP_FNAME){
                        if(delegate.isEOF())
                            throw new EOFException();
                        return 0;
                    }
                }else
                    state++;
            case STATE_SKIP_FCOMMENT:
                if((flag&FCOMMENT)==FCOMMENT){
                    while(canRead(UBYTE)){
                        if(readUByte()==0){
                            state++;
                            break;
                        }
                    }
                    if(state==STATE_SKIP_FCOMMENT){
                        if(delegate.isEOF())
                            throw new EOFException();
                        return 0;
                    }
                }else
                    state++;
            case STATE_SKIP_FHCRC:
                if((flag&FHCRC)==FHCRC){
                    if(canRead(USHORT)){
                        CRC32 crc = new CRC32();
                        crc.update(readBuffer.array(), 0, from);
                        int crcValue = (int)crc.getValue() & 0xffff;
                        int x = readUShort();
                        if(crcValue!=x)
                            throw new ZipException("Corrupt GZIP header");
                        state++;
                    }else{
                        if(delegate.isEOF())
                            throw new EOFException();
                        return 0;
                    }
                }else
                    state++;
            case STATE_CONTENT:
                if(from<readBuffer.position())
                    inflater.setInput(readBuffer.array(), from, readBuffer.position()-from);
                return super.doRead(dst);
        }
        throw new ImpossibleException();
    }

    private long isize;
    @Override
    protected void inflateFinished(){
        isize = inflater.getBytesWritten() & 0xffffffffL; // rfc1952; ISIZE is the input size modulo 2^32
        from = 0;
        if(inflater.getRemaining()>0){
            readBuffer.limit(readBuffer.position());
            readBuffer.position(readBuffer.position()-inflater.getRemaining());
            readBuffer.compact();
        }else
            readBuffer.clear();
    }

    private final static int FHCRC = 2;    // Header CRC
    private final static int FEXTRA = 4;    // Extra field
    private final static int FNAME = 8;    // File name
    private final static int FCOMMENT = 16;    // File comment

    private static final int UBYTE = 1;
    private static final int USHORT = UBYTE+UBYTE;
    private static final int UINT = USHORT+USHORT;
    private boolean canRead(int type){
        return readBuffer.position()-from>=type;
    }

    private int readUByte() throws IOException{
        int b = readBuffer.get(from++) & 0xff;
        if(b<-1 || b>255)
            throw new ZipException("read() returned value out of range -1..255: " + b);
        return b;
    }

    private int readUShort() throws IOException{
        int b = readUByte();
        return (readUByte()<<8)|b;
    }

    private long readUInt() throws IOException{
        long s = readUShort();
        return ((long)readUShort()<<16)|s;
    }
}
