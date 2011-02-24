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

package jlibs.core.nio.handlers;

import jlibs.core.nio.ClientChannel;
import jlibs.core.nio.NIOChannel;
import jlibs.core.nio.NIOSelector;
import jlibs.core.nio.ServerChannel;

import java.net.SocketAddress;

/**
 * @author Santhosh Kumar T
 */
public class SelectionHandler implements Runnable{
    public final NIOSelector selector;

    public SelectionHandler(NIOSelector selector){
        this.selector = selector;
    }

    private void onException(ChannelHandler handler, NIOChannel channel, Operation operation, Throwable ex){
        try{
            handler.onThrowable(channel, operation, ex);
        }catch(Exception e){
            handleException(e);
        }
    }

    protected void handleException(Exception ex){
        ex.printStackTrace();
    }

    @Override
    public void run(){
        for(NIOChannel channel: selector){
            if(channel instanceof ServerChannel){
                ServerChannel server = (ServerChannel)channel;
                ServerHandler handler = (ServerHandler)channel.attachment();
                ClientChannel client = null;
                try {
                    client = server.accept(selector);
                    if(client!=null)
                        handler.onAccept(server, client);
                }catch(Throwable ex){
                    onException(handler, client != null ? client : server, Operation.ACCEPT, ex);
                }
            }else{
                ClientChannel client = (ClientChannel)channel;
                ClientHandler handler = (ClientHandler)channel.attachment();
                if(client.isTimeout()){
                    try{
                        handler.onTimeout(client);
                    }catch(Throwable ex){
                        onException(handler, client, Operation.TIMEOUT, ex);
                    }
                }else{
                    if(client.isConnectable()){
                        try{
                            if(client.finishConnect())
                                handler.onConnect(client);
                            else{
                                client.addInterest(ClientChannel.OP_CONNECT);
                                continue;
                            }
                        }catch(Throwable ex){
                            onException(handler, client, Operation.CONNECT, ex);
                        }
                    }
                    if(client.isReadable() || client.isWritable()){
                        try {
                            handler.onIO(client);
                        }catch(Throwable ex){
                            onException(handler, client, Operation.IO, ex);
                        }
                    }
                }
            }
        }
    }

    public static void connect(ClientChannel client, SocketAddress remote) throws Exception{
        ClientHandler handler = (ClientHandler)client.attachment();
        if(client.connect(remote))
            handler.onConnect(client);
        else
            client.addInterest(ClientChannel.OP_CONNECT);
    }
}
