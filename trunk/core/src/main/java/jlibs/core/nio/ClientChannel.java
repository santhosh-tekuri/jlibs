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

import jlibs.core.net.SSLUtil;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Santhosh Kumar T
 */
public class ClientChannel extends NIOChannel implements SelectableByteChannel{
    private static AtomicLong ID_GENERATOR = new AtomicLong();

    protected final NIOSelector nioSelector;
    protected final SelectionKey key;
    protected ClientChannel(NIOSelector nioSelector, SocketChannel socketChannel) throws IOException{
        super(ID_GENERATOR.incrementAndGet(), socketChannel);
        key = realChannel().register(nioSelector.selector, 0, this);
        this.nioSelector = nioSelector;
        if(isConnected())
            nioSelector.connectedClients++;
        else
            nioSelector.connectionPendingClients++;
        defaults().apply(socketChannel.socket());
    }

    protected ClientChannel(NIOSelector nioSelector, SocketChannel socketChannel, SocketAddress acceptedFrom) throws IOException{
        this(nioSelector, socketChannel);
        this.acceptedFrom = acceptedFrom;
    }

    protected ClientChannel(){
        nioSelector = null;
        key = null;
        next = prev = this;
    }

    public NIOSelector selector(){
        return nioSelector;
    }

    @Override
    public SocketChannel realChannel(){
        return (SocketChannel)channel;
    }

    private SocketAddress acceptedFrom;
    public SocketAddress acceptedFrom(){
        return acceptedFrom;
    }

    public boolean connect(SocketAddress remote) throws IOException{
        boolean wasConnected = isConnected();
        boolean connected = realChannel().connect(remote);
        if(!wasConnected && connected){
            nioSelector.connectionPendingClients--;
            nioSelector.connectedClients++;
        }
        return connected;
    }

    public boolean finishConnect() throws IOException{
        boolean wasConnected = isConnected();
        boolean connected = realChannel().finishConnect();
        if(!wasConnected && connected){
            nioSelector.connectionPendingClients--;
            nioSelector.connectedClients++;
        }
        return connected;
    }

    public boolean isConnected(){
        return realChannel().isConnected();
    }

    public static final int OP_READ = SelectionKey.OP_READ;
    public static final int OP_WRITE = SelectionKey.OP_WRITE;
    public static final int OP_CONNECT = SelectionKey.OP_CONNECT;

    protected Transport transport = new PlainTransport(this);

    private HandshakeCompletedListener handshakeCompletedListener;
    public void setHandshakeCompletedListener(HandshakeCompletedListener handshakeCompletedListener){
        this.handshakeCompletedListener = handshakeCompletedListener;
    }

    public void enableSSL(SSLEngine engine) throws IOException{
        if(isConnected()){
            engine.setUseClientMode(acceptedFrom()==null);
            HandshakeCompletedListener listener = handshakeCompletedListener;
            handshakeCompletedListener = null;
            transport = new SSLTransport(transport, engine, listener);
        }else
            throw new ConnectionPendingException();
    }

    public void enableSSL(SSLParameters sslParameters) throws IOException{
        if(isConnected()){
            try {
                SSLEngine engine = SSLUtil.defaultContext().createSSLEngine();
                if(sslParameters!=null)
                    engine.setSSLParameters(sslParameters);
                enableSSL(engine);
            }catch(IOException ex){
                throw ex;
            }catch(Exception ex){
                throw new IOException(ex);
            }
        }else
            throw new ConnectionPendingException();
    }

    public void enableSSL() throws IOException{
        enableSSL((SSLParameters)null);
    }

    public boolean sslEnabled(){
        return transport instanceof SSLTransport;
    }

    public int interests(){
        return transport.interests();
    }

    protected int heapIndex = -1;
    public void addInterest(int operation) throws IOException{
        if(operation!=OP_CONNECT && operation!=OP_READ && operation!=OP_WRITE)
            throw new IllegalArgumentException(String.valueOf(operation));
        transport.addInterest(operation);
    }

    public void removeInterest(int operation) throws IOException{
        if(operation!=OP_CONNECT && operation!=OP_READ && operation!=OP_WRITE)
            throw new IllegalArgumentException(String.valueOf(operation));
        transport.removeInterest(operation);
    }

    public int ready(){
        return transport.ready();
    }

    public boolean isConnectable(){
        return (ready() & OP_CONNECT)!=0;
    }

    public boolean isReadable(){
        return (ready() & OP_READ)!=0;
    }

    public boolean isWritable(){
        return (ready() & OP_WRITE)!=0;
    }

    private long timeout = defaults.SO_TIMEOUT!=null && defaults.SO_TIMEOUT>0 ? defaults.SO_TIMEOUT : 0;
    public long getTimeout(){
        return timeout;
    }

    public void setTimeout(long timeout){
        if(timeout<0)
            throw new IllegalArgumentException("timeout can't be negative");
        this.timeout = timeout;
    }

    protected long timeoutAt = Long.MAX_VALUE;
    public boolean isTimeout(){
        return timeoutAt < nioSelector.timeoutTracker.time;
    }

    @Override
    protected boolean process(){
        return transport.process();
    }

    private boolean eof;
    public boolean isEOF(){
        return eof;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException{
        int read = transport.read(dst);
        if(read==-1)
            eof = true;
        return read;
    }

    @Override
    public int write(ByteBuffer src) throws IOException{
        return transport.write(src);
    }

    public void shutdownOutput() throws IOException{
        transport.shutdownOutput();
    }

    public boolean isOutputShutdown(){
        return transport.isOutputShutdown();
    }

    @Override
    public boolean isOpen(){
        return transport.isOpen();
    }

    @Override
    public void close() throws IOException{
        transport.close();
    }

    protected ClientChannel prev, next;

    @Override
    public String toString(){
        return (sslEnabled() ? "SSLClientChannel@" : "ClientChannel@")+id;
    }

    private static final Defaults defaults = new Defaults();
    public static Defaults defaults(){
        return defaults;
    }

    public static class Defaults{
        public Boolean TCP_NODELAY;
        public Integer SO_LINGER;
        public Integer SO_RCVBUF;
        public Integer SO_SNDBUF;
        public Long SO_TIMEOUT;
        private Defaults(){}

        void apply(Socket socket) throws SocketException{
            if(TCP_NODELAY!=null)
                socket.setTcpNoDelay(TCP_NODELAY);
            if(SO_LINGER!=null)
                socket.setSoLinger(SO_LINGER<0, SO_LINGER);
            if(SO_SNDBUF!=null)
                socket.setSendBufferSize(SO_SNDBUF);
            if(SO_RCVBUF!=null)
                socket.setReceiveBufferSize(SO_RCVBUF);
        }
    }

    // -ve=notPooled; 0=pooled; +ve=waitingToPool
    protected long poolFlag = -1;

    public void addToPool(long timeout){
        addToPool(realChannel().socket().getRemoteSocketAddress(), timeout);
    }

    private SocketAddress poolAddress;
    public void addToPool(SocketAddress address, long timeout){
        poolAddress = address;
        selector().pool.add(this, address, timeout);
    }

    public boolean isPooled(){
        return poolFlag==0;
    }

    public boolean removeFromPool(){
        return nioSelector.pool().remove(this, poolAddress);
    }
}
