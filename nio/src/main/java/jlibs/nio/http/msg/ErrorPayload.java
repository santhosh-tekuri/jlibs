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

package jlibs.nio.http.msg;

import jlibs.nio.http.util.MediaType;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ErrorPayload extends EncodablePayload{
    private Throwable thr;
    public ErrorPayload(Throwable thr){
        super(MediaType.TEXT_PLAIN.toString());
        this.thr = thr;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException{
        thr.printStackTrace(new PrintStream(out, true));
    }
}
