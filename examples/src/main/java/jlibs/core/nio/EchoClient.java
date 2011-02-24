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

import jlibs.core.lang.OS;
import jlibs.core.util.logging.AnsiFormatter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar T
 */
public class EchoClient{
    private static void printUsage(){
        System.err.println("usage: echo-client."+(OS.get().isUnix()?"sh":"bat")+" [-ssl] [host] port");
        System.exit(1);
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

        final NIOSelector selector = new NIOSelector(1000);
        final ClientChannel client = selector.newClient();
        final ByteBuffer inBuffer = ByteBuffer.allocate(1024);

        Thread readThread = new Thread(new Runnable(){
            @Override
            public void run(){
                while(true){
                    try{
                        inBuffer.clear();
                        int read = System.in.read(inBuffer.array(), 0, inBuffer.remaining());
                        if(read!=-1)
                            inBuffer.position(inBuffer.position()+read);
                        inBuffer.flip();
                        synchronized(inBuffer){
                            selector.invokeLater(new Runnable(){
                                public void run(){
                                    try{
                                        if(inBuffer.hasRemaining())
                                            client.addInterest(ClientChannel.OP_WRITE);
                                        else
                                            client.shutdownOutput();
                                    }catch(IOException ex){
                                        ex.printStackTrace();
                                    }
                                }
                            });
                            selector.wakeup();
                            inBuffer.wait();
                        }
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        });
        readThread.setDaemon(true);

        client.connect(new InetSocketAddress(host, port));
        client.addInterest(ClientChannel.OP_CONNECT);

        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        for(NIOChannel channel: selector){
            assert channel==client;
            if(client.isConnectable()){
                if(client.finishConnect()){
                    System.out.println("connected to "+host+":"+port+(ssl?" with SSL":""));
                    System.out.println("press CTRL+"+(OS.get().isUnix()?'D':'Z')+" to signal end of input");
                    if(ssl)
                        client.enableSSL();
                    readThread.start();
                    client.addInterest(ClientChannel.OP_READ);
                }else
                    client.addInterest(ClientChannel.OP_CONNECT);
            }
            if(client.isWritable()){
                client.write(inBuffer);
                if(inBuffer.hasRemaining())
                    client.addInterest(ClientChannel.OP_WRITE);
                else{
                    synchronized(inBuffer){
                        inBuffer.notifyAll();
                    }
                }
            }

            if(client.isReadable()){
                if(client.read(readBuffer)>0){
                    AnsiFormatter.WARNING.out(new String(readBuffer.array(), 0, readBuffer.position()));
                    readBuffer.clear();
                }
                if(client.isEOF()){
                    client.close();
                    return;
                }else
                    client.addInterest(ClientChannel.OP_READ);
            }
        }
    }
}
