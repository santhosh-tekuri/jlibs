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
import jlibs.nio.channels.impl.AbstractInputChannel;
import jlibs.nio.channels.impl.SelectableInputChannel;

import java.io.IOException;

import static java.nio.channels.SelectionKey.OP_READ;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class AbstractInputFilterChannel extends AbstractInputChannel implements InputFilterChannel{
    protected SelectableInputChannel peerInput;

    @Override
    public void setPeerInput(SelectableInputChannel input){
        peerInput = input;
    }

    @Override
    public SelectableInputChannel getPeerInput(){
        return peerInput;
    }

    protected int selfInterestOps;

    @Override
    public final int selfInterestOps(){
        return selfInterestOps;
    }

    @Override
    public void process(int peerInterestOps) throws IOException{
        if(Debugger.IO)
            Debugger.println(this+".process("+Debugger.ops(peerInterestOps)+"){");
        int peerReadyOps = peerInput.readyOps()&OP_READ;

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
                interestOps &= ~readyOps;
                if(selfInterestOpsBefore!=0 && interestOps!=0)
                    peerInput.addInterestOps(interestOps); // push app interests to peer
            }else
                peerInput.addInterestOps(selfInterestOps);
        }
        if(Debugger.IO){
            Debugger.println("readyOps = "+Debugger.ops(readyOps));
            Debugger.println("}");
        }
    }

    protected void _process() throws IOException{}

    @Override
    protected void _addInterestOps(int ops){
        if(selfInterestOps==0)
            peerInput.addInterestOps(ops);
    }

    @Override
    protected final int _selfReadyOps(){
        return isReadReady() ? OP_READ : 0;
    }

    protected abstract boolean isReadReady();

    @Override
    protected void _close() throws IOException{}
}
