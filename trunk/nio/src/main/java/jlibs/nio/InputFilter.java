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

import jlibs.nio.filters.BufferInput;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class InputFilter implements Input{
    protected Input peer;
    protected final Socket transport;
    protected boolean eof;

    public InputFilter(Input peer){
        this.peer = peer;
        transport = peer.channel().transport;
        transport.peekIn = this;
    }

    protected void reattach(){
        peer = transport.peekIn;
        transport.peekIn = this;
    }

    @Override
    public NBStream channel(){
        return transport.channel();
    }

    public Input peer(){
        return peer;
    }

    @Override
    public Listener getInputListener(){
        return transport.getInputListener();
    }

    @Override
    public final void setInputListener(Input.Listener listener){
        transport.setInputListener(listener);
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException{
        return read(dsts, 0, dsts.length);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException{
        while(length>0){
            if(dsts[offset].hasRemaining())
                return read(dsts[offset]);
            ++offset;
            --length;
        }
        return 0;
    }

    @Override
    public long transferTo(long position, long count, FileChannel target) throws IOException{
        return target.transferFrom(this, position, count);
    }

    @Override
    public void addReadInterest(){
        if(transport.peekIn==this)
            transport.peekInInterested = true;
        if(readReady())
            wakeupReader();
        else
            peer.addReadInterest();
    }

    @Override
    public void wakeupReader(){
        transport.wakeupReader();
    }

    protected abstract boolean readReady();

    @Override
    public boolean eof(){
        return eof;
    }

    private boolean open = true;

    @Override
    public boolean isOpen(){
        return open;
    }

    @Override
    public final void close() throws IOException{
        if(open){
            open = false;
            doClose();
        }
    }

    protected void doClose(){}

    protected ByteBuffer detached(){
        return null;
    }

    @Override
    public Input detachInput(){
        if(transport.peekIn==this){
            ByteBuffer unread = detached();
            if(unread==null)
                return transport.peekIn = peer;
            else
                return transport.peekIn = new BufferInput(peer, unread);
        }else
            return this;
    }

    @Override
    public String toString(){
        String name = getClass().getSimpleName();
        if(name.endsWith("Input"))
            name = name.substring(0, name.length()-"Input".length());
        return name+'_'+peer;
    }
}
