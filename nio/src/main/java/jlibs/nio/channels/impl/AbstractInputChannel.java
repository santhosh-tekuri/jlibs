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
import jlibs.nio.Reactor;
import jlibs.nio.channels.InputLimitExceeded;
import jlibs.nio.channels.impl.filters.InputFilterChannel;
import jlibs.nio.util.Bytes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class AbstractInputChannel extends AbstractSelectableChannel implements SelectableInputChannel{
    private boolean eofSeen;
    public final boolean isEOF(){
        return eofSeen;
    }

    protected Bytes unread;
    public final void unread(ByteBuffer buffer){
        if(!buffer.hasRemaining())
            return;
        if(Debugger.IO)
            Debugger.println(this+".unread("+buffer.remaining()+")");
        eofSeen = false;
        if(unread==null)
            unread = new Bytes();
        unread.prepend(buffer);
        if(inputMetric>=0)
            inputMetric -= buffer.remaining();
        if(limit>=0)
            limit += buffer.remaining();
    }

    private int fillFromUnread(ByteBuffer dst){
        if(Debugger.IO)
            Debugger.println("fillFromUnread()");
        int pos = dst.position();
        while(!unread.isEmpty()){
            ByteBuffer buffer = unread.remove();
            int min = Math.min(dst.remaining(), buffer.remaining());
            int bufferLimit = buffer.limit();
            buffer.limit(buffer.position()+min);
            dst.put(buffer);
            buffer.limit(bufferLimit);

            if(buffer.hasRemaining()){
                assert !dst.hasRemaining();
                unread.prepend(buffer);
                break;
            }else
                Reactor.current().bufferPool.returnBack(buffer);
        }
        if(unread.isEmpty())
            unread = null;
        return dst.position()-pos;
    }

    @Override
    public final int selfReadyOps(){
        int ops = eofSeen||unread!=null ? SelectionKey.OP_READ: 0;
        return ops|_selfReadyOps();
    }

    protected abstract int _selfReadyOps();

    /*-------------------------------------------------[ ReadableByteChannel ]---------------------------------------------------*/

    @Override
    public final int read(ByteBuffer dst) throws IOException{
        if(!isOpen())
            throw new ClosedChannelException();
        if(Debugger.IO)
            Debugger.println(this+".read{");
        if(limit>=0 && available()>limit){
            limit = -1;
            throw new InputLimitExceeded();
        }
        int read = 0;
        if(unread!=null)
            read += fillFromUnread(dst);
        if(read>0){
            if(Debugger.IO){
                Debugger.println("return "+read);
                Debugger.println("}");
            }
            if(limit>=0){
                limit -= read;
                if(limit<0)
                    throw new InputLimitExceeded();
            }
            if(inputMetric>=0)
                inputMetric += read;
            return read;
        }

        read = _read(dst);
        eofSeen = read==-1;
        if(read!=-1 && inputMetric>=0)
            inputMetric += read;
        if(Debugger.IO){
            Debugger.println("return "+read);
            Debugger.println("}");
        }
        return read;
    }

    protected abstract int _read(ByteBuffer dst) throws IOException;

    /*-------------------------------------------------[ ScatteringByteChannel ]---------------------------------------------------*/

    @Override
    public final long read(ByteBuffer[] dsts) throws IOException{
        return read(dsts, 0, dsts.length);
    }

    @Override
    public final long read(ByteBuffer[] dsts, int offset, int length) throws IOException{
        if(!isOpen())
            throw new ClosedChannelException();
        if(Debugger.IO)
            Debugger.println(this+".read{");
        if(limit>=0 && available()>limit){
            limit = -1;
            throw new InputLimitExceeded();
        }
        long read = 0;
        while(length!=0 && unread!=null){
            read += fillFromUnread(dsts[offset]);
            offset++;
            length--;
        }
        if(read>0){
            if(Debugger.IO){
                Debugger.println("return "+read);
                Debugger.println("}");
            }
            if(limit>=0){
                limit -= read;
                if(limit<0)
                    throw new InputLimitExceeded();
            }
            if(inputMetric>=0)
                inputMetric += read;
            return read;
        }

        long tempMetric = inputMetric;
        inputMetric = -1;
        long tempLimit = limit;
        limit = -1;
        try{
            read = _read(dsts, offset, length);
        }finally{
            inputMetric = tempMetric;
            limit = tempLimit;
        }
        eofSeen = read==-1;
        if(read!=-1 && inputMetric>=0)
            inputMetric += read;
        if(Debugger.IO){
            Debugger.println("return "+read);
            Debugger.println("}");
        }
        return read;
    }

    protected long _read(ByteBuffer[] dsts, int offset, int length) throws IOException{
        long read = 0;
        while(length>0){
            if(dsts[offset].hasRemaining()){
                int r = _read(dsts[offset]);
                if(r==0)
                    break;
                else if(r==-1){
                    if(read==0)
                        read = -1;
                    break;
                }else
                    read += r;
            }
            offset++;
            length--;
        }
        return read;
    }

    /*-------------------------------------------------[ Limit ]---------------------------------------------------*/

    private long limit = -1;

    @Override
    public void setLimit(long limit){
        this.limit = limit;
    }

    /*-------------------------------------------------[ Metric ]---------------------------------------------------*/

    private long inputMetric = -1;

    @Override
    public final void startInputMetric(){
        inputMetric = 0;
    }

    @Override
    public final long getInputMetric(){
        return inputMetric;
    }

    @Override
    public final long stopInputMetric(){
        long t = inputMetric;
        inputMetric = -1;
        return t;
    }

    /*-------------------------------------------------[ Misc ]---------------------------------------------------*/

    private InputFilterChannel appInput;

    @Override
    public void setAppInput(InputFilterChannel input){
        appInput = input;
    }

    @Override
    public InputFilterChannel getAppInput(){
        return appInput;
    }

    private Listener listener;

    @Override
    public void setInputListener(Listener listener){
        this.listener = listener;
    }

    @Override
    public Listener getInputListener(){
        return listener;
    }
}
