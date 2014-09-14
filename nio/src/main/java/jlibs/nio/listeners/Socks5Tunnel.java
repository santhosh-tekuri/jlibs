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

import jlibs.core.io.IOUtil;
import jlibs.nio.Reactor;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

import static java.nio.channels.SelectionKey.OP_WRITE;
import static jlibs.nio.listeners.Socks5Tunnel.Step.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Socks5Tunnel extends Task{
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

    private ByteBuffer buffer = Reactor.current().allocator.allocate();
    private String user;
    private String password;
    private InetSocketAddress endpoint;
    public Socks5Tunnel(String user, String password, InetSocketAddress endpoint){
        super(OP_WRITE);
        this.user = user;
        this.password = password;
        this.endpoint = endpoint;

        buffer.put(VERSION);
        buffer.put((byte)2); // number of authentication methods supported
        buffer.put(NO_AUTH);
        buffer.put(USER_PASSWORD);
        buffer.flip();
    }

    enum Step{
        SEND_GREETING, READ_SERVER_CHOICE,
        SEND_AUTHENTICATION, READ_AUTHENTICATION_RESULT, // if authentication needed
        PREPARE_CONNECT_REQUEST, SEND_CONNECT_REQUEST, READ_CONNECT_REQUEST, READ_DESTINATION_ADDRESS
    }
    private Step step = SEND_GREETING;

    @Override
    protected boolean process(int readyOp) throws IOException{
        while(true){
            switch(step){
                case SEND_GREETING:
                    if(!send(buffer))
                        return false;
                    buffer.position(0);
                    buffer.limit(2);
                    step = READ_SERVER_CHOICE;
                case READ_SERVER_CHOICE:
                    if(!read(buffer))
                        return false;
                    buffer.flip();
                    if(buffer.remaining()!=2 || buffer.get()!=VERSION){
                        setChild(new Socks4Tunnel(user, endpoint));
                        return true;
                    }

                    byte authenticationType = buffer.get();
                    if(authenticationType==NO_METHODS)
                        throw new SocketException("SOCKS: No acceptable methods");
                    if(authenticationType==NO_AUTH){
                        step = PREPARE_CONNECT_REQUEST;
                        break;
                    }
                    if(authenticationType!=USER_PASSWORD) // Username/Password
                        throw new SocketException("SOCKS: unsupported authentication");

                    buffer.clear();
                    buffer.put((byte)1);
                    if(user==null)
                        user = System.getProperty("user.name");
                    buffer.put((byte)user.length());
                    buffer.put(user.getBytes(IOUtil.ISO_8859_1));
                    if(password!=null) {
                        buffer.put((byte)password.length());
                        buffer.put(password.getBytes(IOUtil.ISO_8859_1));
                    } else
                        buffer.put((byte)0);
                    buffer.flip();
                    step = SEND_AUTHENTICATION;
                case SEND_AUTHENTICATION:
                    if(!send(buffer))
                        return false;
                    buffer.position(0);
                    buffer.limit(2);
                    step = READ_AUTHENTICATION_RESULT;
                case READ_AUTHENTICATION_RESULT:
                    if(!read(buffer))
                        return false;
                    buffer.flip();
                    if(buffer.remaining()!=2 || buffer.get(1)!=0)
                        throw new SocketException("SOCKS: authentication failed");
                    step = PREPARE_CONNECT_REQUEST;
                case PREPARE_CONNECT_REQUEST:
                    buffer.clear();
                    buffer.put(VERSION); // SOCKS version number
                    buffer.put(CONNECT); // CONNECT command code
                    buffer.put((byte)0);
                    if(endpoint.isUnresolved()){
                        buffer.put(DOMAIN_NAME);
                        buffer.put((byte)endpoint.getHostName().length());
                        buffer.put(endpoint.getHostName().getBytes(IOUtil.ISO_8859_1));
                    }else{
                        buffer.put(endpoint.getAddress() instanceof Inet6Address ? IPV6 : IPV4);
                        buffer.put(endpoint.getAddress().getAddress());
                    }
                    buffer.put((byte)((endpoint.getPort()>>8)&0xff));
                    buffer.put((byte)(endpoint.getPort()&0xff));
                    buffer.flip();
                    step = SEND_CONNECT_REQUEST;
                case SEND_CONNECT_REQUEST:
                    if(!send(buffer))
                        return false;
                    buffer.position(0);
                    buffer.limit(4);
                    step = READ_CONNECT_REQUEST;
                case READ_CONNECT_REQUEST:
                    if(!read(buffer))
                        return false;
                    buffer.flip();
                    if(buffer.remaining()!=4)
                        throw new SocketException("Reply from SOCKS server has bad length");
                    switch(buffer.get(1)){
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
                            step = READ_DESTINATION_ADDRESS;
                    }
                case READ_DESTINATION_ADDRESS:
                    if(!read(buffer))
                        return false;
                    if(buffer.hasRemaining())
                        throw new SocketException("Reply from SOCKS server badly formatted");
                    return true;
            }
        }
    }

    @Override
    protected void cleanup(Throwable thr){
        Reactor.current().allocator.free(buffer);
        buffer = null;
    }
}
