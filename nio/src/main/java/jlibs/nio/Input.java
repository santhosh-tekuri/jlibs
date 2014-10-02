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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ScatteringByteChannel;

import static jlibs.nio.Debugger.DEBUG;
import static jlibs.nio.Debugger.IO;

/**
 * @author Santhosh Kumar Tekuri
 */
public interface Input extends ScatteringByteChannel{
    @Override
    @Trace(condition=IO, args="\"dst\"")
    int read(ByteBuffer dst) throws IOException;

    @Override
    @Trace(condition=IO, args="\"dsts\"")
    long read(ByteBuffer[] dsts, int offset, int length) throws IOException;

    @Override
    @Trace(condition=IO, args="\"dsts\"")
    long read(ByteBuffer[] dsts) throws IOException;

    @Override
    @Trace(condition=DEBUG)
    void close() throws IOException;

    public NBStream channel();
    public void addReadInterest();
    public long available();
    public boolean eof();
    public Listener getInputListener();
    public void setInputListener(Listener listener);
    public void wakeupReader();
    public Input detachInput();

    @Trace(condition=IO, args="\"pos:\"+$1+\", count:\"+$2")
    public long transferTo(long position, long count, FileChannel target) throws IOException;

    public interface Listener{
        public void process(Input in);
    }
}
