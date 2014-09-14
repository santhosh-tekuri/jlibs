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
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Connection<T extends SelectableChannel> extends NBStream<T> implements Readable, Writable{
    protected Connection(T selectable, SelectionKey selectionKey) throws IOException{
        super(selectable, selectionKey);
    }

    @Override
    public Input in(){
        return transport.peekIn;
    }

    @Override
    public Output out(){
        return transport.peekOut;
    }

    Connection poolPrev, poolNext;
    String poolKey;
}
