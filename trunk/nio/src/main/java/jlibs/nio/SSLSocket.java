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
import jlibs.nio.util.Buffers;
import jlibs.nio.util.NIOUtil;

import javax.net.ssl.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.security.cert.X509Certificate;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.*;
import static javax.net.ssl.SSLEngineResult.Status.*;
import static jlibs.nio.Debugger.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class SSLSocket implements Transport, Bean{
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);

    private final Input peerIn;
    private final Output peerOut;
    private final SSLEngine engine;

    private final int packetBufferSize;
    private ByteBuffer peerReadBuffer;
    private ByteBuffer peerWriteBuffer;

    private ByteBuffer appReadBuffers[];
    private int appReadBuffersOffset;

    private final ByteBuffer buffersArray1[] = { EMPTY_BUFFER };
    private final Buffers appWriteBuffers = new Buffers(buffersArray1, 0, 1);

    public SSLSocket(Input in, Output out, SSLEngine engine) throws IOException{
        peerIn = in;
        transportIn = peerIn.channel().transport;
        transportIn.peekIn = this;

        peerOut = out;
        transportOut = peerOut.channel().transport;
        transportOut.peekOut = this;

        this.engine = engine;
        SSLSession session = engine.getSession();

        packetBufferSize = session.getPacketBufferSize();
        peerWriteBuffer = Reactor.current().allocator.allocate(2*packetBufferSize);
        peerWriteBuffer.position(peerWriteBuffer.limit());

        ByteBuffer appReadBuffer =  Reactor.current().allocator.allocate(session.getApplicationBufferSize());
        appReadBuffer.position(appReadBuffer.limit());
        appReadBuffers = new ByteBuffer[]{ null, appReadBuffer };
        appReadBuffersOffset = 1;

        if(IO){
            println("useClientMode: "+engine.getUseClientMode()+
                    " applicationBufferSize: "+session.getApplicationBufferSize()+
                    " packetBufferSize: "+session.getPacketBufferSize() +
                    " handshakeStatus: "+engine.getHandshakeStatus());
        }
        engine.beginHandshake();
        selfInterests = engine.getHandshakeStatus()==NEED_UNWRAP ? OP_READ : OP_WRITE;
    }

    public SSLSession getSession(){
        return engine.getSession();
    }

    private int selfInterests;
    private long appWrote, appRead;
    private boolean unwrapUnderflow;
    private void run(SSLEngineResult.HandshakeStatus handshakeStatus) throws IOException{
        assert handshakeStatus==engine.getHandshakeStatus() || engine.getHandshakeStatus()==NOT_HANDSHAKING;
        selfInterests = 0;
        appRead = appWrote = 0;
        while(!engine.isOutboundDone()){
            if(IO)
                println(handshakeStatus+".run()");
            switch(handshakeStatus){
                case NEED_TASK:
                    Runnable task;
                    while((task=engine.getDelegatedTask())!=null)
                        task.run();
                    break;
                case NEED_WRAP:
                    if(peerWriteBuffer==null){
                        if(peerReadBuffer.hasRemaining()){
                            peerWriteBuffer = Reactor.current().allocator.allocate(2*packetBufferSize);
                            peerWriteBuffer.position(peerWriteBuffer.limit());
                        }else{
                            peerWriteBuffer = peerReadBuffer;
                            peerReadBuffer = null;
                        }
                    }else if(peerWriteBuffer.hasRemaining() && (peerWriteBuffer.capacity()-peerWriteBuffer.limit())<packetBufferSize){
                        do{
                            if(peerOut.write(peerWriteBuffer)==0){
                                selfInterests |= OP_WRITE;
                                return;
                            }
                        }while(peerWriteBuffer.hasRemaining());
                    }

                    if(peerWriteBuffer.hasRemaining()){
                        peerWriteBuffer.position(peerWriteBuffer.limit());
                        peerWriteBuffer.limit(peerWriteBuffer.capacity());
                    }else
                        peerWriteBuffer.clear();
                    try{
                        SSLEngineResult result = engine.wrap(appWriteBuffers.array, appWriteBuffers.offset, appWriteBuffers.length, peerWriteBuffer);
                        if(IO) println(result);
                        assert result.getStatus()!=BUFFER_UNDERFLOW;
                        assert result.getStatus()==OK || (result.getStatus()==CLOSED && engine.isOutboundDone());
                        appWrote += result.bytesConsumed();
                    }finally{
                        peerWriteBuffer.flip();
                    }
                    break;
                case NEED_UNWRAP:
                    if(!writePendingToPeer())
                        return;
                    if(peerReadBuffer==null){
                        peerReadBuffer = peerWriteBuffer;
                        peerWriteBuffer = null;
                    }
                    while(true){
                        if(!peerReadBuffer.hasRemaining() || unwrapUnderflow){
                            NIOUtil.compact(peerReadBuffer);
                            try{
                                int read = peerIn.read(peerReadBuffer);
                                if(read==0){
                                    selfInterests |= OP_READ;
                                    return;
                                }else if(read==-1){
                                    try{
                                        engine.closeInbound();
                                    }catch(SSLException ignore){
                                        // ignore.printStackTrace();
                                    }
                                    break;
                                }else
                                    unwrapUnderflow = false;
                            }finally{
                                peerReadBuffer.flip();
                            }
                        }
                        ByteBuffer appReadBuffer = appReadBuffers[appReadBuffers.length-1];
                        if(appReadBuffer.hasRemaining())
                            return;
                        appReadBuffer.clear();
                        try{
                            SSLEngineResult result = engine.unwrap(peerReadBuffer, appReadBuffers, appReadBuffersOffset, appReadBuffers.length-appReadBuffersOffset);
                            if(IO) println(result);
                            if(result.getStatus()==BUFFER_UNDERFLOW)
                                unwrapUnderflow = true;
                            else{
                                assert result.getStatus()!=BUFFER_OVERFLOW;
                                assert result.getStatus()==OK || (result.getStatus()==CLOSED && engine.isInboundDone());
                                appRead += result.bytesProduced();
                                if(appRead>0){
                                    if(isOpen())
                                        return;
                                    else
                                        appReadBuffer.position(appReadBuffer.limit());
                                }
                                break;
                            }
                        }finally{
                            appReadBuffer.flip();
                        }
                    }
                    break;
                case FINISHED:
                case NOT_HANDSHAKING:
                    if(open)
                        return;
                    else
                        engine.closeOutbound();
            }
            handshakeStatus = engine.getHandshakeStatus();
        }
    }

    private boolean writePendingToPeer() throws IOException{
        if(peerWriteBuffer!=null && peerWriteBuffer.hasRemaining()){
            do{
                if(peerOut.write(peerWriteBuffer)==0){
                    selfInterests |= OP_WRITE;
                    return false;
                }
            }while(peerWriteBuffer.hasRemaining());
        }
        return true;
    }

    /*-------------------------------------------------[ App Read ]---------------------------------------------------*/

    @Override
    public void addReadInterest(){
        if(transportIn.peekIn==this)
            transportIn.peekInInterested = true;
        if(appReadBuffers[appReadBuffers.length-1].hasRemaining()
                || engine.isInboundDone()
                || (peerReadBuffer!=null && peerReadBuffer.hasRemaining() && !unwrapUnderflow))
            transportIn.wakeupReader();
        else{
            if(selfInterests==0)
                peerIn.addReadInterest();
            else{
                if((selfInterests&OP_READ)!=0)
                    peerIn.addReadInterest();
                if((selfInterests&OP_WRITE)!=0)
                    peerOut.addWriteInterest();
            }
        }
    }

    @Override
    public int read(ByteBuffer dst) throws IOException{
        if(!isOpen())
            throw new ClosedChannelException();
        if(IO)
            enter("SSLSocket1.read(dst)");
        if(selfInterests!=0){
            run(engine.getHandshakeStatus());
            if(selfInterests!=0)
                return 0;
        }
        ByteBuffer appReadBuffer = appReadBuffers[appReadBuffers.length-1];
        if(appReadBuffer.hasRemaining()){
            if(IO)
                exit("return "+Math.min(appReadBuffer.remaining(), dst.remaining()));
            return NIOUtil.copy(appReadBuffer, dst);
        }
        if(engine.isInboundDone()){
            if(IO)
                exit("return -1");
            eof = true;
            return -1;
        }

        appReadBuffers[--appReadBuffersOffset] = dst;
        try{
            while(true){
                run(NEED_UNWRAP);
                if(appRead==0){
                    if(engine.isInboundDone()){
                        if(IO)
                            exit("return -1");
                        eof = true;
                        return -1;
                    }else if(selfInterests!=0){
                        if(IO)
                            exit("return 0");
                        return 0;
                    }
                }else{
                    if(IO)
                        exit("return "+appRead);
                    return (int)appRead;
                }
            }
        }finally{
            appReadBuffers[appReadBuffersOffset++] = null;
        }
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException{
        return read(dsts, 0, dsts.length);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException{
        if(!isOpen())
            throw new ClosedChannelException();
        if(IO)
            enter("SSLSocket1.read(dsts)");
        if(selfInterests!=0){
            run(engine.getHandshakeStatus());
            if(selfInterests!=0)
                return 0;
        }
        ByteBuffer appReadBuffer = appReadBuffers[appReadBuffers.length-1];
        if(appReadBuffer.hasRemaining()){
            if(IO)
                exit("return "+Math.min(appReadBuffer.remaining(), new Buffers(dsts, offset, length).remaining()));
            return NIOUtil.copy(appReadBuffer, dsts, offset, length);
        }
        if(engine.isInboundDone()){
            if(IO)
                exit("return -1");
            eof = true;
            return -1;
        }

        if(appReadBuffers.length<length+1){
            appReadBuffers = new ByteBuffer[length+1];
            appReadBuffersOffset = length;
            appReadBuffers[length] = appReadBuffer;
        }

        appReadBuffersOffset -= length;
        System.arraycopy(dsts, offset, appReadBuffers, appReadBuffersOffset, length);
        try{
            while(true){
                run(NEED_UNWRAP);
                if(appRead==0){
                    if(engine.isInboundDone()){
                        if(IO)
                            exit("return -1");
                        eof = true;
                        return -1;
                    }else if(selfInterests!=0){
                        if(IO)
                            exit("return 0");
                        return 0;
                    }
                }else{
                    if(IO)
                        exit("return "+appRead);
                    return appRead;
                }
            }
        }finally{
            for(int i=0; i<length; i++)
                appReadBuffers[appReadBuffersOffset++] = null;
        }
    }

    private boolean eof;

    @Override
    public boolean eof(){
        return eof;
    }

    @Override
    public long available(){
        return appReadBuffers[appReadBuffers.length-1].remaining();
    }

    @Override
    public long transferTo(long position, long count, FileChannel target) throws IOException{
        ByteBuffer appReadBuffer = appReadBuffers[appReadBuffers.length-1];
        if(appReadBuffer.hasRemaining())
            return NIOUtil.transfer(appReadBuffer, target, position, count);
        return target.transferFrom(this, position, count);
    }

    /*-------------------------------------------------[ App Write ]---------------------------------------------------*/

    @Override
    public void addWriteInterest(){
        if(transportOut.peekOut==this)
            transportOut.peekOutInterested = true;
        if(engine.isOutboundDone())
            transportOut.wakeupWriter();
        else{
            if(selfInterests==0)
                peerOut.addWriteInterest();
            else{
                if((selfInterests&OP_READ)!=0)
                    peerIn.addReadInterest();
                if((selfInterests&OP_WRITE)!=0)
                    peerOut.addWriteInterest();
            }
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException{
        if(!isOpen())
            throw new ClosedChannelException();
        if(IO)
            enter("SSLSocket1.write(src)");
        if(selfInterests!=0){
            run(engine.getHandshakeStatus());
            if(selfInterests!=0)
                return 0;
        }
        if(engine.isOutboundDone())
            throw new IOException("outboundDone");
        appWriteBuffers.array[0] = src;
        try{
            run(NEED_WRAP);
            if(IO)
                exit("return "+appWrote);
            return (int)appWrote;
        }finally{
            appWriteBuffers.array[0] = EMPTY_BUFFER;
        }
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException{
        return write(srcs, 0, srcs.length);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException{
        if(!isOpen())
            throw new ClosedChannelException();
        if(IO)
            enter("SSLSocket1.write(srcs)");
        if(selfInterests!=0){
            run(engine.getHandshakeStatus());
            if(selfInterests!=0)
                return 0;
        }
        if(engine.isOutboundDone())
            throw new IOException("outboundDone");
        appWriteBuffers.array = srcs;
        appWriteBuffers.offset = offset;
        appWriteBuffers.length = length;
        try{
            run(NEED_WRAP);
            if(IO)
                exit("return "+appWrote);
            return (int)appWrote;
        }finally{
            appWriteBuffers.array = buffersArray1;
            appWriteBuffers.offset = 0;
            appWriteBuffers.length = 1;
        }
    }

    @Override
    public boolean flush() throws IOException{
        if(IO)
            println("SSLSocket1.flush()");
        if(peerIn.isOpen() && peerOut.isOpen()){
            if(selfInterests!=0){
                run(engine.getHandshakeStatus());
                if(selfInterests!=0)
                    return false;
            }
            if(!writePendingToPeer())
                return false;
            if(!open){
                if(peerReadBuffer!=null){
                    Reactor.current().allocator.free(peerReadBuffer);
                    peerReadBuffer = null;
                }
                if(peerWriteBuffer!=null){
                    Reactor.current().allocator.free(peerWriteBuffer);
                    peerWriteBuffer = null;
                }
                Reactor.current().allocator.free(appReadBuffers[appReadBuffers.length-1]);
                appReadBuffers[appReadBuffers.length-1] = null;
                try{
                    peerIn.close();
                }finally{
                    peerOut.close();
                }
            }
        }
        return peerOut.flush();
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
            if(IO)
                println("SSLSocket1.close()");
            open = false;
            ByteBuffer appReadBuffer = appReadBuffers[appReadBuffers.length-1];
            if(appReadBuffer.hasRemaining())
                appReadBuffer.position(appReadBuffer.limit());
            run(engine.getHandshakeStatus());
        }
    }

    /*-------------------------------------------------[ Transport-Misc ]---------------------------------------------------*/

    private final Socket transportIn, transportOut;

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

    /*-------------------------------------------------[ Bean ]---------------------------------------------------*/

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
