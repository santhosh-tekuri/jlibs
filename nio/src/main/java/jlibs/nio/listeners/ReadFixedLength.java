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

package jlibs.nio.listeners;

import jlibs.nio.util.Buffers;

import java.io.IOException;

import static java.nio.channels.SelectionKey.OP_READ;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ReadFixedLength extends Task{
    private Buffers buffers;
    public ReadFixedLength(Buffers buffers, long readLimit){
        super(OP_READ);
        this.buffers = buffers;
        prepareReadFixedLength(readLimit);
    }

    @Override
    protected boolean process(int readyOp) throws IOException{
        return readFixedLength(buffers);
    }
}
