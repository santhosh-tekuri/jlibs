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
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;

/**
 * @author Santhosh Kumar Tekuri
 */
public interface Output extends GatheringByteChannel{
    public NBStream channel();
    public void addWriteInterest();
    public boolean flush() throws IOException;
    public Listener getOutputListener();
    public void setOutputListener(Listener listener);
    public void wakeupWriter();
    public Output detachOutput();
    public long transferFrom(FileChannel src, long position, long count) throws IOException;

    public interface Listener{
        public void process(Output out);
    }
}
