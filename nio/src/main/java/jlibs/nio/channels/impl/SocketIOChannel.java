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

package jlibs.nio.channels.impl;

import jlibs.nio.Debugger;
import jlibs.nio.Client;
import jlibs.nio.channels.OutputChannel;
import jlibs.nio.channels.impl.filters.OutputFilterChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class SocketIOChannel extends AbstractInputChannel implements SelectableIOChannel{
    public final Client client;
    private final SocketChannel channel;
    private final SelectionKey selectionKey;

    public SocketIOChannel(Client client, SocketChannel channel, SelectionKey selectionKey){
        this.client = client;
        this.channel = channel;
        this.selectionKey = selectionKey;
    }

    private OutputFilterChannel appOutput;

    @Override
    public void setAppOutput(OutputFilterChannel output){
        appOutput = output;
    }

    @Override
    public OutputFilterChannel getAppOutput(){
        return appOutput;
    }

    @Override
    protected int _read(ByteBuffer dst) throws IOException{
        return channel.read(dst);
    }

    @Override
    protected long _read(ByteBuffer[] dsts, int offset, int length) throws IOException{
        return channel.read(dsts, offset, length);
    }

    @Override
    public int write(ByteBuffer src) throws IOException{
        if(Debugger.IO)
            Debugger.println(this+".write{");
        int wrote = channel.write(src);
        if(outputMetric>=0)
            outputMetric += wrote;
        if(Debugger.IO){
            Debugger.println("wrote = "+wrote);
            Debugger.println("}");
        }
        return wrote;
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException{
        if(Debugger.IO)
            Debugger.println(this+".write{");
        long wrote = channel.write(srcs);
        if(outputMetric>=0)
            outputMetric += wrote;
        if(Debugger.IO){
            Debugger.println("wrote = "+wrote);
            Debugger.println("}");
        }
        return wrote;
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException{
        if(Debugger.IO)
            Debugger.println(this+".write{");
        long wrote = channel.write(srcs, offset, length);
        if(outputMetric>=0)
            outputMetric += wrote;
        if(Debugger.IO){
            Debugger.println("wrote = "+wrote);
            Debugger.println("}");
        }
        return wrote;
    }

    @Override
    protected void _close() throws IOException{
        reactor.closing(client);
        channel.close();
    }

    @Override
    public boolean isClosed(){
        return !isOpen();
    }

    private OutputChannel.Listener outputListener;

    @Override
    public void setOutputListener(OutputChannel.Listener listener){
        outputListener = listener;
    }

    @Override
    public OutputChannel.Listener getOutputListener(){
        return outputListener;
    }

    @Override
    protected int _selfReadyOps(){
        return 0;
    }

    @Override
    public void process(int peerInterests) throws IOException{
        readyOps = selectionKey.readyOps()&~SelectionKey.OP_CONNECT;
        readyOps |= selfReadyOps();
        interestOps &= ~readyOps;
        selectionKey.interestOps(selectionKey.interestOps()&~readyOps);
    }

    @Override
    protected void _addInterestOps(int ops){
        if(client.getTimeout()>0)
            reactor.track(client);
        selectionKey.interestOps(interestOps);
    }

    /*-------------------------------------------------[ Metric ]---------------------------------------------------*/

    private long outputMetric = -1;

    @Override
    public final void startOutputMetric(){
        outputMetric = 0;
    }

    @Override
    public final long getOutputMetric(){
        return outputMetric;
    }

    @Override
    public final long stopOutputMetric(){
        long t = outputMetric;
        outputMetric = -1;
        return t;
    }
}
