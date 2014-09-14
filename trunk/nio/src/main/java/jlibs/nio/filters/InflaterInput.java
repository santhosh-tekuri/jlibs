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

import jlibs.nio.Input;
import jlibs.nio.InputFilter;
import jlibs.nio.Reactor;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

/**
 * @author Santhosh Kumar Tekuri
 */
public class InflaterInput extends InputFilter{
    protected Inflater inflater;
    protected ByteBuffer buffer;
    private ByteBuffer tmpBuffer;

    public InflaterInput(Input peer){
        this(new Inflater(false), peer);
    }

    protected InflaterInput(Inflater inflater, Input in){
        super(in);
        buffer = Reactor.current().allocator.allocateHeap();
        this.inflater = inflater;
    }

    @Override
    protected boolean readReady(){
        return inflater==null || !inflater.needsInput();
    }

    protected int inflate(byte bytes[], int offset, int len) throws DataFormatException{
        return inflater.inflate(bytes, offset, len);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException{
        if(inflater==null)
            return -1;

        if(inflater.needsInput()){
            buffer.clear();
            int read = peer.read(buffer);
            if(read==0)
                return 0;
            else if(read==-1)
                throw new EOFException();
            else
                inflater.setInput(buffer.array(), 0, read);
        }

        int pos = dst.position();
        try{
            if(dst.hasArray()){
                int uncompressed = inflate(dst.array(), dst.arrayOffset()+dst.position(), dst.remaining());
                dst.position(dst.position()+uncompressed);
            }else{
                if(tmpBuffer==null)
                    tmpBuffer = Reactor.current().allocator.allocateHeap();
                int min = Math.min(dst.remaining(), tmpBuffer.remaining());
                int uncompressed = inflate(tmpBuffer.array(), 0, min);
                tmpBuffer.position(0);
                tmpBuffer.limit(uncompressed);
                dst.put(tmpBuffer);
            }
            if(inflater.finished() || inflater.needsDictionary())
                endInflater();
        }catch(DataFormatException ex){
            String s = ex.getMessage();
            throw (ZipException)new ZipException(s!=null ? s : "Invalid ZLIB data format").initCause(ex);
        }

        int read = dst.position()-pos;
        if(read==0 && inflater==null){
            eof = true;
            return -1;
        }
        return read;
    }

    protected void endInflater(){
        if(tmpBuffer!=null){
            Reactor.current().allocator.free(tmpBuffer);
            tmpBuffer = null;
        }
        if(inflater.getRemaining()>0){
            buffer.limit(buffer.position());
            buffer.position(buffer.position()-inflater.getRemaining());
        }else{
            Reactor.current().allocator.free(buffer);
            buffer = null;
        }
        inflater.end();
        inflater = null;
    }

    @Override
    protected ByteBuffer detached(){
        if(tmpBuffer!=null){
            Reactor.current().allocator.free(tmpBuffer);
            tmpBuffer = null;
        }
        if(inflater!=null){
            inflater.end();
            inflater = null;
            if(buffer!=null){
                Reactor.current().allocator.free(buffer);
                buffer = null;
            }
        }
        return buffer;
    }

    @Override
    public long available(){
        return 0;
    }
}
