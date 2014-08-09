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

import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Socks5Tunnel{
    private static final byte VERSION            = 5;
    private static final byte NO_AUTH            = 0;
    private static final byte USER_PASSWORD      = 2;
    private static final byte NO_METHODS         = -1;
    private static final byte CONNECT            = 1;

    private static final byte IPV4               = 1;
    private static final byte DOMAIN_NAME        = 3;
    private static final byte IPV6               = 4;

    private static final byte REQUEST_OK         = 0;
    private static final byte GENERAL_FAILURE    = 1;
    private static final byte NOT_ALLOWED        = 2;
    private static final byte NET_UNREACHABLE    = 3;
    private static final byte HOST_UNREACHABLE   = 4;
    private static final byte CONN_REFUSED       = 5;
    private static final byte TTL_EXPIRED        = 6;
    private static final byte CMD_NOT_SUPPORTED  = 7;
    private static final byte ADDR_TYPE_NOT_SUP  = 8;

    private ClientEndpoint endpoint;
    private Proxy proxy;
    public Socks5Tunnel(ClientEndpoint endpoint, Proxy proxy){
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

        buffer.put(VERSION);
        buffer.put((byte)2); // number of authentication methods supported
        buffer.put(NO_AUTH);
        buffer.put(USER_PASSWORD);
        buffer.flip();
        new WriteBuffer(buffer).start(client.out(), this::readServerChoice);
    }

    private void resume(Throwable thr, boolean timeout){
        client.reactor.bufferPool.returnBack(buffer);
        context.resume(thr, timeout);
    }

    private void readServerChoice(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            resume(thr, timeout);
            return;
        }
        buffer.position(0);
        buffer.limit(2);
        new ReadBuffer(buffer).start(client.in(), this::decodeServerChoice);
    }

    private void decodeServerChoice(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            resume(thr, timeout);
            return;
        }
        buffer.flip();
        if(buffer.remaining()!=2 || buffer.get()!=VERSION){
            client.reactor.bufferPool.returnBack(buffer);
            new Socks4Tunnel(endpoint, proxy).start(client, context);
            return;
        }
        authenticate(buffer.get());
    }

    private void authenticate(byte authenticationType){
        if(authenticationType==NO_METHODS){
            resume(new SocketException("SOCKS: No acceptable methods"), false);
            return;
        }
        if(authenticationType==NO_AUTH){
            sendConnectRequest();
            return;
        }
        if(authenticationType!=USER_PASSWORD){ // Username/Password
            resume(new SocketException("SOCKS: unsupported authentication"), false);
            return;
        }
        buffer.clear();
        buffer.put((byte)1);
        String user = proxy.user;
        if(user==null)
            user = System.getProperty("user.name");
        buffer.put((byte)user.length());
        buffer.put(user.getBytes(IOUtil.ISO_8859_1));
        if(proxy.password!=null) {
            buffer.put((byte)proxy.password.length());
            buffer.put(proxy.password.getBytes(IOUtil.ISO_8859_1));
        } else
            buffer.put((byte)0);
        buffer.flip();
        new WriteBuffer(buffer).start(client.out(), this::readAuthenticationResult);
    }

    private void readAuthenticationResult(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            resume(thr, timeout);
            return;
        }
        buffer.position(0);
        buffer.limit(2);
        new ReadBuffer(buffer).start(client.in(), this::decodeAuthenticationResult);
    }

    private void decodeAuthenticationResult(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            resume(thr, timeout);
            return;
        }
        buffer.flip();
        if(buffer.remaining()!=2 || buffer.get(1)!=0){
            resume(new SocketException("SOCKS: authentication failed"), false);
            return;
        }
        sendConnectRequest();
    }

    private void sendConnectRequest(){
        buffer.clear();
        buffer.put(VERSION); // SOCKS version number
        buffer.put(CONNECT); // CONNECT command code
        buffer.put((byte)0);
        InetSocketAddress address = endpoint.socketAddress();
        if(address.isUnresolved()){
            buffer.put(DOMAIN_NAME);
            buffer.put((byte)address.getHostName().length());
            buffer.put(address.getHostName().getBytes(IOUtil.ISO_8859_1));
        }else{
            buffer.put(address.getAddress() instanceof Inet6Address ? IPV6 : IPV4);
            buffer.put(address.getAddress().getAddress());
        }
        buffer.put((byte)((endpoint.port>>8)&0xff));
        buffer.put((byte)(endpoint.port&0xff));
        buffer.flip();
        new WriteBuffer(buffer).start(client.out(), this::readConnectResult);
    }

    private void readConnectResult(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            resume(thr, timeout);
            return;
        }
        buffer.position(0);
        buffer.limit(4);
        new ReadBuffer(buffer).start(client.in(), this::decodeConnectResult);
    }

    private void decodeConnectResult(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            resume(thr, timeout);
            return;
        }
        buffer.flip();
        if(buffer.remaining()!=4){
            resume(new SocketException("Reply from SOCKS server has bad length"), false);
            return;
        }
        try{
            switch(buffer.get(1)){
                case REQUEST_OK:
                    int len;
                    switch(buffer.get(3)){
                        case IPV4:
                            len = 4;
                            break;
                        case IPV6:
                        case DOMAIN_NAME:
                            len = buffer.get(1);
                            break;
                        default:
                            throw new SocketException("Reply from SOCKS server contains wrong code");
                    }
                    buffer.position(0);
                    buffer.limit(len+2);
                    new ReadBuffer(buffer).start(client.in(), this::finished);
                    return;
                case GENERAL_FAILURE:
                    throw new SocketException("SOCKS server general failure");
                case NOT_ALLOWED:
                    throw new SocketException("SOCKS: Connection not allowed by ruleset");
                case NET_UNREACHABLE:
                    throw new SocketException("SOCKS: Network unreachable");
                case HOST_UNREACHABLE:
                    throw new SocketException("SOCKS: Host unreachable");
                case CONN_REFUSED:
                    throw new SocketException("SOCKS: Connection refused");
                case TTL_EXPIRED:
                    throw new SocketException("SOCKS: TTL expired");
                case CMD_NOT_SUPPORTED:
                    throw new SocketException("SOCKS: Command not supported");
                case ADDR_TYPE_NOT_SUP:
                    throw new SocketException("SOCKS: address type not supported");
            }
        }catch(SocketException ex){
            context.resume(ex, false);
        }
    }

    private void finished(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            resume(thr, timeout);
            return;
        }
        if(buffer.hasRemaining())
            resume(new SocketException("Reply from SOCKS server badly formatted"), false);
        else
            resume(null, false);
    }
}
