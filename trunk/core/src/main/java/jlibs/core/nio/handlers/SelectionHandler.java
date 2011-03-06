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

import jlibs.core.nio.*;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * @author Santhosh Kumar T
 */
public class SelectionHandler implements Runnable{
    public final NIOSelector selector;

    public SelectionHandler(NIOSelector selector){
        this.selector = selector;
    }

    protected void handleException(Throwable ex){
        ex.printStackTrace();
    }

    @Override
    public void run(){
        for(NIOChannel channel: selector){
            if(channel instanceof ServerChannel){
                ServerChannel server = (ServerChannel)channel;
                ServerHandler handler = (ServerHandler)channel.attachment();
                try{
                    try{
                        ClientChannel client = server.accept(selector);
                        if(client!=null)
                            handler.onAccept(server, client);
                    }catch(IOException ex){
                        handler.onAcceptFailure(server, ex);
                    }
                }catch(Throwable thr){
                    handleException(thr);
                }
            }else{
                ClientChannel client = (ClientChannel)channel;
                ClientHandler handler = (ClientHandler)channel.attachment();
                if(client.isTimeout()){
                    ClientPool pool = client.pool();
                    if(pool!=null && !pool.remove(client))
                        pool = null;
                    if(pool==null){
                        try{
                            handler.onTimeout(client);
                        }catch(Throwable ex){
                            handleException(ex);
                        }
                    }else{
                        try{
                            ((ClientPoolHandler)pool.attachment()).onTimeout(client);
                        }catch(Throwable ex){
                            handleException(ex);
                        }
                    }
                }else{
                    if(client.isConnectable()){
                        try{
                            try{
                                if(client.finishConnect())
                                    handler.onConnect(client);
                                else{
                                    client.addInterest(ClientChannel.OP_CONNECT);
                                    continue;
                                }
                            }catch(IOException ex){
                                handler.onConnectFailure(client, ex);
                            }
                        }catch(Throwable thr){
                            handleException(thr);
                        }
                    }
                    if(client.isReadable() || client.isWritable()){
                        try{
                            handler.onIO(client);
                        }catch(Throwable ex){
                            handleException(ex);
                        }
                    }
                }
            }
        }
    }

    public static void connect(ClientChannel client, SocketAddress remote) throws IOException{
        ClientHandler handler = (ClientHandler)client.attachment();
        if(client.connect(remote))
            handler.onConnect(client);
        else
            client.addInterest(ClientChannel.OP_CONNECT);
    }
}
