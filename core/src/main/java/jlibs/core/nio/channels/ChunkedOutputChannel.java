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

import jlibs.core.io.IOUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar T
 */
public class ChunkedOutputChannel extends FilterOutputChannel{
    public ChunkedOutputChannel(OutputChannel delegate){
        super(delegate);
        chunkStart.limit(0);
        chunkEnd.limit(0);
    }

    private ByteBuffer chunkStart = ByteBuffer.allocate(100);
    private ByteBuffer writeBuffer;
    private ByteBuffer chunkEnd = ByteBuffer.wrap(new byte[]{ '\r', '\n' });
    private boolean lastChunk;

    @Override
    protected int onWrite(ByteBuffer src) throws IOException{
        int wrote = src.remaining();
        writeBuffer = src.duplicate();
        src.position(src.limit());
        notifyChunk(wrote);
        return wrote;
    }

    @Override
    protected void doWritePending() throws IOException{
        while(true){
            if(chunkStart.hasRemaining()){
                delegate.write(chunkStart);
                if(chunkStart.hasRemaining())
                    return;
            }
            if(writeBuffer!=null){
                delegate.write(writeBuffer);
                if(writeBuffer.hasRemaining())
                    return;
                else
                    writeBuffer = null;
            }
            if(chunkEnd.hasRemaining()){
                delegate.write(chunkEnd);
                if(chunkEnd.hasRemaining())
                    return;
            }
            if(isOpen() || lastChunk)
                return;

            lastChunk = true;
            writeBuffer = null;
            notifyChunk(0);
        }
    }

    @Override
    protected Status selfStatus(){
        return !chunkEnd.hasRemaining() && (isOpen() || lastChunk) ? Status.COMPLETED : Status.NEEDS_OUTPUT;
    }

    private Listener listener;

    public Listener getListener(){
        return listener;
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    private void notifyChunk(int len){
        chunkStart.clear();
        chunkStart.put(Integer.toString(len, 16).getBytes(IOUtil.US_ASCII));
        if(listener!=null){
            String extension = listener.onChunk(len);
            if(extension!=null){
                chunkStart.put((byte)';');
                chunkStart.put(extension.getBytes(IOUtil.US_ASCII));
            }
        }
        chunkStart.put(chunkEnd.array());
        if(len==0)
            chunkStart.put(chunkEnd.array());
        else
            chunkEnd.clear();
        chunkStart.flip();
    }

    public interface Listener{
        public String onChunk(int len);
    }
}
