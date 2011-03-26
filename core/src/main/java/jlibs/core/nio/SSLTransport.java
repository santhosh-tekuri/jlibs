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
import jlibs.core.lang.NotImplementedException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.NEED_WRAP;

/**
 * @author Santhosh Kumar T
 */
public final class SSLTransport extends Transport{
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
        transport.parent = this;

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
        hsStatus = engine.getHandshakeStatus();
        run();

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
                    case NOT_HANDSHAKING:
                    case FINISHED:
                        if(hsStatus== SSLEngineResult.HandshakeStatus.FINISHED && handshakeCompletedListener!=null)
                            handshakeCompletedListener.handshakeCompleted(new HandshakeCompletedEvent(client(), engine.getSession()));
                        boolean wasInitialHandShake = initialHandshake;
                        initialHandshake = false;
                        if(shutdownOutputRequested && !engine.isOutboundDone()){
                            if(DEBUG)
                                println("app@"+id()+".closeOutbound");
                            engine.closeOutbound();
                            hsStatus = NEED_WRAP;
                            continue;
                        }
                        if(wasInitialHandShake && appReadWait && !appReadReady()){
                            hsStatus = NEED_UNWRAP;
                            continue;
                        }
                        return true;
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
                "%-6s: %5d %5d %-16s %-15s",
                "TASK",
                0, 0,
                "OK", hsStatus
            ));
        }
    }

    private boolean eofRead;
    private boolean processUnwrap() throws IOException{
        assert !appReadBuffer.hasRemaining();

        if(!netReadBuffer.hasRemaining() || status==SSLEngineResult.Status.BUFFER_UNDERFLOW){
            netReadBuffer.compact();
            int read = transport.read(netReadBuffer);
            netReadBuffer.flip();
            if(read==0){
                transport.addInterest(OP_READ);
                return false;
            }else if(read==-1){
                eofRead = true;
                try{
                    engine.closeInbound();
                }catch(SSLException ex){
                    // peer's close_notify is not received yet
                }
                status = SSLEngineResult.Status.CLOSED;
                hsStatus = engine.getHandshakeStatus();
                return false;
            }
        }

        assert netReadBuffer.hasRemaining();
        appReadBuffer.clear();
        SSLEngineResult result = engine.unwrap(netReadBuffer, appReadBuffer);
        appReadBuffer.flip();
        hsStatus = result.getHandshakeStatus();
        status = result.getStatus();

        if(DEBUG){
            println(String.format(
                "%-6s: %5d %5d %-16s %-15s",
                "UNWRAP",
                result.bytesConsumed(), result.bytesProduced(),
                result.getStatus(), result.getHandshakeStatus()
            ));
        }

        switch(status){
            case BUFFER_UNDERFLOW: // not enough data in netReadBuffer
                transport.addInterest(OP_READ);
                return false;
            case BUFFER_OVERFLOW: // not enough room in appReadBuffer
                throw new ImpossibleException();
            case OK:
                if(appReadBuffer.hasRemaining()){
                    if(appClosed){
                        if(DEBUG)
                            println("discarding data in appReadBuffer@"+id());
                        appReadBuffer.position(appReadBuffer.limit());
                        return true;
                    }
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
                return false;
        }
        throw new ImpossibleException();
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
                "%-6s: %5d %5d %-16s %-15s",
                "WRAP",
                result.bytesConsumed(), result.bytesProduced(),
                status, hsStatus
            ));
        }

        assert status!=SSLEngineResult.Status.BUFFER_UNDERFLOW; // Nothing to wrap: no data was present in appWriteBuffer
        assert status!=SSLEngineResult.Status.BUFFER_OVERFLOW; // not enough room in netWriteBuffer
        assert status!=SSLEngineResult.Status.CLOSED || engine.isOutboundDone();

        return flush();
    }

    private boolean flush() throws IOException{
        if(netWriteBuffer.hasRemaining()){
            transport.write(netWriteBuffer);
            if(netWriteBuffer.hasRemaining()){
                transport.addInterest(OP_WRITE);
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

    private IOException asyncException;


    private void validate() throws IOException{
        if(asyncException!=null)
            throw new IOException("ASYNC_EXCEPTION: "+asyncException.getMessage(), asyncException);
        if(appClosed)
            throw new ClosedChannelException();
    }

    /*-------------------------------------------------[ interests ]---------------------------------------------------*/

    private boolean appReadWait;
    private boolean appWriteWait;

    @Override
    public int interests(){
        int ops = 0;
        if(appReadWait)
            ops |= OP_READ;
        if(appWriteWait)
            ops |= OP_WRITE;
        return ops;
    }

    private boolean addedToReadyList;
    @Override
    public void addInterest(int operation) throws IOException{
        if(DEBUG)
            println("app@"+id()+"."+(operation==OP_READ?"read":"write")+"Wait{");
        validate();
        if(!appReadWait && operation==OP_READ){
            appReadWait = true;
            if(!initialHandshake && !appReadReady()){
                hsStatus = SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
                run();
            }
        }
        if(!appWriteWait && operation==OP_WRITE){
            if(isOutputShutdown())
                throw new IOException("output is shutdown for app@"+id());
            appWriteWait = true;
        }
        if(!addedToReadyList && appReady()){
            client().nioSelector.ready.add(this);
            addedToReadyList = true;
        }
        if(DEBUG)
            println("}");
    }

    @Override
    public void removeInterest(int operation) throws IOException{
        validate();
        throw new NotImplementedException();
    }

    /*-------------------------------------------------[ ready ]---------------------------------------------------*/

    private Boolean appReadReady(){
        return !appClosed && (asyncException!=null || engine.isInboundDone() || appReadBuffer.hasRemaining());
    }

    private boolean appWriteReady(){
        return !appClosed && (asyncException!=null || (!initialHandshake && !isOutputShutdown() && !netWriteBuffer.hasRemaining()));
    }

    private boolean appReady(){
        return (appReadWait && appReadReady()) || (appWriteWait && appWriteReady());
    }

    @Override
    public int ready(){
        int ops = appReadReady ? OP_READ : 0;
        return appWriteReady ? ops|OP_WRITE : ops;
    }

    private boolean appReadReady, appWriteReady;

    @Override
    public boolean updateReadyInterests(){
        addedToReadyList = false;
        if(appReadWait){
            if(appReadReady=appReadReady())
                appReadWait = false;
        }else
            appReadReady = false;
        if(appWriteWait){
            if(appWriteReady=appWriteReady())
                appWriteWait = false;
        }else
            appWriteReady = false;

        if(DEBUG){
            if(appReadReady || appWriteReady)
                println("app@"+id()+"."+(appReadReady?"Read":"")+(appWriteReady?"Write":"")+"Ready");
        }
        return appReadReady || appWriteReady;
    }

    /*-------------------------------------------------[ IO ]---------------------------------------------------*/

    @Override
    public boolean process(){
        if(DEBUG)
            println("app@"+id()+".process{");
        try{
            int ops = transport.interests();
            boolean readReady = (ops&OP_READ)!=0;
            boolean writeReady = (ops&OP_WRITE)!=0;
            if(readReady)
                transport.removeInterest(OP_READ);
            if(writeReady)
                transport.removeInterest(OP_WRITE);

            if(writeReady){ // to check key validity
                hsStatus = SSLEngineResult.HandshakeStatus.NEED_WRAP;
                run();
            }
            if(readReady){
                hsStatus = SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
                run();
            }
        }catch(IOException ex){
            if(DEBUG)
                println("Async Exception: "+ex.getMessage());
            if(appClosed)
                closeTransport();
            else
                asyncException = ex;
        }
        if(DEBUG)
            println("}");

        if(client().poolFlag>0 && client().key.interestOps()==0)
            client().selector().pool().track(client());

        return updateReadyInterests();
    }

    public int read(ByteBuffer dst) throws IOException{
        if(DEBUG)
            println("app@"+id()+".read{");

        validate();
        int read;
        try{
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
                read = limit;
            }else
                read = engine.isInboundDone() ? -1 : 0;
            if(DEBUG)
                println("return: "+read);
            return read;
        }finally{
            if(DEBUG)
                println("}");
        }
    }

    public int write(ByteBuffer dst) throws IOException{
        if(DEBUG)
            println("app@"+id()+".write{");

        validate();
        if(isOutputShutdown())
            throw new IOException("output is shutdown for app@"+id());

        int pos = dst.position();
        try{
            if(!initialHandshake && !netWriteBuffer.hasRemaining()){
                appWriteBuffer = dst;
                hsStatus = NEED_WRAP;
                run();
            }
            if(DEBUG)
                println("return: "+(dst.position()-pos));
            return dst.position()-pos;
        }finally{
            appWriteBuffer = dummy;
            if(DEBUG)
                println("}");
        }
    }

    /*-------------------------------------------------[ shutdown-output ]---------------------------------------------------*/

    private boolean shutdownOutputRequested;

    @Override
    public void shutdownOutput() throws IOException{
        if(appClosed)
            throw new ClosedChannelException();
        if(!isOutputShutdown())
            _shutdownOutput();
    }

    private void _shutdownOutput() throws IOException{
        if(DEBUG)
            println("app@"+id()+".shutdownOutput{");
        appWriteWait = appWriteReady = false;

        boolean appClosed = this.appClosed;
        this.appClosed = false;
        boolean canShutdown = appWriteReady();
        this.appClosed = appClosed;
        if(canShutdown){
            if(DEBUG)
                println("app@"+id()+".closeOutbound");
            engine.closeOutbound();
            hsStatus = NEED_WRAP;
            run();
        }else
            shutdownOutputRequested = true;
        if(DEBUG)
            println("}");
    }

    @Override
    public boolean isOutputShutdown(){
        return shutdownOutputRequested || engine.isOutboundDone();
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
            appReadWait = appWriteWait = false;
            appClosed = true;
            if(DEBUG)
                println("app@"+id()+".close{");
            if(eofRead || asyncException!=null)
                closeTransport();
            else{
                if(appReadBuffer.hasRemaining()){
                    appReadBuffer.position(appReadBuffer.limit());
                    if(DEBUG)
                        println("discarding data in appReadBuffer@"+id());
                }
                if(!isOutputShutdown())
                    _shutdownOutput();
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
