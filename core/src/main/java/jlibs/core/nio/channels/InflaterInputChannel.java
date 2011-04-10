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

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

/**
 * @author Santhosh Kumar T
 */
public class InflaterInputChannel extends FilterInputChannel{
    protected Inflater inflater;

    public InflaterInputChannel(InputChannel delegate){
        this(delegate, new Inflater());
    }

    protected InflaterInputChannel(InputChannel delegate, Inflater inflater){
        super(delegate);
        this.inflater = inflater;
    }

    @Override
    protected boolean activateInterest(){
        return super.activateInterest() && inflater!=null && inflater.needsInput();
    }

    protected ByteBuffer readBuffer = ByteBuffer.allocate(1024);

    @Override
    protected int doRead(ByteBuffer dst) throws IOException{
        if(inflater==null)
            return -1;
        if(inflater.needsInput()){
            readBuffer.clear();
            int read = delegate.read(readBuffer);
            if(read==0)
                return 0;
            else if(read==-1)
                throw new EOFException();
            else
                inflater.setInput(readBuffer.array(), 0, read);
        }
        try{
            int uncompressed = inflater.inflate(dst.array(), dst.arrayOffset()+dst.position(), dst.remaining());
            if(uncompressed>0)
                dst.position(dst.position()+uncompressed);

            if(inflater.finished()){
                inflateFinished();
                inflater.end();
                inflater = null;
                if(uncompressed==0)
                    uncompressed = -1;
            }
            return uncompressed;
        }catch(DataFormatException ex){
            String s = ex.getMessage();
            throw (ZipException)new ZipException(s!=null ? s : "Invalid ZLIB data format").initCause(ex);
        }
    }

    @Override
    protected void onIOException(){
        try{
            inflater.end();
        }catch(Exception ignore){
            // ignore
        }
        inflater = null;
    }

    protected void inflateFinished(){
        if(inflater.getRemaining()>0)
            delegate.unread(readBuffer.array(), readBuffer.position()-inflater.getRemaining(), inflater.getRemaining(), false);
        readBuffer = null;
    }
}
