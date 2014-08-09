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

package jlibs.nio.channels.impl.filters;

import jlibs.nio.Debugger;
import jlibs.nio.channels.OutputChannel;
import jlibs.nio.channels.impl.AbstractInputChannel;
import jlibs.nio.channels.impl.SelectableInputChannel;
import jlibs.nio.channels.impl.SelectableOutputChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class AbstractIOFilterChannel extends AbstractInputChannel implements IOFilterChannel{
    private OutputFilterChannel appOutput;

    @Override
    public void setAppOutput(OutputFilterChannel output){
        appOutput = output;
    }

    @Override
    public OutputFilterChannel getAppOutput(){
        return appOutput;
    }

    protected SelectableInputChannel peerInput;

    @Override
    public void setPeerInput(SelectableInputChannel input){
        peerInput = input;
    }

    @Override
    public SelectableInputChannel getPeerInput(){
        return peerInput;
    }

    protected SelectableOutputChannel peerOutput;

    @Override
    public void setPeerOutput(SelectableOutputChannel output){
        peerOutput = output;
    }

    @Override
    public SelectableOutputChannel getPeerOutput(){
        return peerOutput;
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
    public final int write(ByteBuffer src) throws IOException{
        if(!isOpen())
            throw new ClosedChannelException();
        if(Debugger.IO)
            Debugger.println(this+".write{");
        int wrote = _write(src);
        if(outputMetric>=0)
            outputMetric += wrote;
        if(Debugger.IO){
            Debugger.println("return "+wrote);
            Debugger.println("}");
        }
        return wrote;
    }

    protected abstract int _write(ByteBuffer src) throws IOException;

    @Override
    public final long write(ByteBuffer[] srcs) throws IOException{
        return write(srcs, 0, srcs.length);
    }

    @Override
    public final long write(ByteBuffer[] srcs, int offset, int length) throws IOException{
        if(!isOpen())
            throw new ClosedChannelException();
        if(Debugger.IO)
            Debugger.println(getClass().getSimpleName()+".write{");
        long temp = outputMetric;
        outputMetric = -1;
        long wrote;
        try{
            wrote = _write(srcs, offset, length);
        }finally{
            outputMetric = temp;
        }
        if(outputMetric>=0)
            outputMetric += wrote;

        if(Debugger.IO){
            Debugger.println("return "+wrote);
            Debugger.println("}");
        }
        return wrote;
    }

    protected abstract long _write(ByteBuffer[] srcs, int offset, int length) throws IOException;

    protected int selfInterestOps;

    @Override
    public final int selfInterestOps(){
        return selfInterestOps;
    }

    @Override
    public void process(int peerInterestOps) throws IOException{
        if(Debugger.IO)
            Debugger.println(this+".process("+Debugger.ops(peerInterestOps)+"){");
        int peerReadyOps = 0;
        if(peerInput==peerOutput)
            peerReadyOps = peerInput.readyOps();
        else{
            if((peerInput.readyOps()&OP_READ)!=0)
                peerReadyOps |= OP_READ;
            if((peerOutput.readyOps()&OP_WRITE)!=0)
                peerReadyOps |= OP_WRITE;
        }

        int selfInterestOpsBefore = selfInterestOps;
        if(selfInterestOpsBefore!=0){
            selfInterestOps = 0;
            _process();
        }

        readyOps = 0;
        if(isOpen()){
            if(selfInterestOps==0){
                int interestOps = interestOps();
                int selfReadyOps = selfReadyOps();
                if((interestOps&OP_READ)!=0){
                    if((selfReadyOps&OP_READ)!=0)
                        readyOps |= OP_READ;
                    else if((peerInterestOps&OP_READ)!=0)
                        readyOps |= peerReadyOps&OP_READ;
                }
                if((interestOps&OP_WRITE)!=0){
                    if((selfReadyOps&OP_WRITE)!=0)
                        readyOps |= OP_WRITE;
                    else if((peerInterestOps&OP_WRITE)!=0)
                        readyOps |= peerReadyOps&OP_WRITE;
                }
                interestOps &= ~readyOps;
                if(selfInterestOpsBefore!=0 && interestOps!=0)
                    addPeerInterests(interestOps); // push app interests to peer
            }else
                addPeerInterests(selfInterestOps);
        }
        if(Debugger.IO){
            Debugger.println("readyOps = "+Debugger.ops(readyOps));
            Debugger.println("}");
        }
    }

    protected abstract void _process() throws IOException;

    protected final void _addInterestOps(int ops){
        if(selfInterestOps==0)
            addPeerInterests(ops);
    }

    protected final void addPeerInterests(int ops){
        if(peerInput==peerOutput)
            peerInput.addInterestOps(ops);
        else{
            if((ops&OP_READ)!=0)
                peerInput.addInterestOps(OP_READ);
            if((ops&OP_WRITE)!=0)
                peerOutput.addInterestOps(OP_WRITE);
        }
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
