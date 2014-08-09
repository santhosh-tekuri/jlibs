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

import jlibs.nio.Debugger;
import jlibs.nio.channels.impl.filters.AbstractIOFilterChannel;
import jlibs.nio.util.NIOUtil;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.nio.ByteBuffer;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static javax.net.ssl.SSLEngineResult.Status.*;
import static jlibs.nio.channels.filters.SSLFilter.Action.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class SSLFilter extends AbstractIOFilterChannel{
    private static final ByteBuffer dummy = ByteBuffer.allocate(0);

    private SSLEngine engine;

    private ByteBuffer socketReadBuffer;
    private ByteBuffer socketWriteBuffer;

    private int appReadBuffersOffset = 1;
    private ByteBuffer appReadBuffers[] = new ByteBuffer[2];
    private ByteBuffer appReadBuffer;
    private int appReadBufferSize;

    private int appWriteBufferOffset = 0;
    private int appWriteBufferLength = 1;
    private ByteBuffer appWriteBuffers[] = new ByteBuffer[]{ dummy };
    private final ByteBuffer appWriteBuffers1[] = appWriteBuffers;
    private void moveAppWritten(){
        while(appWriteBufferLength!=0){
            if(!appWriteBuffers[appWriteBufferOffset].hasRemaining()){
                ++appWriteBufferOffset;
                --appWriteBufferLength;
            }else
                break;
        }
    }

    public SSLFilter(SSLEngine engine) throws IOException{
        this.engine = engine;

        SSLSession session = engine.getSession();

        appReadBufferSize = session.getApplicationBufferSize();
        appReadBuffer =  ByteBuffer.allocate(appReadBufferSize);
        appReadBuffer.position(appReadBuffer.limit());
        appReadBuffers[1] = appReadBuffer;

        socketWriteBuffer = ByteBuffer.allocate(session.getPacketBufferSize());
        socketWriteBuffer.position(socketWriteBuffer.limit());

        if(Debugger.IO){
            Debugger.println("useClientMode: "+engine.getUseClientMode()+
                    " applicationBufferSize: "+session.getApplicationBufferSize()+
                    " packetBufferSize: "+session.getPacketBufferSize());
        }

        engine.beginHandshake();
        if(Debugger.IO)
            Debugger.println("beginHandshake");
        setAction(null);
        selfInterestOps = action== NEED_READ ? OP_READ : OP_WRITE;
    }

    /*-------------------------------------------------[ Action ]---------------------------------------------------*/

    enum Action{ NOT_HANDSHAKING, NEED_TASK, NEED_WRAP, NEED_WRITE, NEED_READ, NEED_UNWRAP, NEED_USER_READ }
    private Action action;
    private void setAction(Action action){
        if(action==null){
            switch(engine.getHandshakeStatus()){
                case NOT_HANDSHAKING:
                    action = NOT_HANDSHAKING;
                    break;
                case NEED_TASK:
                    action = NEED_TASK;
                    break;
                case NEED_WRAP:
                    action = NEED_WRAP;
                    break;
                case NEED_UNWRAP:
                    action = NEED_READ;
            }
            assert action!=null;
        }
        this.action = action;
        if(Debugger.IO)
            Debugger.println("action="+this.action);
    }

    /*-------------------------------------------------[ SSLEngineResult ]---------------------------------------------------*/

    private boolean initialHandshake = true;
    private SSLEngineResult.Status status = null;
    private SSLEngineResult.Status unwrapStatus = null;

    private long appRead = 0;
    private long appWrote = 0;
    private void setResult(SSLEngineResult result){
        if(action==NEED_UNWRAP){
            unwrapStatus = result.getStatus();
            appRead += result.bytesProduced();
        }else if(action==NEED_WRAP)
            appWrote += result.bytesConsumed();
        this.status = result.getStatus();
        if(Debugger.IO){
            Debugger.println(String.format(
                    "RESULT: %5d %5d %-16s %-15s",
                    result.bytesConsumed(), result.bytesProduced(),
                    result.getStatus(), result.getHandshakeStatus()
            ));
        }
    }

    /*-------------------------------------------------[ Misc ]---------------------------------------------------*/

    @Override
    protected int _selfReadyOps(){
        if(peerClosed)
            return OP_READ|OP_WRITE;
        if(initialHandshake)
            return 0;

        int ops = 0;
        if(appReadBuffer.hasRemaining() || engine.isInboundDone())
            ops |= OP_READ;
        else if(socketReadBuffer!=null && socketReadBuffer.hasRemaining() && unwrapStatus!=BUFFER_UNDERFLOW)
            ops |= OP_READ;

        if(engine.isOutboundDone())
            ops |= OP_WRITE;

        return ops;
    }

    /*-------------------------------------------------[ Run ]---------------------------------------------------*/

    private boolean peerClosed;
    private void run() throws IOException{
        if(Debugger.IO)
            Debugger.println("run{");
        boolean readFromPeer = false;

        while(!peerClosed && (!engine.isInboundDone() || !engine.isOutboundDone() || action!= NOT_HANDSHAKING)){
            switch(action){
                case NEED_TASK:{
                    Runnable task;
                    while((task=engine.getDelegatedTask())!=null)
                        task.run();
                    setAction(null);
                    break;
                }
                case NEED_WRAP:{
                    // prepare write
                    if(socketWriteBuffer==null){
                        if(socketReadBuffer.hasRemaining()){
                            if(Debugger.IO)
                                Debugger.println("allocating: socketWriteBuffer");
                            socketWriteBuffer = ByteBuffer.allocate(socketReadBuffer.capacity());
                            socketWriteBuffer.position(socketWriteBuffer.limit());
                        }else{
                            socketWriteBuffer = socketReadBuffer;
                            socketReadBuffer = null;
                        }
                    }
                    assert !socketWriteBuffer.hasRemaining();

                    socketWriteBuffer.clear();
                    setResult(engine.wrap(appWriteBuffers, appWriteBufferOffset, appWriteBufferLength, socketWriteBuffer));
                    socketWriteBuffer.flip();
                    if(appWriteBuffers[appWriteBufferOffset]!=dummy)
                        moveAppWritten();
                    assert status==OK || (status==CLOSED && engine.isOutboundDone());

                    setAction(NEED_WRITE);
                    break;
                }
                case NEED_WRITE:{
                    if(socketWriteBuffer.hasRemaining()){
                        peerOutput.write(socketWriteBuffer);
                        if(socketWriteBuffer.hasRemaining()){
                            selfInterestOps |= OP_WRITE;
                            if(Debugger.IO)
                                Debugger.println("writeInterested");
                            return;
                        }
                    }
                    setAction(null);
                    break;
                }
                case NEED_USER_READ:
                case NEED_READ:{
                    assert !appReadBuffer.hasRemaining();

                    // prepare read
                    if(socketReadBuffer==null){
                        if(socketWriteBuffer.hasRemaining()){
                            if(Debugger.IO)
                                Debugger.println("allocating: socketReadBuffer");
                            socketReadBuffer = ByteBuffer.allocate(socketWriteBuffer.capacity());
                            socketReadBuffer.position(socketReadBuffer.limit());
                        }else{
                            socketReadBuffer = socketWriteBuffer;
                            socketWriteBuffer = null;
                        }
                    }

                    readFromPeer = false;
                    if(!socketReadBuffer.hasRemaining() || unwrapStatus==BUFFER_UNDERFLOW){
                        NIOUtil.compact(socketReadBuffer);
                        int read = peerInput.read(socketReadBuffer);
                        readFromPeer = true;
                        socketReadBuffer.flip();
                        if(read==0){
                            if(initialHandshake || !isOpen()){
                                selfInterestOps |= OP_READ;
                                if(Debugger.IO)
                                    Debugger.println("readInterested");
                            }
                            return;
                        }else if(read==-1){
                            if(Debugger.IO)
                                Debugger.println("peerClosed");
                            peerClosed = true;
                            return;
                        }
                    }
                    setAction(engine.isInboundDone()? null : NEED_UNWRAP);
                    break;
                }
                case NEED_UNWRAP:{
                    assert socketReadBuffer.hasRemaining();

                    appReadBuffer.clear();
                    setResult(engine.unwrap(socketReadBuffer, appReadBuffers, appReadBuffersOffset, appReadBuffers.length-appReadBuffersOffset));
                    appReadBuffer.flip();

                    switch(status){
                        case BUFFER_UNDERFLOW: // not enough data in netReadBuffer
                            if(initialHandshake){
                                setAction(NEED_READ);
                                if(readFromPeer){
                                    selfInterestOps |= OP_READ;
                                    if(Debugger.IO)
                                        Debugger.println("readInterested");
                                    return;
                                }
                            }else{
                                if(readFromPeer){
                                    setAction(null);
                                    return;
                                }else
                                    setAction(NEED_USER_READ);
                            }
                            continue;
                        case BUFFER_OVERFLOW: // not enough room in appReadBuffer
                            assert appReadBuffer==dummy;
                            appReadBuffer = ByteBuffer.allocate(appReadBufferSize);
                            appReadBuffer.position(appReadBuffer.limit());
                            appReadBuffers[appReadBuffers.length-1] = appReadBuffer;
                            continue;
                        case CLOSED:
                            assert engine.isInboundDone();
                        case OK:
                            if(appReadBuffer.hasRemaining()){
                                if(!isOpen()){
                                    if(Debugger.IO)
                                        Debugger.println("draining appReadBuffer");
                                    appReadBuffer.position(appReadBuffer.limit());
                                }else{
                                    setAction(null);
                                    return;
                                }
                            }
                            break;
                    }
                    setAction(null);
                    break;
                }
                case NOT_HANDSHAKING:{
                    initialHandshake = false;
                    if(appWriteBufferLength!=0 && appWriteBuffers[appWriteBufferOffset].hasRemaining())
                        setAction(NEED_WRAP);
                    else if(!isOpen() && !engine.isOutboundDone()){
                        if(Debugger.IO)
                            Debugger.println("closeOutbound");
                        engine.closeOutbound();
                        setAction(null);
                    }else
                        return;
                    break;
                }
            }
        }
    }

    /*-------------------------------------------------[ ReadyOps ]---------------------------------------------------*/

    @Override
    protected void _process() throws IOException{
        if(action==NEED_READ || action==NEED_WRITE || action==NEED_WRAP){
            run();
            if(Debugger.IO)
                Debugger.println("}");
        }
    }

    private boolean canAppRead(){
        if(!initialHandshake && selfInterestOps ==0){
            assert action==NOT_HANDSHAKING;
            assert socketWriteBuffer==null || !socketWriteBuffer.hasRemaining();
            return true;
        }else
            return false;
    }

    private boolean canAppWrite() throws IOException{
        if(canAppRead()){
            if(peerClosed || engine.isOutboundDone())
                throw new SSLException("peer closed");
            return true;
        }else
            return false;
    }

    /*-------------------------------------------------[ App Read ]---------------------------------------------------*/

    @Override
    public long available(){
        return appReadBuffer.remaining();
    }

    private int copyFromAppReadBuffer(ByteBuffer dst){
        int appReadBufferLimit = appReadBuffer.limit();
        int read = Math.min(appReadBuffer.remaining(), dst.remaining());
        appReadBuffer.limit(appReadBuffer.position()+read);
        dst.put(appReadBuffer);
        appReadBuffer.limit(appReadBufferLimit);
        return read;
    }

    @Override
    protected int _read(ByteBuffer dst) throws IOException{
        int read = 0;
        if(canAppRead()){
            if(appReadBuffer.hasRemaining())
                read = copyFromAppReadBuffer(dst);

            if(read==0 && dst.hasRemaining() && !engine.isInboundDone() && !peerClosed){
                appRead = 0;
                appReadBuffers[--appReadBuffersOffset] = dst;
                try{
                    do{
                        setAction(NEED_USER_READ);
                        run();
                        if(Debugger.IO)
                            Debugger.println("}");
                    }while(appRead==0 && (_selfReadyOps()&OP_READ)!=0 && !engine.isInboundDone() && !peerClosed);
                }finally{
                    appReadBuffers[appReadBuffersOffset++] = null;
                }
                if(action==NEED_USER_READ)
                    setAction(null);
                read = (int)appRead-appReadBuffer.remaining();
            }

            if(read==0 && (engine.isInboundDone() || peerClosed))
                read = -1;
            assert selfInterestOps==0;
        }
        return read;
    }

    @Override
    protected long _read(ByteBuffer[] dsts, int offset, int length) throws IOException{
        long read = 0;
        if(canAppRead()){
            while(appReadBuffer.hasRemaining() && length!=0){
                read += copyFromAppReadBuffer(dsts[offset]);
                if(!dsts[offset].hasRemaining()){
                    ++offset;
                    --length;
                }
            }

            if(read==0 && length!=0 && !engine.isInboundDone() && !peerClosed){
                appRead = 0;
                if(appReadBuffers.length<length+1){
                    appReadBuffers = new ByteBuffer[length+1];
                    appReadBuffersOffset = appReadBuffers.length-1;
                    appReadBuffers[appReadBuffersOffset] = appReadBuffer;
                }
                for(int i=offset+length-1; i>=offset; i--){
                    if(dsts[i].hasRemaining())
                        appReadBuffers[--appReadBuffersOffset] = dsts[i];
                }
                try{
                    do{
                        setAction(NEED_USER_READ);
                        run();
                        if(Debugger.IO)
                            Debugger.println("}");
                    }while(appRead==0 && (_selfReadyOps()&OP_READ)!=0 && !engine.isInboundDone() && !peerClosed);
                }finally{
                    while(appReadBuffersOffset!=appReadBuffers.length-1)
                        appReadBuffers[appReadBuffersOffset++] = null;
                }
                if(action==NEED_USER_READ)
                    setAction(null);
                read = appRead-appReadBuffer.remaining();
            }

            if(read==0 && (engine.isInboundDone() || peerClosed))
                read = -1;
            assert selfInterestOps==0;
        }
        return read;
    }

    /*-------------------------------------------------[ App Write ]---------------------------------------------------*/

    private long appWrite() throws IOException{
        try{
            appWrote = 0;
            if(canAppWrite()){
                setAction(NEED_WRAP);
                run();
                if(Debugger.IO)
                    Debugger.println("}");
                if(selfInterestOps!=0)
                    addPeerInterests(selfInterestOps);
            }
            return appWrote;
        }finally{
            appWriteBuffers = appWriteBuffers1;
            appWriteBuffers[0] = dummy;
            appWriteBufferOffset = 0;
            appWriteBufferLength = 1;
        }
    }

    @Override
    protected int _write(ByteBuffer dst) throws IOException{
        appWriteBuffers[0] = dst;
        return (int)appWrite();
    }

    @Override
    protected long _write(ByteBuffer[] srcs, int offset, int length) throws IOException{
        appWriteBuffers = srcs;
        appWriteBufferOffset = offset;
        appWriteBufferLength = length;
        return appWrite();
    }

    /*-------------------------------------------------[ Close ]---------------------------------------------------*/

    protected void _close() throws IOException{
        if(appReadBuffer.hasRemaining()){
            if(Debugger.IO)
                Debugger.println("draining appReadBuffer");
            appReadBuffer.position(appReadBuffer.limit());
        }
        run();
        if(Debugger.IO)
            Debugger.println("}");
        if(selfInterestOps!=0)
            addPeerInterests(selfInterestOps);
    }
}
