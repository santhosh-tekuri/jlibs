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

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Santhosh Kumar Tekuri
 */
public class FixedLengthInput extends InputFilter{
    private long available;
    public FixedLengthInput(Input peer, long available){
        super(peer);
        this.available = available;
    }

    @Override
    protected boolean readReady(){
        return available==0;
    }

    @Override
    public long available(){
        return available;
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException{
        if(available==0){
            eof = true;
            return -1;
        }

        long remaining = 0;
        ByteBuffer dst = null;
        for(int i=0; i<length; i++){
            dst = dsts[offset+i];
            remaining += remaining+dst.remaining();
            if(remaining==available){
                length = i+1;
                dst = null;
                break;
            }else if(remaining<available)
                dst = null;
            else{
                length = i+1;
                break;
            }
        }

        int dstLimit = dst==null ? 0 : dst.limit();
        if(dst!=null && remaining>available)
            dst.limit(dst.limit()-(int)(remaining-available));
        try{
            long read = peer.read(dsts, offset, length);
            if(read==-1)
                throw new EOFException(available+" more bytes expected");
            available -= read;
            return read;
        }finally{
            if(dst!=null)
                dst.limit(dstLimit);
        }
    }

    @Override
    public int read(ByteBuffer dst) throws IOException{
        if(available==0){
            eof = true;
            return -1;
        }

        int min = (int)Math.min(available, dst.remaining());
        int dstLimit = dst.limit();
        dst.limit(dst.position()+min);
        try{
            int read = peer.read(dst);
            if(read==-1)
                throw new EOFException(available+" more bytes expected");
            available -= read;
            return read;
        }finally{
            dst.limit(dstLimit);
        }
    }

    @Override
    public long transferTo(long position, long count, FileChannel target) throws IOException{
        if(available==0){
            eof = true;
            return 0;
        }
        long wrote = peer.transferTo(position, Math.min(count, available), target);
        available -= wrote;
        return wrote;
    }

    @Override
    public String toString(){
        return "FixedLength["+available+"]_"+peer;
    }
}
