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

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public class GZIPInputFilter  extends InflaterInputFilter{
    public GZIPInputFilter(){
        super(new Inflater(true));
    }

    private static final int STATE_GZIP_MAGIC = 0;
    private static final int STATE_COMPRESSION_METHOD = 1;
    private static final int STATE_FLAG = 2;
    private static final int STATE_MTIME_XFL_OS = 3;
    private static final int STATE_READ_EXTRA = 4;
    private static final int STATE_SKIP_EXTRA = 5;
    private static final int STATE_SKIP_FNAME = 6;
    private static final int STATE_SKIP_FCOMMENT = 7;
    private static final int STATE_SKIP_FHCRC = 8;
    private static final int STATE_CONTENT = 9;
    private static final int STATE_TRAILER = 10;
    private static final int STATE_FINISHED = 11;

    private int state = STATE_GZIP_MAGIC;
    private CRC32 crc = new CRC32();

    @Override
    protected int _read(ByteBuffer dst) throws IOException{
        if(state==STATE_CONTENT){
            int pos = dst.position();
            int read = super._read(dst);
            if(read==-1){
                state = STATE_TRAILER;
                readTrailer();
            }else{
                crc.update(dst.array(), pos, read);
                return read;
            }
        }

        if(state==STATE_FINISHED)
            return -1;

        int read = peerInput.read(buffer);
        if(read==0)
            return 0;
        if(read==-1)
            throw new EOFException();

        switch(state){
            case STATE_GZIP_MAGIC:
                if(canRead(USHORT)){
                    if(readUShort()!= GZIPInputStream.GZIP_MAGIC)
                        throw new ZipException("Not in GZIP format");
                    state++;
                }else
                    return 0;
            case STATE_COMPRESSION_METHOD:
                if(canRead(UBYTE)){
                    if(readUByte()!= Deflater.DEFLATED)
                        throw new ZipException("Unsupported compression method");
                    state++;
                }else
                    return 0;
            case STATE_FLAG:
                if(canRead(UBYTE)){
                    flag = readUByte();
                    state++;
                }else
                    return 0;
            case STATE_MTIME_XFL_OS:
                if(canRead(6)){
                    readPos += 6;
                    state++;
                }else
                    return 0;
            case STATE_READ_EXTRA:
                if((flag&FEXTRA)==FEXTRA){
                    if(canRead(USHORT)){
                        extraLen = readUShort();
                        state++;
                    }else
                        return 0;
                }else
                    state++;
            case STATE_SKIP_EXTRA:
                if((flag&FEXTRA)==FEXTRA){
                    if(canRead(extraLen)){
                        readPos += extraLen;
                        state++;
                    }else
                        return 0;
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
                    if(state==STATE_SKIP_FNAME)
                        return 0;
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
                    if(state==STATE_SKIP_FCOMMENT)
                        return 0;
                }else
                    state++;
            case STATE_SKIP_FHCRC:
                if((flag&FHCRC)==FHCRC){
                    if(canRead(USHORT)){
                        crc.update(buffer.array(), 0, readPos);
                        int crcValue = (int)crc.getValue() & 0xffff;
                        if(crcValue!=readUShort())
                            throw new ZipException("Corrupt GZIP header");
                        crc.reset();
                        state++;
                    }else
                        return 0;
                }else
                    state++;
                if(readPos<buffer.position())
                    inflater.setInput(buffer.array(), readPos, buffer.position()-readPos);
                return _read(dst);
            default:
                assert state==STATE_TRAILER;
                readTrailer();
                return state==STATE_FINISHED ? -1 : 0;
        }
    }

    private long isize;
    @Override
    protected void endInflater(){
        isize = inflater.getBytesWritten() & 0xffffffffL; // rfc1952; ISIZE is the input size modulo 2^32
        if(inflater.getRemaining()>0){
            if(inflater.getRemaining()+buffer.remaining()>=4)
                readPos = buffer.position()-inflater.getRemaining();
            else{
                buffer.limit(buffer.position());
                buffer.position(buffer.position()-inflater.getRemaining());
                buffer.compact();
                readPos = 0;
            }
        }else{
            buffer.clear();
            readPos = 0;
        }
        inflater.end();
        inflater = null;
    }

    private void readTrailer() throws IOException{
        if(canRead(UINT+UINT)){
            if(readUInt()!=crc.getValue() || readUInt()!=isize)
                throw new ZipException("Corrupt GZIP trailer");
            if(readPos<buffer.position()){
                buffer.limit(buffer.position());
                buffer.position(readPos);
                peerInput.unread(buffer);
            }else
                Reactor.current().bufferPool.returnBack(buffer);
            buffer = null;
            state++;
        }
    }

    @Override
    protected boolean isReadReady(){
        return state==STATE_FINISHED ||
                (state==STATE_CONTENT && !inflater.needsInput());
    }

    private final static int FHCRC = 2;    // Header CRC
    private final static int FEXTRA = 4;    // Extra field
    private final static int FNAME = 8;    // File name
    private final static int FCOMMENT = 16;    // File comment

    private static final int UBYTE = 1;
    private static final int USHORT = UBYTE+UBYTE;
    private static final int UINT = USHORT+USHORT;

    private int readPos = 0;
    private int flag;
    private int extraLen = -1;
    private boolean canRead(int size){
        return buffer.position()-readPos>=size;
    }

    private int readUByte() throws IOException{
        int b = buffer.get(readPos++) & 0xff;
        if(b<-1 || b>255)
            throw new ZipException("value out of range -1..255: " + b);
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
