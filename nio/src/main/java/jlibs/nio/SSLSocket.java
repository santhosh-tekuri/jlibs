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

import jlibs.nio.http.expr.Bean;
import jlibs.nio.http.expr.UnresolvedException;
import jlibs.nio.http.expr.ValueMap;
import jlibs.nio.util.NIOUtil;

import javax.net.ssl.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.cert.X509Certificate;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static javax.net.ssl.SSLEngineResult.Status.*;
import static jlibs.nio.Debugger.IO;
import static jlibs.nio.Debugger.println;
import static jlibs.nio.SSLSocket.Action.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class SSLSocket implements Transport, Bean{
    private static final ByteBuffer dummy = ByteBuffer.allocate(0);

    private final Input peerIn;
    private final Output peerOut;
    private Socket transportIn, transportOut;
    private SSLEngine engine;

    private int selfInterestOps;
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

    public SSLSocket(Input in, Output out, SSLEngine engine) throws SSLException{
        peerIn = in;
        transportIn = peerIn.channel().transport;
        transportIn.peekIn = this;

        peerOut = out;
        transportOut = peerOut.channel().transport;
        transportOut.peekOut = this;

        this.engine = engine;

        SSLSession session = engine.getSession();

        appReadBufferSize = session.getApplicationBufferSize();
        appReadBuffer =  ByteBuffer.allocate(appReadBufferSize);
        appReadBuffer.position(appReadBuffer.limit());
        appReadBuffers[1] = appReadBuffer;

        socketWriteBuffer = ByteBuffer.allocate(session.getPacketBufferSize());
        socketWriteBuffer.position(socketWriteBuffer.limit());

        if(IO){
            println("useClientMode: "+engine.getUseClientMode()+
                    " applicationBufferSize: "+session.getApplicationBufferSize()+
                    " packetBufferSize: "+session.getPacketBufferSize());
        }

        engine.beginHandshake();
        if(IO)
            println("beginHandshake");
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
        if(IO)
            println("action="+this.action);
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
        if(IO){
            println(String.format(
                    "RESULT: %5d %5d %-16s %-15s",
                    result.bytesConsumed(), result.bytesProduced(),
                    result.getStatus(), result.getHandshakeStatus()
            ));
        }
    }

    /*-------------------------------------------------[ Misc ]---------------------------------------------------*/


    private boolean readReady(){
        if(peerClosed)
            return true;
        if(initialHandshake)
            return false;
        if(appReadBuffer.hasRemaining() || engine.isInboundDone())
            return true;
        else if(socketReadBuffer!=null && socketReadBuffer.hasRemaining() && unwrapStatus!=BUFFER_UNDERFLOW)
            return true;
        return false;
    }

    private boolean writeReady(){
        if(peerClosed)
            return true;
        if(initialHandshake)
            return false;
        if(engine.isOutboundDone())
            return true;
        return false;
    }

    /*-------------------------------------------------[ Run ]---------------------------------------------------*/

    private boolean peerClosed;
    private void run() throws IOException{
        if(IO)
            println("run{");
        boolean readFromPeer = false;

        while(!peerClosed && (!engine.isInboundDone() || !engine.isOutboundDone() || action!=NOT_HANDSHAKING)){
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
                            if(IO)
                                println("allocating: socketWriteBuffer");
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
                        peerOut.write(socketWriteBuffer);
                        if(socketWriteBuffer.hasRemaining()){
                            selfInterestOps |= OP_WRITE;
                            if(IO)
                                println("writeInterested");
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
                            if(IO)
                                println("allocating: socketReadBuffer");
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
                        int read = peerIn.read(socketReadBuffer);
                        readFromPeer = true;
                        socketReadBuffer.flip();
                        if(read==0){
                            if(initialHandshake || !isOpen()){
                                selfInterestOps |= OP_READ;
                                if(IO)
                                    println("readInterested");
                            }
                            return;
                        }else if(read==-1){
                            if(IO)
                                println("peerClosed");
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
                                    if(IO)
                                        println("readInterested");
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
                                    if(IO)
                                        println("draining appReadBuffer");
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
                        if(IO)
                            println("closeOutbound");
                        engine.closeOutbound();
                        setAction(null);
                    }else
                        return;
                    break;
                }
            }
        }

        if(!open){
            try{
                peerIn.close();
            }finally{
                peerOut.close();
            }
        }
    }

    /*-------------------------------------------------[ ReadyOps ]---------------------------------------------------*/

    private void process() throws IOException{
        selfInterestOps = 0;
        if(action==NEED_READ || action==NEED_WRITE || action==NEED_WRAP){
            run();
            if(IO)
                println("}");
        }
    }

    private boolean canAppRead() throws IOException{
        process();
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

    private boolean eof;

    @Override
    public boolean eof(){
        return eof;
    }

    @Override
    public long available(){
        return appReadBuffer.remaining();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException{
        if(IO)
            println("SSLSocket.read(dst){");
        int read = 0;
        if(canAppRead()){
            if(appReadBuffer.hasRemaining())
                read = NIOUtil.copy(appReadBuffer, dst);

            if(dst.hasRemaining() && !engine.isInboundDone() && !peerClosed){
                appRead = 0;
                appReadBuffers[--appReadBuffersOffset] = dst;
                try{
                    do{
                        setAction(NEED_USER_READ);
                        run();
                        if(IO)
                            println("}");
                    }while(appRead==0 && readReady() && !engine.isInboundDone() && !peerClosed);
                }finally{
                    appReadBuffers[appReadBuffersOffset++] = null;
                }
                if(action==NEED_USER_READ)
                    setAction(null);
                read = (int)appRead-appReadBuffer.remaining();
            }

            if(read==0 && (engine.isInboundDone() || peerClosed)){
                eof = true;
                read = -1;
            }
            assert selfInterestOps==0;
        }
        if(IO){
            println("return "+read);
            println("}");
        }
        return read;
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException{
        return read(dsts, 0, dsts.length);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException{
        if(IO)
            println("SSLSocket.read(dsts){");
        long read = 0;
        if(canAppRead()){
            if(appReadBuffer.hasRemaining())
                read = NIOUtil.copy(appReadBuffer, dsts, offset, length);

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
                        if(IO)
                            println("}");
                    }while(appRead==0 && readReady() && !engine.isInboundDone() && !peerClosed);
                }finally{
                    while(appReadBuffersOffset!=appReadBuffers.length-1)
                        appReadBuffers[appReadBuffersOffset++] = null;
                }
                if(action==NEED_USER_READ)
                    setAction(null);
                read = appRead-appReadBuffer.remaining();
            }

            if(read==0 && (engine.isInboundDone() || peerClosed)){
                eof = true;
                read = -1;
            }
            assert selfInterestOps==0;
        }
        if(IO){
            println("return "+read);
            println("}");
        }
        return read;
    }

    @Override
    public long transferTo(long position, long count, FileChannel target) throws IOException{
        if(canAppRead() && appReadBuffer.hasRemaining())
            return NIOUtil.transfer(appReadBuffer, target, position, count);
        return target.transferFrom(this, position, count);
    }

    /*-------------------------------------------------[ App Write ]---------------------------------------------------*/

    private long appWrite() throws IOException{
        try{
            appWrote = 0;
            setAction(NEED_WRAP);
            run();
            if(IO)
                println("}");
//                if(selfInterestOps!=0)
//                    addPeerInterests(selfInterestOps);
            return appWrote;
        }finally{
            appWriteBuffers = appWriteBuffers1;
            appWriteBuffers[0] = dummy;
            appWriteBufferOffset = 0;
            appWriteBufferLength = 1;
        }
    }

    @Override
    public int write(ByteBuffer dst) throws IOException{
        if(IO)
            println("SSLSocket.write(dst){");
        int wrote = 0;
        if(canAppWrite()){
            appWriteBuffers[0] = dst;
            wrote = (int)appWrite();
        }
        if(IO){
            println("wrote = "+wrote);
            println("}");
        }
        return wrote;
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException{
        return write(srcs, 0, srcs.length);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException{
        if(IO)
            println("SSLSocket.write(dsts){");
        long wrote = 0;
        if(canAppWrite()){
            appWriteBuffers = srcs;
            appWriteBufferOffset = offset;
            appWriteBufferLength = length;
            wrote = appWrite();
        }
        if(IO){
            println("wrote = "+wrote);
            println("}");
        }
        return wrote;
    }

    @Override
    public long transferFrom(FileChannel src, long position, long count) throws IOException{
        return src.transferTo(position, count, this);
    }

    /*-------------------------------------------------[ Close ]---------------------------------------------------*/

    private boolean open = true;

    @Override
    public boolean isOpen(){
        return open;
    }

    @Override
    public void close() throws IOException{
        if(open){
            open = false;
            if(appReadBuffer.hasRemaining()){
                if(IO)
                    println("draining appReadBuffer");
                appReadBuffer.position(appReadBuffer.limit());
            }
            run();
            if(IO)
                println("}");
//            if(selfInterestOps!=0)
//                addPeerInterests(selfInterestOps);
        }
    }

    /*-------------------------------------------------[ Transport-Misc ]---------------------------------------------------*/

    @Override
    public Input.Listener getInputListener(){
        return transportIn.getInputListener();
    }

    @Override
    public void setInputListener(Input.Listener listener){
        transportIn.setInputListener(listener);
    }

    @Override
    public Output.Listener getOutputListener(){
        return transportOut.getOutputListener();
    }

    @Override
    public void setOutputListener(Output.Listener listener){
        transportOut.setOutputListener(listener);
    }

    @Override
    public void wakeupReader(){
        transportIn.wakeupReader();
    }

    @Override
    public void wakeupWriter(){
        transportOut.wakeupWriter();
    }

    @Override
    public Input detachInput(){
        return this;
    }

    @Override
    public Output detachOutput(){
        return this;
    }

    @Override
    public NBStream channel(){
        return transportIn.channel(); //todo
    }

    @Override
    public boolean flush() throws IOException{
        process();
        return (selfInterestOps&OP_WRITE)==0 && peerOut.flush();
    }

    @Override
    public void addReadInterest(){
        if(transportIn.peekIn==this)
            transportIn.peekInInterested = true;
        if(readReady())
            transportIn.wakeupReader();
        else{
            if(selfInterestOps==0)
                peerIn.addReadInterest();
            else{
                if((selfInterestOps&OP_READ)!=0)
                    peerIn.addReadInterest();
                if((selfInterestOps&OP_WRITE)!=0)
                    peerOut.addWriteInterest();
            }
        }
    }

    @Override
    public void addWriteInterest(){
        if(transportOut.peekOut==this)
            transportOut.peekOutInterested = true;
        if(writeReady())
            transportOut.wakeupWriter();
        else{
            if(selfInterestOps==0)
                peerOut.addWriteInterest();
            else{
                if((selfInterestOps&OP_READ)!=0)
                    peerIn.addReadInterest();
                if((selfInterestOps&OP_WRITE)!=0)
                    peerOut.addWriteInterest();
            }
        }
    }

    public SSLSession getSession(){
        return engine.getSession();
    }

    @Override
    @SuppressWarnings("StringEquality")
    public Object getField(String name) throws UnresolvedException{
        if(name=="protocol")
            return getSession().getProtocol();
        else if(name=="cipher")
            return getSession().getCipherSuite();
        else if(name=="local")
            return new CertificateBean((X509Certificate)getSession().getLocalCertificates()[0]);
        else if(name=="peer"){
            try{
                return new CertificateBean((X509Certificate)getSession().getPeerCertificates()[0]);
            }catch(SSLPeerUnverifiedException ex){
                return null;
            }
        }else
            throw new UnresolvedException(name);
    }

    public static class CertificateBean implements Bean{
        public final X509Certificate cert;
        public CertificateBean(X509Certificate cert){
            this.cert = cert;
        }

        @Override
        @SuppressWarnings("StringEquality")
        public Object getField(String name) throws UnresolvedException{
            if(name=="sdn")
                return new DistinguishedName(cert.getSubjectX500Principal().getName());
            else if(name=="idn")
                return new DistinguishedName(cert.getIssuerX500Principal().getName());
            return null;
        }

        @Override
        public String toString(){
            return cert.toString();
        }
    }

    public static class DistinguishedName implements ValueMap{
        public final String dn;
        public DistinguishedName(String dn){
            this.dn = dn;
        }

        @Override
        public Object getValue(String name){
            for(String attr: dn.split(",")){
                String str[] = attr.split("=");
                if(str[0].equals(name))
                    return str[1];
            }
            return null;
        }

        @Override
        public String toString(){
            return dn;
        }
    }
}
