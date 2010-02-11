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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A FilterInputStream implemenation which writes the all the bytes that are read
 * from given inputstream to specifed outputstream. 
 *
 * @author Santhosh Kumar T
 */
public class TeeInputStream extends FilterInputStream{
    private final OutputStream delegate;
    private boolean closeDelegate;

    public TeeInputStream(InputStream in, OutputStream delegate, boolean closeDelegate){
        super(in);
        this.delegate = delegate;
        this.closeDelegate = closeDelegate;
    }

    @Override
    public int read() throws IOException{
        int ch = super.read();
        if(ch!=-1)
            delegate.write(ch);
        return ch;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException{
        len = super.read(b, off, len);
        if(len>0)
            delegate.write(b, off, len);
        return len;
    }

    @Override
    public void close() throws IOException{
        try{
            super.close();
        }finally{
            if(closeDelegate)
                delegate.close();
        }
    }
}
