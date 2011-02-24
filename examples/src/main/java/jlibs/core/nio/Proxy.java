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

import jlibs.core.lang.ArrayUtil;
import jlibs.core.lang.OS;
import jlibs.core.net.Protocols;
import jlibs.core.nio.handlers.ClientHandler;
import jlibs.core.nio.handlers.Operation;
import jlibs.core.nio.handlers.SelectionHandler;
import jlibs.core.nio.handlers.ServerHandler;
import jlibs.core.util.CollectionUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class Proxy implements ServerHandler{
    Endpoint inboundEndpoint, outboundEndpoint;
    private ServerChannel server;

    public Proxy(Endpoint inboundEndpoint, Endpoint outboundEndpoint) throws IOException{
        this.inboundEndpoint = inboundEndpoint;
        this.outboundEndpoint = outboundEndpoint;

        server = new ServerChannel();
        server.bind(inboundEndpoint.address);
        server.attach(this);
    }

    private int count;
    @Override
    public void onAccept(ServerChannel channel, ClientChannel inboundClient) throws Exception{
        System.out.println(" Inbound"+count+": client"+inboundClient.id+" accepted");
        count++;
        ClientChannel outboundClient = null;
        try{
            if(inboundEndpoint.enableSSL)
                inboundClient.enableSSL();
            outboundClient = inboundClient.selector().newClient();

            ByteBuffer buffer1 = ByteBuffer.allocate(9000);
            ByteBuffer buffer2 = ByteBuffer.allocate(9000);

            ClientListener inboundListener = new ClientListener(" Inbound"+count, buffer1, buffer2, false, outboundClient);
            ClientListener outboundListener = new ClientListener("Outbound"+count, buffer2, buffer1, outboundEndpoint.enableSSL, inboundClient);

            inboundClient.attach(inboundListener);
            outboundClient.attach(outboundListener);

            SelectionHandler.connect(outboundClient, outboundEndpoint.address);
            inboundClient.addInterest(ClientChannel.OP_READ);
        }catch(Exception ex){
            if(outboundClient!=null)
                outboundClient.close();
            throw ex;
        }
    }

    @Override
    public void onThrowable(NIOChannel channel, Operation operation, Throwable error) throws Exception{
        error.printStackTrace();
        if(channel instanceof ClientChannel)
            channel.close();
    }

    private static void printUsage(){
        System.err.println("usage: proxy."+(OS.get().isUnix()?"sh":"bat")+" [-samples] source-url=target-url ...");
        System.err.println("source-url\tserver is started on this");
        System.err.println("target-url\ttraffic from server is redirected to this");
        System.err.println("-samples\twill add following sample proxies");
        for(String sample: samples)
            System.err.println("\t\t    "+sample);
        System.err.println();
        System.err.println("example: \n\tproxy."+(OS.get().isUnix()?"sh":"bat")+" http://localhost:3333=https://blog.torproject.org");
        System.exit(1);
    }

    private static final String samples[] = {
        "http://localhost:1111=http://mvnrepository.com",
        "https://localhost:2222=http://mvnrepository.com",
        "http://localhost:3333=https://blog.torproject.org",
        "https://localhost:4444=https://blog.torproject.org"
    };

    public static void main(String[] args) throws IOException{
        if(args.length==0)
            printUsage();
        System.setErr(System.out);

        int samplesIndex = ArrayUtil.indexOf(args, "-samples");
        if(samplesIndex!=-1){
            List<String> list = new ArrayList<String>();
            CollectionUtil.addAll(list, args);
            list.remove(samplesIndex);
            CollectionUtil.addAll(list, samples);
            args = list.toArray(new String[list.size()]);
        }

        ClientChannel.defaults().SO_TIMEOUT = 10*1000L;
        final NIOSelector nioSelector = new NIOSelector(1000);
        for(String arg: args){
            int equal = arg.indexOf("=");
            if(equal==-1)
                throw new IllegalArgumentException();

            String inbound = arg.substring(0, equal);
            String outbound = arg.substring(equal+1);
            Proxy proxy = new Proxy(new Endpoint(inbound), new Endpoint(outbound));
            proxy.server.register(nioSelector);
            System.out.println("added proxy: "+inbound+" -> "+outbound);
        }

        nioSelector.shutdownOnExit(false);
        new SelectionHandler(nioSelector).run();
    }
}

class Endpoint{
    InetSocketAddress address;
    boolean enableSSL;

    public Endpoint(String str) throws MalformedURLException{
        URL url = new URL(str);
        enableSSL = url.getProtocol().equals("https");

        String host = url.getHost();
        int port = url.getPort();
        if(port==-1)
            port = Protocols.valueOf(url.getProtocol().toUpperCase()).port();
        address = new InetSocketAddress(host, port);
    }
}

class ClientListener implements ClientHandler{
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

    @Override
    public void onConnect(ClientChannel client) throws IOException{
        System.out.println(this+": Connection established");
        if(enableSSL)
            client.enableSSL();
        client.addInterest(ClientChannel.OP_READ);
    }

    @Override
    public void onIO(ClientChannel client) throws Exception{
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
    }

    @Override
    public void onTimeout(ClientChannel client) throws Exception{
        if(buddy.isOpen() && buddy.isTimeout()){
            System.out.println(name+": timedout");
            client.close();
            buddy.close();
        }
    }

    public void process(NIOSelector nioSelector, ClientChannel client) throws IOException{
        if(client.isTimeout()){
            if(buddy.isOpen() && buddy.isTimeout()){
                System.out.println(name+": timedout");
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

    @Override
    public void onThrowable(NIOChannel channel, Operation operation, Throwable error) throws Exception{
        error.printStackTrace();
        if(channel instanceof ClientChannel)
            channel.close();
        buddy.close();
    }
}
