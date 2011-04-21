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

import jlibs.core.nio.SelectableByteChannel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Santhosh Kumar T
 */
public class SimulatingByteChannel implements SelectableByteChannel{
    private InputStream reader;
    private Iterator<Integer> readChunks;
    private int readChunk = 0;

    public SimulatingByteChannel(InputStream reader, Iterator<Integer> readChunks, boolean inReady, InputStream writer, Iterator<Integer> writeChunks, boolean outReady){
        this.reader = reader;
        this.readChunks = readChunks;
        this.writer = writer;
        this.writeChunks = writeChunks;

        readInterested = inReady;
        writeInterested = outReady;
        prepare();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException{
        if(readChunk==0)
            return readChunks.hasNext() ? 0 : -1;

        int min = Math.min(dst.remaining(), readChunk);
        min = reader.read(dst.array(), dst.arrayOffset()+dst.position(), min);
        if(min>=0){
            dst.position(dst.position()+min);
            readChunk -= min;
        }
        return min;
    }

    private InputStream writer;
    private Iterator<Integer> writeChunks;
    private int writeChunk = 0;

    @Override
    public int write(ByteBuffer dst) throws IOException{
        if(writeChunk==0)
            return 0;

        int min = Math.min(dst.remaining(), writeChunk);
        ByteBuffer temp = ByteBuffer.allocate(min);
        int read = writer.read(temp.array(), 0, min);
        if(read!=min)
            throw new AssertionError("trying to write "+min+" bytes but only allowed "+read);

        for(int i=0; i<min; i++){
            if(dst.get()!=temp.get())
                throw new AssertionError("data mismatch");
        }
        writeChunk -= min;
        return min;
    }

    boolean readInterested, writeInterested;

    @Override
    public void addInterest(int operation) throws IOException{
        if(operation== SelectionKey.OP_READ)
            readInterested = true;
        if(operation==SelectionKey.OP_WRITE)
            writeInterested = true;
        if(readInterested || writeInterested)
            NIOSimulator.INSTANCE.register(this);
    }

    @Override
    public void removeInterest(int operation) throws IOException{
        if(operation==SelectionKey.OP_READ)
            readInterested = false;
        if(operation==SelectionKey.OP_WRITE)
            writeInterested = false;
        if(!readInterested && !writeInterested)
            NIOSimulator.INSTANCE.unregister(this);
    }

    public boolean isReadable(){
        return readInterested;
    }

    public boolean isWritable(){
        return writeInterested;
    }

    public void prepare(){
        if(readInterested && readChunk==0 && readChunks.hasNext())
            readChunk = readChunks.next();
        if(writeInterested && writeChunk==0 && writeChunks.hasNext())
            writeChunk = writeChunks.next();
        readInterested = writeInterested = false;
    }

    private static AtomicLong ID_GENERATOR = new AtomicLong();
    protected final long id = ID_GENERATOR.incrementAndGet();

    @Override
    public long id(){
        return id;
    }

    private Object attachment;
    @Override
    public void attach(Object obj){
        attachment = obj;
    }

    @Override
    public Object attachment(){
        return attachment;
    }

    private boolean open = true;
    @Override
    public boolean isOpen(){
        return open;
    }

    @Override
    public void close() throws IOException{
        open = false;
    }

    public boolean isInPending(){
        return readChunks.hasNext() || readChunk>0;
    }

    public boolean isOutPending(){
        return writeChunks.hasNext() || writeChunk>0;
    }
}
