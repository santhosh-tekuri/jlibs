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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.security.KeyStore;

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
    public static SSLEngine createSSLEngine(boolean clientMode) throws IOException{
        SSLContext sslContext = null;
        try {
            char[] passphrase = "dontknow".toCharArray();
            // First initialize the key and trust material.
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("/Volumes/Backup/projects/jlibs/keystore.ks"), passphrase);
            sslContext = SSLContext.getInstance("TLS");

            if(clientMode){
                // TrustManager's decide whether to allow connections.
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(ks);
                sslContext.init(null, null/*tmf.getTrustManagers()*/, null);
            }else{
                // KeyManager's decide which key material to use.
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, passphrase);
                sslContext.init(kmf.getKeyManagers(), null, null);
            }
        }catch(Exception ex) {
            throw new IOException(ex);
        }

        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(clientMode);
        return engine;
    }

    @Override
    public void process(NIOSelector nioSelector, ServerChannel server) throws IOException{
        ClientChannel inboundClient = server.accept(nioSelector);
        if(inboundClient==null)
            return;
        if(inboundEndpoint.enableSSL)
            inboundClient.enableSSL(createSSLEngine(false));
        count++;
        ClientChannel outboundClient = nioSelector.newClient();

        System.out.println(" Inbound"+count+": client"+inboundClient.id+" accepted");
        ByteBuffer buffer1 = ByteBuffer.allocate(9000);
        ByteBuffer buffer2 = ByteBuffer.allocate(9000);

        ClientListener inboundListener = new ClientListener(" Inbound"+count, buffer1, buffer2, null, outboundClient);

        SSLEngine outboundSSLEngine = outboundEndpoint.enableSSL ? createSSLEngine(true) : null;
        ClientListener outboundListener = new ClientListener("Outbound"+count, buffer2, buffer1, outboundSSLEngine, inboundClient);

        inboundClient.attach(inboundListener);
        outboundClient.attach(outboundListener);

        if(outboundClient.connect(outboundEndpoint.address))
            outboundListener.onConnect(outboundClient);
        else
            outboundClient.addInterest(ClientChannel.OP_CONNECT);

        inboundClient.addInterest(ClientChannel.OP_READ);
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

        for(String arg: args){
            int equal = arg.indexOf("=");
            if(equal==-1)
                throw new IllegalArgumentException();

            String inbound = arg.substring(0, equal);
            String outbound = arg.substring(equal+1);
            new Proxy(new Endpoint(inbound), new Endpoint(outbound)).server.register(nioSelector);
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
    private SSLEngine engine;
    private ClientChannel buddy;
    ClientListener(String name, ByteBuffer readBuffer, ByteBuffer writeBuffer, SSLEngine engine, ClientChannel buddy){
        this.name = name;
        this.readBuffer = readBuffer;
        this.writeBuffer = writeBuffer;
        this.engine = engine;
        this.buddy = buddy;
    }

    public void onConnect(ClientChannel client) throws IOException{
        System.out.println(this+": Connection established");
        if(engine!=null)
            client.enableSSL(engine);
        client.addInterest(ClientChannel.OP_READ);
    }

    int readCount, writeCount;
    int lastRead, lastWrote;

    @Override
    public void process(NIOSelector nioSelector, ClientChannel client) throws IOException{
//        System.out.println(this+": isReadable="+isReadable+" isWritable="+isWritable);
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
                lastRead = read;
                if(read==-1){
                    client.close();
                    if((buddy.interests()&ClientChannel.OP_WRITE)==0)
                        buddy.close();
                }else if(read>0){
                    readCount++;
                    if(buddy.isOpen()){
                        readBuffer.flip();
                        buddy.addInterest(SelectionKey.OP_WRITE);
                    }else
                        client.close();
                }else
                    client.addInterest(SelectionKey.OP_READ);
            }
            if(client.isWritable()){
                int wrote = client.write(writeBuffer);
                lastWrote = wrote;
                if(writeBuffer.hasRemaining()){
                    writeCount++;
                    client.addInterest(SelectionKey.OP_WRITE);
                }else{
                    if(buddy.isOpen()){
                        writeBuffer.flip();
                        buddy.addInterest(SelectionKey.OP_READ);
                    }else
                        client.close();
                }
            }
        }catch(IOException ex){
            client.close();
            buddy.close();
            throw ex;
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