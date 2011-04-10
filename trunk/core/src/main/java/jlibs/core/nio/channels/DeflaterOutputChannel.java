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
import java.util.zip.Deflater;

/**
 * @author Santhosh Kumar T
 */
public class DeflaterOutputChannel extends FilterOutputChannel{
    protected Deflater deflater = new Deflater();

    public DeflaterOutputChannel(OutputChannel delegate){
        this(delegate, new Deflater());
    }

    protected DeflaterOutputChannel(OutputChannel delegate, Deflater deflater){
        super(delegate);
        this.deflater = deflater;
        writeBuffer.limit(0);
    }

    private ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

    @Override
    protected int onWrite(ByteBuffer src) throws IOException{
        int wrote = 0;
        if(deflater.needsInput()){
            wrote = src.remaining();
            deflater.setInput(src.array(), src.arrayOffset()+src.position(), wrote);
            src.position(src.limit());
        }
        return wrote;
    }

    protected void doWritePending() throws IOException{
        while(true){
            if(writeBuffer.hasRemaining()){
                delegate.write(writeBuffer);
                if(writeBuffer.hasRemaining())
                    break;
            }
            if(deflater.finished()){
                deflater.end();
                break;
            }
            if(isOpen() && deflater.needsInput())
                break;
            int compressed = deflater.deflate(writeBuffer.array(), 0, writeBuffer.capacity());
            writeBuffer.position(0);
            writeBuffer.limit(compressed);
        }
    }

    @Override
    protected Status selfStatus(){
        return !writeBuffer.hasRemaining() && ((isOpen() && deflater.needsInput()) || deflater.finished()) ? Status.COMPLETED : Status.NEEDS_OUTPUT;
    }

    @Override
    protected void onIOException(){
        try{
            deflater.end();
        }catch(Exception ignore){
            // ignore
        }
    }

    @Override
    protected void doClose() throws IOException{
        deflater.finish();
    }
}
