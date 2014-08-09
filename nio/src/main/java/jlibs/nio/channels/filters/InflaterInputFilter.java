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
import jlibs.nio.util.Bytes;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

/**
 * @author Santhosh Kumar Tekuri
 */
public class InflaterInputFilter extends AbstractInputFilterChannel{
    protected Inflater inflater;
    protected ByteBuffer buffer;

    public InflaterInputFilter(){
        this(new Inflater());
    }

    protected InflaterInputFilter(Inflater inflater){
        this.inflater = inflater;
        buffer = Reactor.current().bufferPool.borrow(Bytes.CHUNK_SIZE);
    }

    @Override
    protected int _read(ByteBuffer dst) throws IOException{
        if(inflater==null)
            return -1;

        if(inflater.needsInput()){
            buffer.clear();
            int read = peerInput.read(buffer);
            if(read==0)
                return 0;
            else if(read==-1)
                throw new EOFException();
            else
                inflater.setInput(buffer.array(), 0, read);
        }

        int pos = dst.position();
        try{
            int uncompressed = inflater.inflate(dst.array(), dst.arrayOffset()+dst.position(), dst.remaining());
            if(inflater.finished() || inflater.needsDictionary())
                endInflater();
            dst.position(dst.position()+uncompressed);
        }catch(DataFormatException ex){
            String s = ex.getMessage();
            throw (ZipException)new ZipException(s!=null ? s : "Invalid ZLIB data format").initCause(ex);
        }

        int read = dst.position()-pos;
        if(read==0 && inflater==null)
            return -1;
        return read;
    }

    protected void endInflater(){
        if(inflater.getRemaining()>0){
            buffer.limit(buffer.position());
            buffer.position(buffer.position()-inflater.getRemaining());
            peerInput.unread(buffer);
        }else
            Reactor.current().bufferPool.returnBack(buffer);
        buffer = null;
        inflater.end();
        inflater = null;
    }

    @Override
    protected boolean isReadReady(){
        return inflater==null || !inflater.needsInput();
    }

    @Override
    public void dispose(){
        if(inflater!=null){
            inflater.end();
            inflater = null;
        }
        if(buffer !=null){
            Reactor.current().bufferPool.returnBack(buffer);
            buffer = null;
        }
    }
}
