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
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ScatteringByteChannel;

/**
 * @author Santhosh Kumar Tekuri
 */
public interface InputChannel extends BaseChannel, ReadableByteChannel, ScatteringByteChannel{
    public default long available(){
        return 0;
    }
    public void unread(ByteBuffer buffer);
    public boolean isEOF();

    public void addReadInterest();
    public void setInputListener(Listener listener);
    public Listener getInputListener();
    public interface Listener{
        public void ready(InputChannel in) throws IOException;
        public void timeout(InputChannel in) throws IOException;
        public void closed(InputChannel in) throws IOException;
        public void error(InputChannel in, Throwable thr);
    }

    public void setLimit(long limit);
    public void startInputMetric();
    public long getInputMetric();
    public long stopInputMetric();
}
