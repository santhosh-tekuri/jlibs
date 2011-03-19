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

package jlibs.core.nio;

import jlibs.core.lang.ImpossibleException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.*;
import static javax.net.ssl.SSLEngineResult.Status.BUFFER_UNDERFLOW;

/**
 * @author Santhosh Kumar T
 */
public class SSLTransport extends Debuggable implements Transport{
    protected Transport transport;
    private final SSLEngine engine;

    private final ByteBuffer dummy = ByteBuffer.allocate(0);

    private final ByteBuffer appReadBuffer; // ready for app.read
    private ByteBuffer appWriteBuffer; // ready for app.write
    private final ByteBuffer netReadBuffer; // ready for engine.unwrap
    private final ByteBuffer netWriteBuffer; // ready for channel.write

    private boolean initialHandshake = true;
    private SSLEngineResult.HandshakeStatus hsStatus;
    private SSLEngineResult.Status status = null;

    private HandshakeCompletedListener handshakeCompletedListener;

    public SSLTransport(Transport transport, SSLEngine engine, HandshakeCompletedListener handshakeCompletedListener) throws IOException{
        this.transport = transport;
        this.engine = engine;
        this.handshakeCompletedListener = handshakeCompletedListener;

        SSLSession session = engine.getSession();

        appReadBuffer = ByteBuffer.allocate(session.getApplicationBufferSize());
        appReadBuffer.position(appReadBuffer.limit());

        appWriteBuffer = dummy;

        netReadBuffer = ByteBuffer.allocate(session.getPacketBufferSize());
        netReadBuffer.position(netReadBuffer.limit());

        netWriteBuffer = ByteBuffer.allocate(session.getPacketBufferSize());
        netWriteBuffer.position(netWriteBuffer.limit());

        start();
    }

    @Override
    public long id(){
        return -transport.id();
    }

    @Override
    public ClientChannel client(){
        return transport.client();
    }

    private void validate() throws IOException{
        if(asyncException!=null)
            throw new IOException("ASYNC_EXCEPTION: "+asyncException.getMessage(), asyncException);
        if(appClosed)
            throw new ClosedChannelException();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException{
        if(DEBUG)
            println("app@"+id()+".read{");
        try{
            int read = appRead(dst);
            if(DEBUG)
                println("return: "+read);
            return read;
        }finally{
            if(DEBUG)
                println("}");
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException{
        if(DEBUG)
            println("app@"+id()+".write{");
        try{
            int wrote = appWrite(src);
            if(DEBUG)
                println("return: "+wrote);
            return wrote;
        }finally{
            if(DEBUG)
                println("}");
        }
    }

    /*-------------------------------------------------[ Processing ]---------------------------------------------------*/

    private void start() throws IOException{
        if(DEBUG)
            println("app@"+id()+".start{");
        int ops = transport.interests();
        boolean readWait = (ops&OP_READ)!=0;
        boolean writeWait = (ops&OP_WRITE)!=0;
        if(readWait)
            transport.removeInterest(OP_READ);
        if(writeWait)
            transport.removeInterest(OP_WRITE);

        engine.beginHandshake();
        switch(hsStatus=engine.getHandshakeStatus()){
            case NEED_UNWRAP:
                channelRead();
                break;
            case NEED_WRAP:
                channelWrite();
                break;
            default:
                assert false;
        }

        if(readWait)
            addInterest(OP_READ);
        if(writeWait)
            addInterest(OP_WRITE);
        if(DEBUG)
            println("}");
    }

    private boolean run() throws IOException{
        try{
            while(true){
                switch(hsStatus){
                    case FINISHED:
                        if(handshakeCompletedListener!=null)
                            handshakeCompletedListener.handshakeCompleted(new HandshakeCompletedEvent(client(), engine.getSession()));
                        break;
                    case NEED_TASK:
                        processNeedTask();
                        continue;
                    case NEED_WRAP:
                        if(processWrap())
                            continue;
                        return false;
                    case NEED_UNWRAP:
                        if(processUnwrap())
                            continue;
                        return false;
                }
                initialHandshake = false;
                return true;
            }
        }finally{
            if(engine.isInboundDone() && engine.isOutboundDone() && !netWriteBuffer.hasRemaining())
                closeTransport();
        }
    }

    private void processNeedTask(){
        Runnable task;
        while((task=engine.getDelegatedTask())!=null)
            task.run();
        hsStatus = engine.getHandshakeStatus();
        if(DEBUG){
            println(String.format(
                "%-6s: %-16s %-15s %5d %5d",
                "tasks",
                "OK", hsStatus,
                0, 0
            ));
        }
    }

    private boolean processWrap() throws IOException{
        assert !netWriteBuffer.hasRemaining();

        netWriteBuffer.clear();
        SSLEngineResult result = engine.wrap(appWriteBuffer, netWriteBuffer);
        hsStatus = result.getHandshakeStatus();
        status = result.getStatus();
        netWriteBuffer.flip();

        if(DEBUG){
            println(String.format(
                "%-6s: %-16s %-15s %5d %5d",
                "wrap",
                status, hsStatus,
                result.bytesConsumed(), result.bytesProduced()
            ));
        }

        switch(status){
            case BUFFER_UNDERFLOW: // Nothing to wrap: no data was present in appWriteBuffer
            case BUFFER_OVERFLOW: // not enough room in netWriteBuffer
                throw new ImpossibleException();
            default: // OK or CLOSED
                assert status!=SSLEngineResult.Status.CLOSED || engine.isOutboundDone();
                return flush();
        }
    }

    private boolean flush() throws IOException{
        if(netWriteBuffer.hasRemaining()){
            transport.write(netWriteBuffer);
            if(netWriteBuffer.hasRemaining()){
                waitForChannelWrite();
                return false;
            }
        }
        if(engine.isOutboundDone()){
            assert (transport.interests() & OP_WRITE)==0;
            try{
                transport.shutdownOutput();
            }catch(IOException ex){
                // ignore
            }
        }
        return true;
    }

    private boolean processUnwrap() throws IOException{
        assert !appReadBuffer.hasRemaining();
        if(!netReadBuffer.hasRemaining()){
            waitForChannelRead();
            return false;
        }

        appReadBuffer.clear();
        SSLEngineResult result = engine.unwrap(netReadBuffer, appReadBuffer);
        appReadBuffer.flip();
        hsStatus = result.getHandshakeStatus();
        status = result.getStatus();

        if(DEBUG){
            println(String.format(
                "%-6s: %-16s %-15s %5d %5d",
                "unwrap",
                result.getStatus(), result.getHandshakeStatus(),
                result.bytesConsumed(), result.bytesProduced()
            ));
        }

        switch(status){
            case BUFFER_OVERFLOW: // not enough room in appReadBuffer
                throw new ImpossibleException();
            case BUFFER_UNDERFLOW: // not enough data in netReadBuffer
                waitForChannelRead();
                return false;
            case OK:
                if(appReadBuffer.hasRemaining()){
                    if(appClosed){
                        if(DEBUG)
                            println("discarding data in appReadBuffer@"+id());
                        appReadBuffer.position(appReadBuffer.limit());
                        return true;
                    }
                    if(appReadWait)
                        enableAppRead();
                    return false;
                }else
                    return true;
            case CLOSED:
                assert engine.isInboundDone();
                try{
                    client().realChannel().socket().shutdownInput();
                }catch(IOException ex){
                    if(!client().realChannel().socket().isInputShutdown())
                        throw ex;
                }
                if(appReadWait)
                    enableAppRead();
                return false;
        }
        throw new ImpossibleException();
    }

    /*-------------------------------------------------[ Wait ]---------------------------------------------------*/

    private boolean channelReadWait = false;
    private void waitForChannelRead() throws IOException{
        if(!channelReadWait){
            channelReadWait = true;
            transport.addInterest(OP_READ);
        }
    }

    private boolean channelWriteWait = false;
    private void waitForChannelWrite() throws IOException{
        if(!channelWriteWait){
            channelWriteWait = true;
            transport.addInterest(OP_WRITE);
        }
    }

    private boolean appReadWait = false;
    private void waitForAppRead() throws IOException{
        if(!appReadWait){
            appReadWait = true;
            if(DEBUG)
                println("app@" + id() + ".readWait");
            if(engine.isInboundDone())
                enableAppRead();
            else if(!initialHandshake){
                if(appReadBuffer.hasRemaining())
                    enableAppRead();
                else
                    channelRead();
            }
        }
    }

    private boolean appWriteWait = false;
    private void waitForAppWrite() throws IOException{
        if(!appWriteWait){
            if(outputShutdown)
                throw new IOException("output is shutdown for app@"+id());
            appWriteWait = true;
            if(DEBUG)
                println("app@"+id()+".writeWait");
            if(!initialHandshake){
                if(!appWriteBuffer.hasRemaining())
                    enableAppWrite();
            }
        }
    }

    @Override
    public int interests(){
        int ops = 0;
        if(appReadWait)
            ops |= OP_READ;
        if(appWriteWait)
            ops |= OP_WRITE;
        return ops;
    }

    @Override
    public void addInterest(int interest) throws IOException{
        if(DEBUG)
            println("app@"+id()+".register{");

        validate();

        if(interest==OP_READ)
            waitForAppRead();

        if(interest==OP_WRITE)
            waitForAppWrite();

        if(isAppReady())
            client().nioSelector.ready.add(client());
        if(DEBUG)
            println("}");
    }

    @Override
    public void removeInterest(int operation) throws IOException{
        validate();
        throw new UnsupportedOperationException();
    }

    /*-------------------------------------------------[ Ready ]---------------------------------------------------*/

    private boolean appReadReady = false;
    private void enableAppRead(){
        if(DEBUG)
            println("app@"+id()+".readReady");
        appReadReady = true;
    }

    private boolean appWriteReady = false;
    private void enableAppWrite(){
        if(DEBUG)
            println("app@"+id()+".writeReady");
        appWriteReady = true;
    }

    private void notifyAppWrite() throws IOException{
        if(appWriteWait)
            enableAppWrite();
        else if(outputShutdown && !engine.isOutboundDone())
            closeOutbound();
    }

    @Override
    public int ready(){
        int ops = 0;
        if(appReadReady)
            ops |= OP_READ;
        if(appWriteReady)
            ops |= OP_WRITE;
        return ops;
    }

    private boolean isAppReady(){
        if(appReadReady)
            appReadWait = false;
        if(appWriteReady)
            appWriteWait = false;
        return !appClosed && (appReadReady || appWriteReady);
    }

    /*-------------------------------------------------[ IO ]---------------------------------------------------*/

    private void channelRead() throws IOException{
        while(true){
            netReadBuffer.compact();
            int read = status==BUFFER_UNDERFLOW ? 0 : netReadBuffer.position();
            read += transport.read(netReadBuffer);
            netReadBuffer.flip();
            if(read==0)
                waitForChannelRead();
            else if(read==-1){
                try{
                    engine.closeInbound();
                }catch(SSLException ex){
                    // peer's close_notify is not received yet
                }
                if(appReadWait)
                    enableAppRead();
            }else{
                hsStatus = NEED_UNWRAP;
                if(run()){
                    notifyAppWrite();
                    if(appReadWait){
                        if(appReadBuffer.hasRemaining())
                            enableAppRead();
                        else{
                            continue;
                        }
                    }
                }
            }
            break;
        }
    }

    private void channelWrite() throws IOException{
        if(flush()){
            if(run()){
                notifyAppWrite();
                if(appReadWait){
                    if(appReadBuffer.hasRemaining())
                        enableAppRead();
                }
            }
        }
    }

    private int appRead(ByteBuffer dst) throws IOException{
        validate();

        appReadReady = false;
        if(appReadBuffer.hasRemaining()){
            int limit = Math.min(appReadBuffer.remaining(), dst.remaining());
            if(dst.hasArray()){
                System.arraycopy(appReadBuffer.array(), appReadBuffer.position(), dst.array(), dst.position(), limit);
                appReadBuffer.position(appReadBuffer.position()+limit);
                dst.position(dst.position()+limit);
            }else{
                for(int i=0; i<limit; i++)
                    dst.put(appReadBuffer.get());
            }
            return limit;
        }else
            return engine.isInboundDone() ? -1 : 0;
    }

    private int appWrite(ByteBuffer dst) throws IOException{
        validate();

        appWriteReady = false;
        if(initialHandshake || outputShutdown || netWriteBuffer.hasRemaining())
            return 0;
        int pos = dst.position();
        appWriteBuffer = dst;
        try{
            hsStatus = NEED_WRAP;
            run();
            return dst.position()-pos;
        }finally{
            appWriteBuffer = dummy;
        }
    }

    private IOException asyncException;

    @Override
    public boolean process(){
        if(DEBUG)
            println("app@"+id()+".process{");
        try{
            int ops = transport.interests();
            boolean readReady = (ops&OP_READ)!=0;
            boolean writeReady = (ops&OP_WRITE)!=0;
            if(readReady){
                channelReadWait = false;
                transport.removeInterest(OP_READ);
            }
            if(writeReady){
                channelWriteWait = false;
                transport.removeInterest(OP_WRITE);
            }

            if(readReady)
                channelRead();
            if(transport.isOpen() && writeReady) // to check key validity
                channelWrite();
            if(engine.isInboundDone() && engine.isOutboundDone() && !netWriteBuffer.hasRemaining())
                closeTransport();
        }catch(IOException ex){
            if(DEBUG)
                println("Async Exception: "+ex.getMessage());
            if(appClosed)
                closeTransport();
            else{
                asyncException = ex;
                if(appReadWait)
                    enableAppRead();
                if(appWriteWait)
                    enableAppWrite();
            }
        }
        if(DEBUG)
            println("}");

        if(client().poolFlag>0 && client().key.interestOps()==0)
            client().selector().pool().track(client());

        return isAppReady();
    }

    /*-------------------------------------------------[ Shutdown ]---------------------------------------------------*/

    private boolean outputShutdown;
    @Override
    public void shutdownOutput() throws IOException{
        if(DEBUG)
            println("app@"+id()+".shutdownOutput{");
        outputShutdown = true;
        appWriteWait = appWriteReady = false;
        if(!netWriteBuffer.hasRemaining() && (engine.isInboundDone() || hsStatus==FINISHED || hsStatus==NOT_HANDSHAKING))
            closeOutbound();
        if(DEBUG)
            println("}");
    }

    private void closeOutbound() throws IOException{
        if(DEBUG)
            println("app@"+id()+".closeOutbound");
        engine.closeOutbound();
        hsStatus = NEED_WRAP;
        channelWrite();
    }

    @Override
    public boolean isOutputShutdown(){
        return outputShutdown;
    }

    /*-------------------------------------------------[ Close ]---------------------------------------------------*/

    private boolean appClosed;

    @Override
    public boolean isOpen(){
        return !appClosed;
    }

    @Override
    public void close() throws IOException{
        if(!appClosed){
            appClosed = true;
            if(DEBUG)
                println("app@"+id()+".close{");
            if(asyncException!=null)
                closeTransport();
            else{
                if(appReadBuffer.hasRemaining()){
                    appReadBuffer.position(appReadBuffer.limit());
                    if(DEBUG)
                        println("discarding data in appReadBuffer@"+id());
                }
                if(!outputShutdown)
                    shutdownOutput();
            }
            if(DEBUG)
                println("}");
        }
    }

    private void closeTransport(){
        try{
            transport.close();
        }catch(IOException ignore){
            // ignore
        }
    }
}
