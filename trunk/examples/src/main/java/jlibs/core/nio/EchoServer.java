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

import jlibs.core.lang.Bytes;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author Santhosh Kumar T
 */
public class EchoServer{
    public static void main(String[] args) throws IOException{
        NIOSelector selector = new NIOSelector(1000, 0);
        ServerChannel server = new ServerChannel();
        server.bind(new InetSocketAddress(1111));
        server.register(selector);

        for(NIOChannel channel: selector){
            if(channel instanceof ServerChannel){
                ClientChannel client = server.accept(selector);
                if(client!=null){
                    client.attach(new Bytes());
                    client.addInterest(ClientChannel.OP_READ);
                }
            }else{
                ClientChannel client = (ClientChannel)channel;
                Bytes bytes = (Bytes)client.attachment();

                if(client.isWritable()){
                    bytes.writeTo(client);
                    if(!bytes.isEmpty())
                        client.addInterest(ClientChannel.OP_WRITE);
                    else if(client.isEOF())
                        client.close();
                }

                if(client.isReadable()){
                    bytes.readFrom(client);
                    if(!client.isEOF())
                        client.addInterest(ClientChannel.OP_READ);
                    if(!bytes.isEmpty())
                        client.addInterest(ClientChannel.OP_WRITE);
                }
            }
        }
    }
}
