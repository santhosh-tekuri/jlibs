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
import jlibs.nio.channels.impl.AbstractSelectableChannel;
import jlibs.nio.channels.impl.SelectableOutputChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import static java.nio.channels.SelectionKey.OP_WRITE;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class AbstractOutputFilterChannel extends AbstractSelectableChannel implements OutputFilterChannel{
    private OutputFilterChannel appOutput;

    @Override
    public void setAppOutput(OutputFilterChannel output){
        appOutput = output;
    }

    @Override
    public OutputFilterChannel getAppOutput(){
        return appOutput;
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
        int wrote = 0;
        if(selfInterestOps==0){
            wrote = _write(src);
            if(selfInterestOps!=0)
                peerOutput.addInterestOps(selfInterestOps);
            if(outputMetric>=0)
                outputMetric += wrote;
        }
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
            Debugger.println(this+".write{");
        long wrote = 0;
        if(selfInterestOps==0){
            long temp = outputMetric;
            outputMetric = -1;
            try{
                wrote = _write(srcs, offset, length);
            }finally{
                outputMetric = temp;
            }
            if(selfInterestOps!=0)
                peerOutput.addInterestOps(selfInterestOps);
            if(outputMetric>=0)
                outputMetric += wrote;
        }
        if(Debugger.IO){
            Debugger.println("return "+wrote);
            Debugger.println("}");
        }
        return wrote;
    }

    protected long _write(ByteBuffer[] srcs, int offset, int length) throws IOException{
        long wrote = 0;
        while(length>0){
            int w = _write(srcs[offset]);
            if(w==0)
                break;
            wrote += w;
            ++offset;
            --length;
        }
        return wrote;
    }

    protected int selfInterestOps;

    @Override
    public final int selfInterestOps(){
        return selfInterestOps;
    }

    @Override
    public int selfReadyOps(){
        return 0;
    }

    @Override
    public void process(int peerInterestOps) throws IOException{
        int peerReadyOps = peerOutput.readyOps()&OP_WRITE;

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
                if((interestOps&OP_WRITE)!=0){
                    if((selfReadyOps&OP_WRITE)!=0)
                        readyOps |= OP_WRITE;
                    else if((peerInterestOps&OP_WRITE)!=0)
                        readyOps |= peerReadyOps&OP_WRITE;
                }
                interestOps &= ~readyOps;
                if(selfInterestOpsBefore!=0 && interestOps!=0)
                    peerOutput.addInterestOps(interestOps); // push app interests to peer
            }else
                peerOutput.addInterestOps(selfInterestOps);
        }
    }

    protected abstract void _process() throws IOException;

    @Override
    protected void _addInterestOps(int ops){
        if(selfInterestOps==0)
            peerOutput.addInterestOps(ops);
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
