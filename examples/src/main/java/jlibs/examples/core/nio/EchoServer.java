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

package jlibs.examples.core.nio;

import jlibs.core.lang.Bytes;
import jlibs.core.lang.OS;
import jlibs.core.nio.ClientChannel;
import jlibs.core.nio.NIOChannel;
import jlibs.core.nio.NIOSelector;
import jlibs.core.nio.ServerChannel;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author Santhosh Kumar T
 */
public class EchoServer{
    private static void printUsage(){
        System.err.println("usage: echo-server."+(OS.get().isUnix()?"sh":"bat")+" [-ssl] [host] port");
        System.exit(1);
    }

    private static void accepted(ClientChannel client){
        System.out.println(client+" accepted from "+((InetSocketAddress)client.realChannel().socket().getRemoteSocketAddress()).getAddress().getHostAddress());
    }

    private static void close(ClientChannel client){
        try{
            client.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        System.out.println(client+" closed");
    }

    public static void main(String[] args) throws IOException{
        if(args.length==0)
            printUsage();

        boolean ssl = false;
        String host = "localhost";
        int port = -1;
        for(String arg: args){
            if(arg.equals("-ssl"))
                ssl = true;
            else{
                try{
                    port = Integer.parseInt(arg);
                }catch (NumberFormatException ex){
                    host = arg;
                }
            }
        }
        if(port==-1)
            printUsage();

        NIOSelector selector = new NIOSelector(1000);
        selector.shutdownOnExit(false);
        ServerChannel server = new ServerChannel();
        server.bind(new InetSocketAddress(host, port));

        System.out.println("Listening on "+host+":"+port+(ssl?" with SSL":""));
        server.register(selector);

        for(NIOChannel channel: selector){
            if(channel instanceof ServerChannel){
                ClientChannel client = server.accept(selector);
                if(client!=null){
                    accepted(client);
                    try{
                        if(ssl)
                            client.enableSSL();
                        client.attach(new Bytes());
                        client.addInterest(ClientChannel.OP_READ);
                    }catch (IOException ex){
                        ex.printStackTrace();
                        close(client);
                    }
                }
            }else{
                ClientChannel client = (ClientChannel)channel;
                Bytes bytes = (Bytes)client.attachment();
                try{
                    if(client.isWritable()){
                        bytes.writeTo(client);
                        if(!bytes.isEmpty())
                            client.addInterest(ClientChannel.OP_WRITE);
                        else if(client.isEOF())
                            close(client);
                    }

                    if(client.isReadable()){
                        bytes.readFrom(client);
                        if(!client.isEOF())
                            client.addInterest(ClientChannel.OP_READ);
                        if(bytes.isEmpty())
                            close(client);
                        else
                            client.addInterest(ClientChannel.OP_WRITE);
                    }
                }catch(IOException ex){
                    ex.printStackTrace();
                    close(client);
                }
            }
        }
    }
}
