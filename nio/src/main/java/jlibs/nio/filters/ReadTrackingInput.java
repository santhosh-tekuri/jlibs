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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.function.Consumer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ReadTrackingInput extends TrackingInput{
    public ReadTrackingInput(Input peer, Consumer<TrackingInput> listener){
        super(peer, listener);
    }

    @Override
    public void addReadInterest(){
        if(listener!=null)
            notifyListener();
        super.addReadInterest();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException{
        if(listener!=null)
            notifyListener();
        return super.read(dst);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException{
        if(listener!=null)
            notifyListener();
        return super.read(dsts, offset, length);
    }

    @Override
    public long transferTo(long position, long count, FileChannel target) throws IOException{
        if(listener!=null)
            notifyListener();
        return super.transferTo(position, count, target);
    }
}
