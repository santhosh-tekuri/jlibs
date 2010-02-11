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

package jlibs.core.io;

import jlibs.core.lang.ThrowableTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Santhosh Kumar T
 */
public class IOPump extends ThrowableTask<Void, IOException>{
    private InputStream is;
    private OutputStream os;
    private boolean closeIn;
    private boolean closeOut;

    public IOPump(InputStream is, OutputStream os, boolean closeIn, boolean closeOut){
        super(IOException.class);
        this.is = is;
        this.os = os;
        this.closeIn = closeIn;
        this.closeOut = closeOut;
    }

    @Override
    public Void run() throws IOException{
        IOUtil.pump(is, os, closeIn, closeOut);
        return null;
    }
}
