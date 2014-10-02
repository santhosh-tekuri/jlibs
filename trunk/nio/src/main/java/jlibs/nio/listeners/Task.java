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

package jlibs.nio.listeners;

import jlibs.nio.*;
import jlibs.nio.filters.BufferInput;
import jlibs.nio.filters.ChunkedOutput;
import jlibs.nio.util.BufferAllocator;
import jlibs.nio.util.Buffers;
import jlibs.nio.util.UnpooledBufferAllocator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static jlibs.nio.Debugger.DEBUG;
import static jlibs.nio.Debugger.println;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class Task{
    int firstOp;
    protected IOListener listener;
    protected Input in;
    protected Output out;

    protected Task(int firstOp){
        this.firstOp = firstOp;
    }

    protected void init(IOListener listener, Input in, Output out){
        this.listener = listener;
        this.in = in;
        this.out = out;
    }


    @Trace(condition=DEBUG, args="($1==1?'R':'W')")
    protected abstract boolean process(int readyOp) throws IOException;
    protected void cleanup(Throwable thr){}

    @Trace(condition=DEBUG, args="$1+\", \"+$2")
    protected int childTaskFinished(Task childTask, Throwable thr) throws Throwable{
        if(thr!=null)
            throw thr;
        return firstOp;
    }

    /*-------------------------------------------------[ writeBuffer ]---------------------------------------------------*/

    protected boolean write(ByteBuffer buffer) throws IOException{
        while(buffer.hasRemaining()){
            if(out.write(buffer)==0){
                out.addWriteInterest();
                return false;
            }
        }
        return true;
    }

    protected boolean send(ByteBuffer buffer) throws IOException{
        while(buffer.hasRemaining()){
            if(out.write(buffer)==0){
                out.addWriteInterest();
                return false;
            }
        }
        if(out.flush())
            return true;
        out.addWriteInterest();
        return false;
    }

    protected boolean flush() throws IOException{
        if(out.flush())
            return true;
        out.addWriteInterest();
        return false;
    }

    /*-------------------------------------------------[ readBuffer ]---------------------------------------------------*/

    protected boolean read(ByteBuffer buffer) throws IOException{
        while(buffer.hasRemaining()){
            int read = in.read(buffer);
            if(read==0){
                in.addReadInterest();
                return false;
            }else if(read==-1)
                return true;
        }
        return true;
    }

    /*-------------------------------------------------[ writeBuffers ]---------------------------------------------------*/

    private Buffers buffers;
    private BufferAllocator allocator = Reactor.current().allocator;
    protected void prepareFlush(Buffers buffers, boolean discard){
        if(discard)
            allocator = Reactor.current().allocator;
        else{
            buffers = buffers.copy();
            allocator = UnpooledBufferAllocator.HEAP;
        }
        buffers.removeEmpty(allocator);
        this.buffers = buffers;
    }

    protected void prepareFlush(Buffers buffers, BufferAllocator allocator){
        this.allocator = allocator;
        buffers.removeEmpty(allocator);
        this.buffers = buffers;
    }

    protected boolean flushBuffers() throws IOException{
        try{
            while(buffers.length>0 && out.write(buffers.array, buffers.offset, buffers.length)>0)
                buffers.removeEmpty(allocator);
            if(buffers.length>0 || !out.flush()){
                out.addWriteInterest();
                return false;
            }
            flushBuffersDone();
            return true;
        }catch(Throwable thr){
            flushBuffersDone();
            throw thr;
        }
    }

    private void flushBuffersDone(){
        if(buffers.length>0)
            allocator.free(buffers);
        buffers = null;
        allocator = Reactor.current().allocator;
    }

    /*-------------------------------------------------[ writeFile ]---------------------------------------------------*/

    private FileChannel fileChannel;
    private long fileOffset;
    private long fileLength;
    protected void prepareTransferFromFile(File file) throws IOException{
        fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ);
        fileOffset = 0;
        fileLength = fileChannel.size();
        if(out instanceof ChunkedOutput)
            ((ChunkedOutput)out).startChunk(fileLength);
    }

    protected boolean transferFromFile() throws IOException{
        try{
            while(fileLength>0){
                long wrote = out.transferFrom(fileChannel, fileOffset, fileLength);
                if(wrote==0){
                    out.addWriteInterest();
                    return false;
                }else{
                    fileOffset += wrote;
                    fileLength -= wrote;
                }
            }
        }catch(Throwable thr){
            transferFromFileDone();
            throw thr;
        }
        transferFromFileDone();
        return true;
    }

    private void transferFromFileDone() throws IOException{
        fileChannel.close();
        fileChannel = null;
    }

    /*-------------------------------------------------[ readBuffers ]---------------------------------------------------*/

    private ByteBuffer buffer;
    protected boolean read(Buffers buffers) throws IOException{
        // first-time
        if(buffer==null)
            buffer = allocator.allocate();

        try{
            while(true){
                int read = in.read(buffer);
                if(read==0){
                    in.addReadInterest();
                    return false;
                }else if(read==-1)
                    break;
                else if(!buffer.hasRemaining()){
                    buffer.flip();
                    buffers.append(buffer);
                    buffer = allocator.allocate();
                }
            }
        }catch(Throwable thr){
            try{
                readBuffersDone(in, buffers);
            }catch(Throwable suppressed){
                thr.addSuppressed(suppressed);
            }
            throw thr;
        }
        readBuffersDone(in, buffers);
        return true;
    }

    private void readBuffersDone(Input in, Buffers buffers) throws IOException{
        if(buffer.position()==0)
            allocator.free(buffer);
        else{
            buffer.flip();
            buffers.append(buffer);
        }
        buffer = null;
        in.close();
    }

    /*-------------------------------------------------[ closeOutputs ]---------------------------------------------------*/

    protected void closeInputs() throws IOException{
        while(true){
            if(in instanceof Transport)
                return;
            in.close();
            in = in.detachInput();
        }
    }

    protected boolean closeOutputs() throws IOException{
        while(true){
            if(out instanceof Transport){
                if(out.flush())
                    return true;
                out.addWriteInterest();
                return false;
            }else{
                out.close();
                if(out.flush())
                    out = out.detachOutput();
                else{
                    out.addWriteInterest();
                    return false;
                }
            }
        }
    }

    protected boolean closeOutput() throws IOException{
        out.close();
        if(out.flush())
            return true;
        else{
            out.addWriteInterest();
            return false;
        }
    }

    protected boolean shutdown() throws IOException{
        try{
            closeInputs();
            if(closeOutputs() && closeOutput()){
                in.close();
                return true;
            }else
                return false;
        }catch(Throwable thr){
            out.channel().shutdown();
            in.channel().shutdown();
            throw thr;
        }
    }

    /*-------------------------------------------------[ drainInputs ]---------------------------------------------------*/

    protected boolean drainInput(boolean close) throws IOException{
        if(buffer==null)
            buffer = allocator.allocate();
        try{
            while(true){
                int read;
                if(in instanceof BufferInput){
                    ((BufferInput)in).drainBuffer();
                    read = -1;
                }else
                    read = in.read(buffer);

                if(read==0){
                    in.addReadInterest();
                    return false;
                }else if(read==-1){
                    if(close)
                        in.close();
                    drainDone();
                    return true;
                }else
                    buffer.clear();
            }
        }catch(Throwable thr){
            drainDone();
            throw thr;
        }
    }

    protected boolean drainInputs() throws IOException{
        if(buffer==null)
            buffer = allocator.allocate();
        try{
            while(true){
                if(BufferInput.getOriginal(in) instanceof Transport){
                    while(in instanceof BufferInput){
                        BufferInput bin = (BufferInput)in;
                        if(bin.canDetach())
                            in = in.detachInput();
                        else
                            break;
                    }
                    drainDone();
                    return true;
                }
                while(true){
                    int read;
                    if(in instanceof BufferInput){
                        ((BufferInput)in).drainBuffer();
                        read = -1;
                    }else
                        read = in.read(buffer);

                    if(read==0){
                        in.addReadInterest();
                        return false;
                    }else if(read==-1){
                        in.close();
                        in = in.detachInput();
                        break;
                    }else
                        buffer.clear();
                }
            }
        }catch(Throwable thr){
            drainDone();
            throw thr;
        }
    }

    private void drainDone(){
        allocator.free(buffer);
        buffer = null;
    }

    /*-------------------------------------------------[ switch-IO ]---------------------------------------------------*/

    private Input _in;
    private Output _out;
    protected void switchIO(Input in, Output out){
        if(this.in!=in){
            _in = this.in;
            this.in = in;
            in.setInputListener(listener);
        }
        if(this.out!=out){
            _out = this.out;
            this.out = out;
            out.setOutputListener(listener);
        }
    }

    protected void revertIO(){
        if(_in!=null){
            if(in.getInputListener()==listener)
                in.setInputListener(null);
            in = _in;
            _in = null;
        }
        if(_out!=null){
            if(out.getOutputListener()==listener)
                out.setOutputListener(null);
            out = _out;
            _out = null;
        }
    }

    /*-------------------------------------------------[ pumping ]---------------------------------------------------*/

    private boolean flushNeeded;
    protected void preparePump(Buffers backup){
        buffers = backup;
        buffer = allocator.allocate();
        flushNeeded = false;
    }

    protected boolean doPump(int readyOp) throws IOException{
        boolean flushing = false;
        try{
            if(readyOp==OP_WRITE && flushNeeded){
                flushing = true;
                if(!out.flush()){
                    out.addWriteInterest();
                    return false;
                }
                flushing = false;
                flushNeeded = false;
                readyOp = OP_READ;
            }
            while(true){
                if(readyOp==OP_READ){
                    int read = in.read(buffer);
                    if(read==0){
                        in.addReadInterest();
                        if(flushNeeded){
                            flushing = true;
                            if(!out.flush()){
                                out.addWriteInterest();
                                return false;
                            }
                            flushing = false;
                            flushNeeded = false;
                        }
                        return false;
                    }else if(read==-1)
                        break;
                    // flip -------------------
                    if(buffers==null)
                        buffer.flip();
                    else{
                        buffer.limit(buffer.position());
                        buffer.position(buffer.limit()-read);
                    }
                    readyOp = OP_WRITE;
                    flushNeeded = false;
                }

                if(buffer.hasRemaining()){
                    do{
                        if(out.write(buffer)==0){
                            out.addWriteInterest();
                            return false;
                        }
                    }while(buffer.hasRemaining());
                    flushNeeded = true;

                    // clear -------------------
                    if(buffers==null)
                        buffer.clear();
                    else{
                        if(buffer.limit()==buffer.capacity()){
                            buffer.flip();
                            buffers.append(buffer);
                            buffer = allocator.allocate();
                        }else{
                            buffer.position(buffer.limit());
                            buffer.limit(buffer.capacity());
                        }
                    }
                }
                readyOp = OP_READ;
            }
        }catch(Throwable thr){
            try{
                pumpDone(flushing ? OP_READ : readyOp);
            }catch(Throwable suppressed){
                thr.addSuppressed(suppressed);
            }
            throw readyOp==OP_WRITE || flushing ? new OutputException(thr) : new InputException(thr);
        }
        try{
            pumpDone(readyOp);
        }catch(Throwable thr){
            throw new InputException(thr);
        }
        return true;
    }

    private void pumpDone(int readyOp) throws IOException{
        if(buffers!=null){
            if(readyOp==OP_READ){
                if(buffer.position()!=0){
                    buffer.flip();
                    buffers.append(buffer);
                    buffer = null;
                }
            }else{
                if(buffer.limit()!=0){
                    buffer.position(0);
                    buffers.append(buffer);
                    buffer = null;
                }
            }
        }
        if(buffer!=null){
            allocator.free(buffer);
            buffer = null;
        }

        buffers = null;
        in.close();
    }

    /*-------------------------------------------------[ writeToOutputStream ]---------------------------------------------------*/

    private OutputStream os;
    protected void prepareWriteToOutputStream(OutputStream os, Buffers backup){
        this.os = os;
        buffers = backup;
        buffer = allocator.allocateHeap();
    }

    protected boolean doWriteToOutputStream() throws IOException{
        try{
            while(true){
                int read = in.read(buffer);
                if(read==0){
                    in.addReadInterest();
                    return false;
                }else if(read==-1)
                    break;
                else{
                    os.write(buffer.array(), 0, buffer.position());
                    if(buffers==null)
                        buffer.clear();
                    else if(buffer.remaining()==0){
                        buffer.flip();
                        buffers.append(buffer);
                        buffer = allocator.allocateHeap();
                    }
                }
            }
        }catch(Throwable thr){
            writeToOutputStreamDone();
            throw thr;
        }
        writeToOutputStreamDone();
        return true;
    }

    private void writeToOutputStreamDone() throws IOException{
        if(buffers!=null){
            if(buffer.position()>0 && buffers!=null){
                buffer.flip();
                buffers.append(buffer);
                buffers = null;
                buffer = null;
            }
        }
        if(buffer!=null){
            allocator.free(buffer);
            buffer = null;
        }
        in.close();
    }

    /*-------------------------------------------------[ Child Task ]---------------------------------------------------*/

    Task child;
    Task parent;
    protected void setChild(Task child){
        if(this.child!=null)
            throw new UnsupportedOperationException(child+"="+child.getClass().getName());
        if(DEBUG)
            println("child = "+child);
        this.child = child;
        child.parent = this;
        child.init(listener, in, out);
    }

    protected boolean hasChild(){
        return child!=null;
    }
}
