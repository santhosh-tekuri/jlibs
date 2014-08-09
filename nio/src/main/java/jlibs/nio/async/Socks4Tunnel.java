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

package jlibs.nio.async;

import jlibs.core.io.IOUtil;
import jlibs.nio.Client;
import jlibs.nio.ClientEndpoint;
import jlibs.nio.Proxy;
import jlibs.nio.util.Bytes;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Socks4Tunnel{
    private static final byte VERSION            = 5;
    private static final byte CONNECT            = 1;

    private ClientEndpoint endpoint;
    private Proxy proxy;
    public Socks4Tunnel(ClientEndpoint endpoint, Proxy proxy){
        this.endpoint = endpoint;
        this.proxy = proxy;
    }

    private ByteBuffer buffer;
    private Client client;
    private ExecutionContext context;
    public void start(Client client, ExecutionContext context){
        this.client = client;
        this.context = context;
        buffer = client.reactor.bufferPool.borrow(Bytes.CHUNK_SIZE);

        buffer.put(VERSION); // SOCKS version number
        buffer.put(CONNECT); // CONNECT command code
        buffer.put((byte)((endpoint.port>>8)&0xff));
        buffer.put((byte)(endpoint.port&0xff));
        InetSocketAddress address = endpoint.socketAddress();
        if(address.isUnresolved()){
            context.resume(new UnknownHostException(endpoint.host), false);
            return;
        }
        buffer.put(address.getAddress().getAddress());
        String user = proxy.user;
        if(user==null)
            user = System.getProperty("user.name");
        buffer.put(user.getBytes(IOUtil.ISO_8859_1));
        buffer.put((byte)0);
        buffer.flip();
        new WriteBuffer(buffer).start(client.out(), this::readResponse);
    }

    private void readResponse(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            client.reactor.bufferPool.returnBack(buffer);
            context.resume(thr, timeout);
            return;
        }
        buffer.position(0);
        buffer.limit(8);
        new ReadBuffer(buffer).start(client.in(), this::decodeResponse);
    }

    private void decodeResponse(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            client.reactor.bufferPool.returnBack(buffer);
            context.resume(thr, timeout);
            return;
        }
        try{
            buffer.flip();
            if(buffer.remaining()!=8)
                throw new SocketException("Reply from SOCKS server has bad length: " + buffer.remaining());
            byte firstByte = buffer.get();
            if(firstByte!=0 && firstByte!=VERSION)
                throw new SocketException("Reply from SOCKS server has bad version");
            switch(buffer.get()){
                case 0x5a:
                    client.reactor.bufferPool.returnBack(buffer);
                    context.resume(null, false);
                    return;
                case 0x5b:
                    throw new SocketException("SOCKS request rejected or failed");
                case 0x5c:
                    throw new SocketException("SOCKS server couldn't reach destination");
                case 0x5d:
                    throw new SocketException("SOCKS authentication failed");
                default:
                    throw new SocketException("Reply from SOCKS server contains bad status");
            }
        }catch(Throwable thr1){
            client.reactor.bufferPool.returnBack(buffer);
            context.resume(thr1, false);
        }
    }
}
