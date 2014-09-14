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
import jlibs.nio.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import static java.nio.channels.SelectionKey.OP_WRITE;
import static jlibs.nio.listeners.Socks4Tunnel.Step.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Socks4Tunnel extends Task{
    private static final byte VERSION            = 4;
    private static final byte CONNECT            = 1;

    private ByteBuffer buffer;
    public Socks4Tunnel(String user, InetSocketAddress endpoint) throws IOException{
        super(OP_WRITE);
        if(endpoint.isUnresolved())
            throw new UnknownHostException(endpoint.getHostString());
        buffer = Reactor.current().allocator.allocate();
        buffer.put(VERSION); // SOCKS version number
        buffer.put(CONNECT); // CONNECT command code
        buffer.put((byte)((endpoint.getPort()>>8)&0xff));
        buffer.put((byte)(endpoint.getPort()&0xff));
        buffer.put(endpoint.getAddress().getAddress());
        if(user==null)
            user = System.getProperty("user.name");
        buffer.put(user.getBytes(IOUtil.ISO_8859_1));
        buffer.put((byte)0);
        buffer.flip();
    }

    enum Step{ WRITE_REQUEST, READ_RESPONSE }
    private Step step = WRITE_REQUEST;

    @Override
    protected boolean process(int readyOp) throws IOException{
        while(true){
            switch(step){
                case WRITE_REQUEST:
                    if(!send(buffer))
                        return false;
                    buffer.position(0);
                    buffer.limit(8);
                    step = READ_RESPONSE;
                case READ_RESPONSE:
                    if(!read(buffer))
                        return false;
                    buffer.flip();
                    if(buffer.remaining()!=8)
                        throw new SocketException("Reply from SOCKS server has bad length: "+buffer.remaining());
                    byte firstByte = buffer.get();
                    if(firstByte!=0 && firstByte != VERSION)
                        throw new SocketException("Reply from SOCKS server has bad version");
                    switch(buffer.get()){
                        case 0x5a:
                            return true;
                        case 0x5b:
                            throw new SocketException("SOCKS request rejected or failed");
                        case 0x5c:
                            throw new SocketException("SOCKS server couldn't reach destination");
                        case 0x5d:
                            throw new SocketException("SOCKS authentication failed");
                        default:
                            throw new SocketException("Reply from SOCKS server contains bad status");
                    }
            }
        }
    }

    @Override
    protected void cleanup(Throwable thr){
        Reactor.current().allocator.free(buffer);
        buffer = null;
    }
}
