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

import java.io.IOException;
import java.net.SocketAddress;

/**
 * @author Santhosh Kumar T
 */
public class NIOThread extends Thread implements Thread.UncaughtExceptionHandler{
    public final NIOSelector selector;

    public NIOThread(NIOSelector selector){
        super("NIOThread@"+selector.id());
        this.selector = selector;
        setUncaughtExceptionHandler(this);
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
                    if(client.isPooled()){
                        client.removeFromPool();
                        try{
                            ((ClientPoolHandler)client.selector().pool().attachment()).onTimeout(client);
                        }catch(Throwable ex){
                            handleException(ex);
                        }
                    }else{
                        try{
                            handler.onTimeout(client);
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

    @Override
    public void uncaughtException(Thread t, Throwable ex){
        handleException(ex);
        new NIOThread(((NIOThread)t).selector).start();
    }

    public static NIOThread currentThread(){
        Thread thread = Thread.currentThread();
        return thread instanceof NIOThread ? (NIOThread)thread : null;
    }

    public static NIOSelector currentSelector(){
        NIOThread thread = currentThread();
        return thread==null ? null : thread.selector;
    }

    public static void connect(ClientChannel client, SocketAddress remote){
        ClientHandler handler = (ClientHandler)client.attachment();
        try{
            if(client.connect(remote))
                handler.onConnect(client);
            else
                client.addInterest(ClientChannel.OP_CONNECT);
        }catch(Exception ex){
            handler.onConnectFailure(client, ex);
        }
    }
}
