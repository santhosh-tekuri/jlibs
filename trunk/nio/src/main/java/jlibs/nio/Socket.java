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

package jlibs.nio;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectionKey;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static jlibs.nio.Debugger.IO;
import static jlibs.nio.Debugger.println;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class Socket implements Transport{
    private static final SocketTimeoutException SOCKET_TIMEOUT_EXCEPTION = new SocketTimeoutException();
    static{
        SOCKET_TIMEOUT_EXCEPTION.setStackTrace(new StackTraceElement[0]);
    }

    private final NBStream channel;
    protected final SelectionKey selectionKey;

    public Socket(NBStream channel, SelectionKey selectionKey){
        this.channel = channel;
        this.selectionKey = selectionKey;
        reader = channel.selectable instanceof ScatteringByteChannel ? (ScatteringByteChannel)channel.selectable : null;
        writer = channel.selectable instanceof GatheringByteChannel ? (GatheringByteChannel)channel.selectable : null;
    }

    @Override
    public NBStream channel(){
        return channel;
    }

    /*-------------------------------------------------[ Source ]---------------------------------------------------*/

    private Input.Listener inputListener;
    @Override public Input.Listener getInputListener(){ return inputListener; }
    @Override public void setInputListener(Input.Listener listener){ inputListener = listener; }

    private final ScatteringByteChannel reader;
    Input peekIn = this;
    boolean peekInInterested;
    private boolean eof;

    @Override
    public void addReadInterest(){
        if(peekIn==this)
            peekInInterested = true;
        if(newInterests==-1){
            if(IO)
                println(selectable()+".addInterestOps(R)");
            selectionKey.interestOps(selectionKey.interestOps()|OP_READ);
            if(channel.getTimeout()>0)
                channel.reactor.startTimer(channel, channel.getTimeout());
        }else
            newInterests |= OP_READ;
    }

    @Override
    public void wakeupReader(){
        if(inputListener!=null){
            peekInInterested = true;
            channel.wakeup();
        }
    }

    @Override
    public int read(ByteBuffer dst) throws IOException{
        if(timeout)
            throw SOCKET_TIMEOUT_EXCEPTION;
        int read = reader.read(dst);
        eof = read==-1;
        return read;
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException{
        if(timeout)
            throw SOCKET_TIMEOUT_EXCEPTION;
        long read = reader.read(dsts);
        eof = read==-1;
        return read;
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException{
        if(timeout)
            throw SOCKET_TIMEOUT_EXCEPTION;
        long read = reader.read(dsts, offset, length);
        eof = read==-1;
        return read;
    }

    @Override
    public long transferTo(long position, long count, FileChannel target) throws IOException{
        return target.transferFrom(reader, position, count);
    }

    @Override
    public long available(){
        return 0;
    }

    @Override
    public boolean eof(){
        return eof;
    }

    @Override
    public Output detachOutput(){
        return this;
    }

    /*-------------------------------------------------[ Sink ]---------------------------------------------------*/

    private Output.Listener outputListener;
    @Override public Output.Listener getOutputListener(){ return outputListener; }
    @Override public void setOutputListener(Output.Listener listener){ outputListener = listener; }

    private final GatheringByteChannel writer;
    Output peekOut = this;
    boolean peekOutInterested;

    @Override
    public void addWriteInterest(){
        if(peekOut==this)
            peekOutInterested = true;
        if(newInterests==-1){
            if(IO)
                println(selectable()+".addInterestOps(W)");
            selectionKey.interestOps(selectionKey.interestOps()|OP_WRITE);
            if(channel.getTimeout()>0)
                channel.reactor.startTimer(channel, channel.getTimeout());
        }else
            newInterests |= OP_WRITE;
    }

    @Override
    public void wakeupWriter(){
        if(outputListener!=null){
            peekOutInterested = true;
            channel.wakeup();
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException{
        if(timeout)
            throw SOCKET_TIMEOUT_EXCEPTION;
        return writer.write(src);
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException{
        if(timeout)
            throw SOCKET_TIMEOUT_EXCEPTION;
        return writer.write(srcs);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException{
        if(timeout)
            throw SOCKET_TIMEOUT_EXCEPTION;
        return writer.write(srcs, offset, length);
    }

    @Override
    public long transferFrom(FileChannel src, long position, long count) throws IOException{
        return src.transferTo(position, count, writer);
    }

    @Override
    @Trace(condition=false)
    public boolean flush() throws IOException{
        return true;
    }

    @Override
    public Input detachInput(){
        return this;
    }

    /*-------------------------------------------------[ Closeable ]---------------------------------------------------*/

    @Override
    public boolean isOpen(){
        return channel.selectable.isOpen();
    }

    @Override
    public void close() throws IOException{
        if(isOpen()){
            channel.closing();
            channel.selectable.close();
        }
    }

    /*-------------------------------------------------[ Process ]---------------------------------------------------*/

    private int newInterests = -1;
    private boolean timeout;
    void process(boolean timeout){
        this.timeout = timeout;
        int readyOps = selectionKey.readyOps();
        int socketInterests = selectionKey.interestOps();
        int oldInterests = newInterests = selectionKey.interestOps()&~readyOps;
        try{
            boolean peekSourceInterested = this.peekInInterested;
            boolean peekSinkInterested = this.peekOutInterested;
            this.peekInInterested = this.peekOutInterested = false;

            if(peekSourceInterested){
                boolean notify = false;
                if(timeout)
                    notify = true;
                else if((readyOps&OP_READ)!=0)
                    notify = true;
                else if((readyOps&OP_WRITE)!=0 && !peekSinkInterested)
                    notify = true;
                if(notify){
                    try{
                        if(inputListener !=null)
                            inputListener.process(peekIn);
                    }catch(Throwable thr){
                        channel.reactor.handleException(thr);
                    }
                }
            }

            if(channel.isOpen() && peekSinkInterested){
                boolean notify = false;
                if(timeout)
                    notify = true;
                else if((readyOps&OP_WRITE)!=0)
                    notify = true;
                else if((readyOps&OP_READ)!=0 && !peekSourceInterested)
                    notify = true;
                if(notify){
                    try{
                        if(outputListener !=null)
                            outputListener.process(peekOut);
                    }catch(Throwable thr){
                        channel.reactor.handleException(thr);
                    }
                }
            }
        }finally{
            if(channel.isOpen()){
                if(newInterests!=socketInterests){
                    if(IO)
                        println(selectable()+".setInterestOps("+Debugger.ops(newInterests)+")");
                    selectionKey.interestOps(newInterests);
                }
                if(oldInterests!=newInterests && channel.getTimeout()>0)
                    channel.reactor.startTimer(channel, channel.getTimeout());
                newInterests = -1;
            }
        }
    }

    void wakeupNow(){
        try{
            if(peekInInterested){
                peekInInterested = false;
                if(inputListener!=null)
                    inputListener.process(peekIn);
            }
        }catch(Throwable thr){
            channel.reactor.handleException(thr);
        }
        try{
            if(peekOutInterested){
                peekOutInterested = false;
                if(outputListener!=null)
                    outputListener.process(peekOut);
            }
        }catch(Throwable thr){
            channel.reactor.handleException(thr);
        }
    }

    private String selectable(){
        String str = channel.toString();
        return "Selectable"+str.substring(channel.getClass().getSimpleName().length());
    }

    @Override
    public String toString(){
        String str = channel.toString();
        return "Socket"+str.substring(channel.getClass().getSimpleName().length());
    }
}
