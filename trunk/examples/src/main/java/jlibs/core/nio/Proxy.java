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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class Proxy implements IOEvent<ServerChannel>{
    Endpoint inboundEndpoint, outboundEndpoint;
    private ServerChannel server;

    public Proxy(Endpoint inboundEndpoint, Endpoint outboundEndpoint) throws IOException{
        this.inboundEndpoint = inboundEndpoint;
        this.outboundEndpoint = outboundEndpoint;

        server = new ServerChannel(ServerSocketChannel.open());
        server.bind(inboundEndpoint.address);
        server.attach(this);
    }

    private int count;
    @Override
    public void process(NIOSelector nioSelector, ServerChannel server) throws IOException{
        ClientChannel inboundClient = server.accept(nioSelector);
        if(inboundClient==null)
            return;
        count++;
        ClientChannel outboundClient = nioSelector.newClient();
        try{
            System.out.println(" Inbound"+count+": client"+inboundClient.id+" accepted");

            if(inboundEndpoint.enableSSL)
                inboundClient.enableSSL();

            ByteBuffer buffer1 = ByteBuffer.allocate(9000);
            ByteBuffer buffer2 = ByteBuffer.allocate(9000);

            ClientListener inboundListener = new ClientListener(" Inbound"+count, buffer1, buffer2, false, outboundClient);
            ClientListener outboundListener = new ClientListener("Outbound"+count, buffer2, buffer1, outboundEndpoint.enableSSL, inboundClient);

            inboundClient.attach(inboundListener);
            outboundClient.attach(outboundListener);

            if(outboundClient.connect(outboundEndpoint.address))
                outboundListener.onConnect(outboundClient);
            else
                outboundClient.addInterest(ClientChannel.OP_CONNECT);

            inboundClient.addInterest(ClientChannel.OP_READ);
        }catch(Exception ex){
            inboundClient.close();
            outboundClient.close();
            if(ex instanceof IOException)
                throw (IOException)ex;
            else
                throw (RuntimeException)ex;
        }
    }

    public static void main(String[] args) throws IOException{
        System.setErr(System.out);

        final NIOSelector nioSelector = new NIOSelector(5000);
        if(args.length==0){
            args = new String[]{
                "tcp://localhost:1111=tcp://apigee.com:80",
                "ssl://localhost:2222=tcp://apigee.com:80",
                "tcp://localhost:3333=ssl://blog.torproject.org:443",
                "ssl://localhost:4444=ssl://blog.torproject.org:443",
            };
        }

        final List<Proxy> proxies = new ArrayList<Proxy>(args.length);
        for(String arg: args){
            int equal = arg.indexOf("=");
            if(equal==-1)
                throw new IllegalArgumentException();

            String inbound = arg.substring(0, equal);
            String outbound = arg.substring(equal+1);
            Proxy proxy = new Proxy(new Endpoint(inbound), new Endpoint(outbound));
            proxy.server.register(nioSelector);
            proxies.add(proxy);
            System.out.println("added proxy: "+inbound+" -> "+outbound);
        }

        final Thread nioThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                System.out.println("shutdown initiated");
                nioSelector.shutdown();
                try{
                    nioThread.join();
                }catch(InterruptedException ex){
                    ex.printStackTrace();
                }
                for(Proxy proxy: proxies){
                    try{
                        proxy.server.close();
                    }catch (IOException ex){
                        ex.printStackTrace();
                    }
                }
            }
        });

        for(NIOChannel channel: nioSelector){
            try{
                ((IOEvent)channel.attachment()).process(nioSelector, channel);
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
        System.out.println("finished shutdown");
    }
}

class Endpoint{
    InetSocketAddress address;
    boolean enableSSL;

    public Endpoint(String str){
        if(str.startsWith("tcp://")){
            enableSSL = false;
            str = str.substring("tcp://".length());
        }else if(str.startsWith("ssl://")){
            enableSSL = true;
            str = str.substring("ssl://".length());
        }else
            throw new IllegalArgumentException();

        int colon = str.indexOf(':');
        if(colon==-1)
            throw new IllegalArgumentException();
        String host = str.substring(0, colon);
        int port = Integer.parseInt(str.substring(colon+1));
        address = new InetSocketAddress(host, port);
    }
}

class ClientListener implements IOEvent<ClientChannel>{
    private String name;
    private ByteBuffer readBuffer, writeBuffer;
    private boolean enableSSL;
    private ClientChannel buddy;
    ClientListener(String name, ByteBuffer readBuffer, ByteBuffer writeBuffer, boolean enableSSL, ClientChannel buddy){
        this.name = name;
        this.readBuffer = readBuffer;
        this.writeBuffer = writeBuffer;
        this.enableSSL = enableSSL;
        this.buddy = buddy;
    }

    public void onConnect(ClientChannel client) throws IOException{
        System.out.println(this+": Connection established");
        if(enableSSL)
            client.enableSSL();
        client.addInterest(ClientChannel.OP_READ);
    }

    @Override
    public void process(NIOSelector nioSelector, ClientChannel client) throws IOException{
        if(client.isTimeout()){
            if(buddy.isOpen() && buddy.isTimeout()){
                System.out.println(name+" timedout");
                client.close();
                buddy.close();
            }
        }
        try{
            if(client.isConnectable()){
                if(client.finishConnect())
                    onConnect(client);
                else
                    return;
            }
            if(client.isReadable()){
                readBuffer.clear();
                int read = client.read(readBuffer);
                if(read==-1){
                    client.close();
                    if((buddy.interests()&ClientChannel.OP_WRITE)==0)
                        buddy.close();
                }else if(read>0){
                    if(buddy.isOpen()){
                        readBuffer.flip();
                        buddy.addInterest(SelectionKey.OP_WRITE);
                    }else
                        client.close();
                }else
                    client.addInterest(SelectionKey.OP_READ);
            }
            if(client.isWritable()){
                client.write(writeBuffer);
                if(writeBuffer.hasRemaining())
                    client.addInterest(SelectionKey.OP_WRITE);
                else{
                    if(buddy.isOpen()){
                        writeBuffer.flip();
                        buddy.addInterest(SelectionKey.OP_READ);
                    }else
                        client.close();
                }
            }
        }catch(Exception ex){
            client.close();
            buddy.close();
            if(ex instanceof IOException)
                throw (IOException)ex;
            else
                throw (RuntimeException)ex;

        }
        if(client.interests()==0 && buddy.interests()==0){
            System.out.println(this+": closing...");
            client.close();
            buddy.close();
        }
    }

    @Override
    public String toString(){
        return name;
    }
}

interface IOEvent<T extends NIOChannel>{
    public void process(NIOSelector nioSelector, T channel) throws IOException;
}