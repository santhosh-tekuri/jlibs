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

/**
 * @author Santhosh Kumar Tekuri
 */
public class LimitInput extends InputFilter{
    private long limit;
    public LimitInput(Input peer, long limit){
        super(peer);
        this.limit = limit;
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
        if(peer.available()>limit)
            throw InputLimitExceeded.INSTANCE;
        long read = peer.read(dsts, offset, length);
        if(read>0){
            if(read>limit)
                throw InputLimitExceeded.INSTANCE;
            limit -= read;
        }
        return read;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException{
        if(peer.available()>limit)
            throw InputLimitExceeded.INSTANCE;
        int read = peer.read(dst);
        if(read>0){
            if(read>limit)
                throw InputLimitExceeded.INSTANCE;
            limit -= read;
        }
        return read;
    }

    @Override
    public boolean eof(){
        return peer.eof();
    }
}
