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
import jlibs.nio.Reactor;
import jlibs.nio.channels.InputChannel;
import jlibs.nio.channels.ListenerUtil;
import jlibs.nio.channels.OutputChannel;
import jlibs.nio.channels.impl.filters.InputFilterChannel;
import jlibs.nio.channels.impl.filters.OutputFilterChannel;

import java.io.IOException;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class AbstractSelectableChannel implements SelectableChannel{
    protected Reactor.Internal reactor;
    protected Client client;

    @Override
    public void initialize(Reactor.Internal reactor, Client client){
        this.reactor = reactor;
        this.client = client;
    }

    @Override
    public Client getClient(){
        return client;
    }

    /*-------------------------------------------------[ Ops ]---------------------------------------------------*/

    protected int interestOps;

    @Override
    public int interestOps(){
        return interestOps;
    }

    protected int readyOps;

    @Override
    public int readyOps(){
        return readyOps;
    }

    @Override
    public final void clearSelfReadyInterests(){
        interestOps &= ~selfReadyOps();
    }

    @Override
    public final void addInterestOps(int ops){
        if(Debugger.IO){
            boolean socket = this instanceof SocketIOChannel;
            Debugger.println(this+".addInterestOps("+Debugger.ops(ops)+")"+(socket?"":"{"));
        }
        interestOps |= ops;
        int selfReadyOps = selfReadyOps();
        if((selfReadyOps&ops)!=0)
            reactor.addToReadyList(this);
        else{
            ops &= ~selfReadyOps;
            if(ops!=0)
                _addInterestOps(ops);
        }
        if(Debugger.IO){
            if(!(this instanceof SocketIOChannel))
                Debugger.println("}");
        }
    }

    protected abstract void _addInterestOps(int ops);

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

    /*-------------------------------------------------[ Channel ]---------------------------------------------------*/

    private boolean open = true;

    @Override
    public final boolean isOpen(){
        return open;
    }

    @Override
    public final void close() throws IOException{
        if(open){
            if(Debugger.DEBUG)
                Debugger.println(this+".close{");
            open = false;
            try{
                _close();
            }finally{
                if(isClosed()){
                    if(this instanceof InputChannel)
                        ListenerUtil.closed((InputChannel)this);
                    if(this instanceof OutputChannel)
                        ListenerUtil.closed((OutputChannel)this);
                }
            }
            if(Debugger.DEBUG)
                Debugger.println("}");
        }
    }

    protected abstract void _close() throws IOException;

    /*-------------------------------------------------[ To-String ]---------------------------------------------------*/

    private static String completeToString(SelectableChannel channel){
        SelectableChannel peerChannel = null;
        if(channel instanceof InputFilterChannel)
            peerChannel = ((InputFilterChannel)channel).getPeerInput();
        else if(channel instanceof OutputFilterChannel)
            peerChannel = ((OutputFilterChannel)channel).getPeerOutput();
        if(peerChannel==null){
            if(channel instanceof SocketIOChannel){
                String str = ((SocketIOChannel)channel).client.toString();
                return "SocketIOChannel"+str.substring("Client".length());
            }else
                return channel==null ? "" : channel.getClass().getSimpleName();
        }else
            return channel.getClass().getSimpleName()+'('+completeToString(peerChannel)+')';
    }

    @Override
    public String toString(){
        boolean peekChannel;
        if(this instanceof SelectableInputChannel)
            peekChannel = ((SelectableInputChannel)this).getAppInput()==null;
        else
            peekChannel = ((SelectableOutputChannel)this).getAppOutput()==null;
        if(peekChannel)
            return completeToString(this);
        else
            return getClass().getSimpleName();
    }
}
