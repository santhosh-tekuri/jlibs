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

package jlibs.nio;

import jlibs.nio.async.CloseOutput;
import jlibs.nio.async.ExecutionContext;
import jlibs.nio.channels.InputChannel;
import jlibs.nio.channels.ListenerUtil;
import jlibs.nio.channels.OutputChannel;
import jlibs.nio.channels.impl.SelectableInputChannel;
import jlibs.nio.channels.impl.SelectableOutputChannel;
import jlibs.nio.channels.impl.SocketIOChannel;
import jlibs.nio.channels.impl.filters.IOFilterChannel;
import jlibs.nio.channels.impl.filters.InputFilterChannel;
import jlibs.nio.channels.impl.filters.OutputFilterChannel;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static java.nio.channels.SelectionKey.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Client implements Closeable, Attachable{
    final long id;
    public final Reactor reactor;
    protected final SocketChannel channel;
    protected final SelectionKey selectionKey;

    Client(Reactor reactor, SocketChannel channel, Server acceptedFrom) throws IOException{
        this.reactor = reactor;
        this.channel = channel;
        this.acceptedFrom = acceptedFrom;
        if(acceptedFrom==null)
            id = ++reactor.lastConnectableClientID;
        else{
            id = ++reactor.lastAcceptedClientID;
            ++reactor.acceptedClients;
        }

        channel.configureBlocking(false);
        Defaults.apply(this);
        selectionKey = channel.register(reactor.selector, 0, this);

        socketIO = new SocketIOChannel(this, channel, selectionKey);
        socketIO.initialize(reactor.internal, this);
        in = socketIO;
        out = socketIO;

        if(acceptedFrom==null)
            initWorkingFor();
        else
            taskCompleted();
    }

    protected Server acceptedFrom;
    public SocketAddress acceptedFrom(){
        return acceptedFrom==null ? null : acceptedFrom.boundTo;
    }

    /*-------------------------------------------------[ connect ]---------------------------------------------------*/

    public boolean isConnected(){
        return channel.isConnected();
    }

    public boolean isConnectionPending(){
        return channel.isConnectionPending();
    }

    public SocketAddress connectedTo(){
        return channel.socket().getRemoteSocketAddress();
    }

    private SocketAddress connectAddress;
    private ExecutionContext connectListener;

    public void connect(SocketAddress address, ExecutionContext listener){
        connectAddress = address;
        connectListener = listener;
        reactor.connectionPendingClients++;
        try{
            if(Debugger.DEBUG)
                Debugger.println(this+".connect()");
            if(channel.connect(address))
                connectSuccessful();
            else{
                selectionKey.interestOps(OP_CONNECT);
                reactor.timeoutTracker.track(this);
            }
        }catch(Throwable thr){
            connectFailed(thr, false);
        }
    }

    void connectSuccessful(){
        if(Debugger.DEBUG)
            Debugger.println(this+".connectSuccessful()");
        try{
            reactor.connectionPendingClients--;
            reactor.connectedClients++;
            ExecutionContext connectListener = this.connectListener;
            this.connectListener = null;
            ListenerUtil.resume(connectListener, null, false);
        }catch(Throwable thr){
            reactor.handleException(thr);
        }
    }

    void connectFailed(Throwable thr, boolean timeout){
        if(Debugger.DEBUG)
            Debugger.println(this+".connectFailed("+thr+")");
        try{
            if(thr!=null)
                reactor.connectionPendingClients--;
            ExecutionContext connectListener = this.connectListener;
            this.connectListener = null;
            ListenerUtil.resume(connectListener, thr, timeout);
        }catch(Throwable thr1){
            reactor.handleException(thr1);
        }
    }

    /*-------------------------------------------------[ process ]---------------------------------------------------*/

    SelectableInputChannel readyInputChannel;
    SelectableOutputChannel readyOutputChannel;

    void process(){
        if(selectionKey.isConnectable()){
            try{
                if(!channel.finishConnect())
                    return;
            }catch(IOException ex){
                connectFailed(ex, false);
                return;
            }
            selectionKey.interestOps(selectionKey.interestOps()&~OP_CONNECT);
            connectSuccessful();
            if(!isOpen() || (selectionKey.readyOps()&~OP_CONNECT)==0)
                return;
        }

        boolean wasInClosed = in.isClosed();
        boolean wasOutClosed = out.isClosed();

        SelectableInputChannel input = null;
        SelectableOutputChannel output = null;
        int peerReadInterested;
        int peerWriteInterested;

        if(readyInputChannel==null && readyOutputChannel==null){
            input = socketIO;
            output = socketIO;
            peerReadInterested = peerWriteInterested = 0;
        }else{
            peerReadInterested = readyInputChannel==null ? 0 : readyInputChannel.interestOps()&OP_READ;
            peerWriteInterested = readyOutputChannel==null ? 0 : readyOutputChannel.interestOps()&OP_WRITE;
            if(readyInputChannel!=null)
                readyInputChannel.clearSelfReadyInterests();
            if(readyOutputChannel!=null && readyOutputChannel!=readyInputChannel)
                readyOutputChannel.clearSelfReadyInterests();
            if(readyInputChannel!=null)
                input = readyInputChannel.getAppInput();
            if(readyOutputChannel!=null)
                output = readyOutputChannel.getAppOutput();
            readyInputChannel = null;
            readyOutputChannel = null;
        }

        while(input!=null || output!=null){
            if(input!=null && output!=null){
                assert input==output;
                int tRead = input.interestOps()&OP_READ;
                int tWrite = output.interestOps()&OP_WRITE;
                try{
                    input.process(peerReadInterested|peerWriteInterested);
                }catch(Throwable thr){
                    ListenerUtil.error(in, thr);
                    ListenerUtil.error(out, thr);
                    return;
                }
                peerReadInterested = tRead;
                peerWriteInterested = tWrite;

                input = input.getAppInput();
                output = output.getAppOutput();
            }
            while(input!=null){
                if(input instanceof IOFilterChannel){
                    if(output==null)
                        output = (SelectableOutputChannel)input;
                    break;
                }
                int t = input.interestOps();
                try{
                    input.process(peerReadInterested);
                }catch(Throwable thr){
                    ListenerUtil.error(in, thr);
                    return;
                }
                peerReadInterested = t;
                input = input.getAppInput();
            }
            while(output!=null){
                if(output instanceof IOFilterChannel){
                    if(input==null)
                        input = (SelectableInputChannel)output;
                    break;
                }
                int t = output.interestOps();
                try{
                    output.process(peerWriteInterested);
                }catch(Throwable thr){
                    ListenerUtil.error(out, thr);
                    return;
                }
                peerWriteInterested = t;
                output = output.getAppOutput();
            }
        }

        if(!wasInClosed && in.isClosed())
            ListenerUtil.closed(in);
        if(!wasOutClosed && out.isClosed())
            ListenerUtil.closed(out);

        if((out.readyOps()&OP_WRITE)!=0)
            ListenerUtil.ready(out);
        if((in.readyOps()&OP_READ)!=0)
            ListenerUtil.ready(in);

        if(poolTimeout>0 && socketIO.interestOps()==0)
            reactor.clientPool.add(poolKey, this, poolTimeout);
    }

    /*-------------------------------------------------[ in ]---------------------------------------------------*/

    private SelectableInputChannel in;

    public InputChannel in(){
        return in;
    }

    public final InPipeline inPipeline = new InPipeline();
    public class InPipeline{
        private InPipeline(){}
        public void push(InputFilterChannel filter) throws IOException{
            if(!channel.isConnected())
                throw new NotYetConnectedException();
            if(Debugger.DEBUG)
                Debugger.println(Client.this+".push("+filter.getClass().getSimpleName()+"){");
            filter.initialize(reactor.internal, Client.this);
            filter.setPeerInput(in);
            in.setAppInput(filter);
            in = filter;
            filter.process(0);
            if(Debugger.DEBUG)
                Debugger.println("}");
        }

        public InputFilterChannel pop(){
            InputFilterChannel filter = null;
            if(!empty()){
                filter = (InputFilterChannel)in;
                filter.initialize(null, null);
                in = filter.getPeerInput();
                filter.setPeerInput(null);
                in.setAppInput(null);
                filter.dispose();
                if(Debugger.DEBUG)
                    Debugger.println(Client.this+".pop("+filter.getClass().getSimpleName()+")");
            }
            return filter;
        }

        public boolean empty(){
            return in instanceof SocketIOChannel || in instanceof IOFilterChannel;
        }
    }

    public boolean isBroken(){
        ByteBuffer buffer = ByteBuffer.allocate(1);
        try{
            int read = channel.read(buffer);
            if(read==-1)
                return true;
            if(read==1){
                buffer.flip();
                socketIO.unread(buffer);
            }
            return false;
        }catch(IOException ex){
            return true;
        }
    }

    /*-------------------------------------------------[ out ]---------------------------------------------------*/

    private SelectableOutputChannel out;

    public OutputChannel out(){
        return out;
    }

    public final OutPipeline outPipeline = new OutPipeline();
    public class OutPipeline{
        private OutPipeline(){}
        public void push(OutputFilterChannel filter) throws IOException{
            if(!channel.isConnected())
                throw new NotYetConnectedException();
            if(Debugger.DEBUG)
                Debugger.println(Client.this+".push("+filter.getClass().getSimpleName()+"){");
            filter.initialize(reactor.internal, Client.this);
            filter.setPeerOutput(out);
            out.setAppOutput(filter);
            out = filter;
            filter.process(0);
            if(Debugger.DEBUG)
                Debugger.println("}");
        }

        public OutputFilterChannel pop(){
            OutputFilterChannel filter = null;
            if(!empty()){
                filter = (OutputFilterChannel)out;
                filter.initialize(null, null);
                out = filter.getPeerOutput();
                filter.setPeerOutput(null);
                out.setAppOutput(null);
                filter.dispose();
                if(Debugger.DEBUG)
                    Debugger.println(Client.this+".pop("+filter.getClass().getSimpleName()+")");
            }
            return filter;
        }

        public boolean empty(){
            return out instanceof SocketIOChannel || out instanceof IOFilterChannel;
        }
    }

    /*-------------------------------------------------[ IO ]---------------------------------------------------*/

    final SocketIOChannel socketIO;

    public final Pipeline pipeline = new Pipeline();
    public class Pipeline{
        private Pipeline(){}

        public void push(IOFilterChannel filter) throws IOException{
            if(!channel.isConnected())
                throw new NotYetConnectedException();
            if(Debugger.DEBUG)
                Debugger.println(Client.this+".push("+filter.getClass().getSimpleName()+"){");
            filter.initialize(reactor.internal, Client.this);
            filter.setPeerInput(in);
            filter.setPeerOutput(out);
            in.setAppInput(filter);
            out.setAppOutput(filter);
            in = filter;
            out = filter;
            filter.process(0);
            if(Debugger.DEBUG)
                Debugger.println("}");
        }

        public IOFilterChannel pop(){
            IOFilterChannel filter = null;
            if(!empty()){
                filter = (IOFilterChannel)in;
                filter.initialize(null, null);
                in = filter.getPeerInput();
                filter.setPeerInput(null);
                in.setAppInput(null);
                out = filter.getPeerOutput();
                filter.setPeerOutput(null);
                out.setAppOutput(null);
                filter.dispose();
                if(Debugger.DEBUG)
                    Debugger.println(Client.this+".pop("+filter.getClass().getSimpleName()+")");
            }
            return filter;
        }

        public boolean empty(){
            return in!=out || in instanceof SocketIOChannel;
        }
    }

    /*-------------------------------------------------[ Timeout ]---------------------------------------------------*/

    int heapIndex = -1;
    private long timeout = 0;

    public long getTimeout(){
        return timeout;
    }

    public void setTimeout(long timeout){
        if(timeout<0)
            throw new IllegalArgumentException("timeout can't be negative");
        this.timeout = timeout;
    }

    long timeoutAt = Long.MAX_VALUE;
    public boolean isTimeout(){
        return timeoutAt < reactor.timeoutTracker.time;
    }

    void processTimeout(){
        if(isConnectionPending())
            connectFailed(null, true);
        if((out.interestOps()&OP_WRITE)!=0)
            ListenerUtil.timeout(out);
        if((in.interestOps()&OP_READ)!=0)
            ListenerUtil.timeout(in);
    }

    /*-------------------------------------------------[ ReadyList ]---------------------------------------------------*/

    protected Client readyPrev, readyNext;

    /*-------------------------------------------------[ Pooling ]---------------------------------------------------*/

    protected long poolTimeout;
    protected String poolKey;
    protected Client poolPrev, poolNext;

    /*-------------------------------------------------[ Attachment ]---------------------------------------------------*/

    private Object attachment;

    @Override
    public void attach(Object attachment){
        this.attachment = attachment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T attachment(){
        return (T)attachment;
    }

    /*-------------------------------------------------[ Close ]---------------------------------------------------*/

    public boolean isOpen(){
        return socketIO.isOpen();
    }

    public void close(){
        if(Debugger.DEBUG)
            Debugger.println(this+".close(){");
        while(true){
            while(!inPipeline.empty())
                inPipeline.pop();
            try{
                out.close();
            }catch(Throwable thr){
                // ignore
            }
            if(out.isClosed()){
                if(!outPipeline.empty())
                    outPipeline.pop();
                else if(!pipeline.empty())
                    pipeline.pop();
                else{
                    if(Debugger.DEBUG)
                        Debugger.println("}");
                    return;
                }
            }else{
                CloseOutput.INSTANCE.start(out, (thr, timeout) -> {
                    if(thr!=null || timeout)
                        closeForcefully();
                    else
                        close();
                });
                if(Debugger.DEBUG)
                    Debugger.println("}");
                return;
            }
        }
    }

    public void closeForcefully(){
        if(Debugger.DEBUG)
            Debugger.println(this+".closeForcefully()");
        while(true){
            if(!inPipeline.empty())
                inPipeline.pop();
            else if(!outPipeline.empty())
                outPipeline.pop();
            else if(!pipeline.empty())
                pipeline.pop();
            else
                break;
        }
        try{
            socketIO.close();
        }catch(IOException ex){
            // ignore
        }
    }

    int taskID = 0;
    Client workingFor;

    void initWorkingFor(){
        if(acceptedFrom!=null)
            throw new UnsupportedOperationException();
        Client activeClient = reactor.activeClient;
        if(activeClient!=null){
            if(activeClient.acceptedFrom!=null)
                workingFor = activeClient;
            else
                workingFor = activeClient.workingFor;
        }
        newTask();
    }

    void addingToPool(){
        reactor.activeClient = workingFor;
        workingFor = null;
    }

    public void taskCompleted(){
        if(acceptedFrom==null)
            throw new UnsupportedOperationException();
        newTask();
    }

    private void newTask(){
        executionID = null;
        ++taskID;
        makeActive();
    }

    public void makeActive(){
        reactor.activeClient = this;
    }

    private String executionID;
    public String getExecutionID(){
        if(executionID==null){
            String myID = (acceptedFrom==null ? "C" : "A")+id+"."+taskID;
            if(workingFor!=null)
                executionID = workingFor.getExecutionID()+"/"+myID;
            else
                executionID = reactor.executionID+"/"+myID;
        }
        return executionID;
    }

    private Runnable runnable;
    public void invokeLater(Runnable runnable){
        this.runnable = runnable;
        reactor.invokeLater(this::run);
    }

    private void run(){
        makeActive();
        Runnable r = runnable;
        runnable = null;
        r.run();
    }

    /*-------------------------------------------------[ Defaults ]---------------------------------------------------*/

    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder();
        buf.append("client"+id+"[");
        InetSocketAddress address;
        if(acceptedFrom==null){
            buf.append(isConnected() ? "c:" : "?:");
            address = (InetSocketAddress)connectAddress;
        }else{
            buf.append("a:");
            address = (InetSocketAddress)acceptedFrom();
        }
        if(address==null)
            buf.append("?");
        else
            buf.append(address.getHostString()+":"+address.getPort());

        buf.append("]");
        return buf.toString();
    }

    /*-------------------------------------------------[ Defaults ]---------------------------------------------------*/

    public static class Defaults{
        public static Boolean TCP_NODELAY;
        public static Integer SO_LINGER;
        public static Integer SO_RCVBUF;
        public static Integer SO_SNDBUF;
        public static Long SO_TIMEOUT;
        private Defaults(){}

        public static void apply(Client client) throws SocketException{
            Socket socket = client.channel.socket();
            if(TCP_NODELAY!=null)
                socket.setTcpNoDelay(TCP_NODELAY);
            if(SO_LINGER!=null)
                socket.setSoLinger(SO_LINGER<0, SO_LINGER);
            if(SO_SNDBUF!=null)
                socket.setSendBufferSize(SO_SNDBUF);
            if(SO_RCVBUF!=null)
                socket.setReceiveBufferSize(SO_RCVBUF);
            if(SO_TIMEOUT!=null && SO_TIMEOUT>0)
                client.timeout = SO_TIMEOUT;
        }

        public static long getTimeout(){
            if(SO_TIMEOUT!=null && SO_TIMEOUT>0)
                return SO_TIMEOUT;
            else
                return 0;
        }
    }
}
