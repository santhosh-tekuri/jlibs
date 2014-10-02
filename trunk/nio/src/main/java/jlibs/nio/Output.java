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
import java.nio.channels.GatheringByteChannel;

import static jlibs.nio.Debugger.DEBUG;
import static jlibs.nio.Debugger.IO;

/**
 * @author Santhosh Kumar Tekuri
 */
public interface Output extends GatheringByteChannel{
    public NBStream channel();
    public void addWriteInterest();
    public void wakeupWriter();

    @Override
    @Trace(condition=IO, args="\"src\"")
    int write(ByteBuffer src) throws IOException;

    @Override
    @Trace(condition=IO, args="\"srcs\"")
    long write(ByteBuffer[] srcs, int offset, int length) throws IOException;

    @Override
    @Trace(condition=IO, args="\"srcs\"")
    long write(ByteBuffer[] srcs) throws IOException;

    @Trace(condition=IO)
    public boolean flush() throws IOException;

    @Override
    @Trace(condition=DEBUG)
    void close() throws IOException;

    public Listener getOutputListener();
    public void setOutputListener(Listener listener);

    public Output detachOutput();

    @Trace(condition=IO, args="\"pos:\"+$2+\", count:\"+$3")
    public long transferFrom(FileChannel src, long position, long count) throws IOException;

    public interface Listener{
        public void process(Output out);
    }
}
