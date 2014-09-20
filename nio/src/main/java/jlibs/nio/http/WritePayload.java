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

package jlibs.nio.http;

import jlibs.nio.filters.ChunkedOutput;
import jlibs.nio.http.util.Encoding;
import jlibs.nio.listeners.Task;

import java.util.List;

import static java.nio.channels.SelectionKey.OP_WRITE;
import static jlibs.nio.Debugger.HTTP;
import static jlibs.nio.Debugger.println;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class WritePayload extends Task{
    protected WritePayload(){
        super(OP_WRITE);
    }

    boolean chunked;
    List<Encoding> encodings;

    protected final void setup(){
        if(chunked)
            out = new ChunkedOutput(out);
        if(encodings!=null){
            for(int i=encodings.size()-1; i>=0; --i)
                out = encodings.get(i).wrap(out);
        }
        if(HTTP)
            println("out = "+out);
    }
}
