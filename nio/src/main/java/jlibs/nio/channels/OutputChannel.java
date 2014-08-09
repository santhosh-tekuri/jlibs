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

package jlibs.nio.channels;

import java.io.IOException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @author Santhosh Kumar Tekuri
 */
public interface OutputChannel extends BaseChannel, WritableByteChannel, GatheringByteChannel{
    public boolean isFlushed();
    public void addWriteInterest();
    public void setOutputListener(Listener listener);
    public Listener getOutputListener();
    public interface Listener{
        public void ready(OutputChannel out) throws IOException;
        public void timeout(OutputChannel out) throws IOException;
        public void closed(OutputChannel out) throws IOException;
        public void error(OutputChannel out, Throwable thr);
    }

    public void startOutputMetric();
    public long getOutputMetric();
    public long stopOutputMetric();
}
