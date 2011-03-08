/**
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

package jlibs.core.nio;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;

/**
 * @author Santhosh Kumar T
 */
public abstract class NIOChannel extends AttachmentSupport implements Channel{
    protected final long id;
    protected final SelectableChannel channel;
    protected NIOChannel(long id, SelectableChannel channel) throws IOException{
        this.id = id;
        this.channel = channel;
        channel.configureBlocking(false);
    }

    protected NIOChannel(){
        channel = null;
        id = -1;
    }

    public long id(){
        return id;
    }

    public SelectableChannel realChannel(){
        return channel;
    }

    @Override
    public boolean isOpen(){
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException{
        channel.close();
    }

    protected boolean process(){
        return true;
    }
}
