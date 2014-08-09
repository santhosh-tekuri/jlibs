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

package jlibs.nio.channels.filters;

import jlibs.nio.channels.impl.filters.AbstractInputFilterChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class TrackingInputFilter extends AbstractInputFilterChannel{
    protected Consumer<TrackingInputFilter> listener;
    public TrackingInputFilter(Consumer<TrackingInputFilter> listener){
        this.listener = listener;
    }

    @Override
    public long available(){
        return peerInput.available();
    }

    @Override
    protected boolean isReadReady(){
        return false;
    }

    @Override
    protected int _read(ByteBuffer dst) throws IOException{
        return peerInput.read(dst);
    }

    protected void notifyListener(){
        Consumer<TrackingInputFilter> listener = this.listener;
        this.listener = null;
        listener.accept(this);
    }
}