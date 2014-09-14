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

/**
 * @author Santhosh Kumar Tekuri
 */
public class PipedConnection extends Connection<SelectableChannel>{
    private ReadPipe readPipe;
    private WritePipe writePipe;
    public PipedConnection(ReadPipe readPipe, WritePipe writePipe) throws IOException{
        super(null, null);
        this.readPipe = readPipe;
        this.writePipe = writePipe;
    }

    @Override
    protected void process(boolean timeout){
        throw new UnsupportedOperationException();
    }

    @Override
    protected void wakeupNow(){
        throw new UnsupportedOperationException();
    }

    @Override
    public Input in(){
        return readPipe.in();
    }

    @Override
    public Output out(){
        return writePipe.out();
    }

    public boolean isOpen(){
        return readPipe.isOpen() & writePipe.isOpen();
    }

    @Override
    public void close(){
        writePipe.close();
        readPipe.close();
    }
}
