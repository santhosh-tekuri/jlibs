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

package jlibs.nio.filters;

import jlibs.nio.Input;
import jlibs.nio.InputFilter;
import jlibs.nio.Reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.function.Consumer;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class TrackingInput extends InputFilter{
    protected Consumer<TrackingInput> listener;
    public TrackingInput(Input peer, Consumer<TrackingInput> listener){
        super(peer);
        this.listener = listener;
    }

    @Override
    protected boolean readReady(){
        return false;
    }

    @Override
    public long available(){
        return peer.available();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException{
        return peer.read(dst);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException{
        return peer.read(dsts, offset, length);
    }

    @Override
    public long transferTo(long position, long count, FileChannel target) throws IOException{
        return peer.transferTo(position, count, target);
    }

    @Override
    public boolean eof(){
        return peer.eof();
    }

    protected void notifyListener(){
        Consumer<TrackingInput> listener = this.listener;
        this.listener = null;
        try{
            listener.accept(this);
        }catch(Throwable thr){
            Reactor.current().handleException(thr);
        }
    }

    public void reattach(){
        super.reattach();
    }
}
