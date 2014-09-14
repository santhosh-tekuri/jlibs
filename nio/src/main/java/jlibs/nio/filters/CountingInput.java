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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Santhosh Kumar Tekuri
 */
public class CountingInput extends InputFilter{
    private long count = 0;
    public CountingInput(Input peer){
        super(peer);
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
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException{
        long read = peer.read(dsts, offset, length);
        if(read>0)
            count += read;
        return read;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException{
        int read = peer.read(dst);
        if(read>0)
            count += read;
        return read;
    }

    @Override
    public long transferTo(long position, long count, FileChannel target) throws IOException{
        long wrote = super.transferTo(position, count, target);
        this.count += wrote;
        return wrote;
    }

    @Override
    public boolean eof(){
        return peer.eof();
    }

    public long getTotalRead(){
        long total = count;
        InputFilter in = (InputFilter)((jlibs.nio.Readable)channel()).in();
        while(in!=this){
            total -= in.available();
            in = (InputFilter)in.peer();
        }
        return total;
    }
}
