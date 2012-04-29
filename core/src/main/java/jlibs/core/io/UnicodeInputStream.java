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
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author Santhosh Kumar T
 */
public class UnicodeInputStream extends FilterInputStream{
    private ByteBuffer marker = ByteBuffer.allocate(4);
    public final String encoding;
    public final boolean hasBOM;

    public UnicodeInputStream(InputStream delegate) throws IOException{
        this(delegate, EncodingDetector.DEFAULT);
    }

    public UnicodeInputStream(InputStream delegate, EncodingDetector detector) throws IOException{
        super(delegate);

        int len = IOUtil.readFully(delegate, marker.array());
        marker.limit(len);

        encoding = detector.detect(marker);
        hasBOM = marker.position()>0;
        if(!marker.hasRemaining())
            marker = null;
    }

    @Override
    public int read() throws IOException{
        if(marker!=null){
            int b = marker.get() & 0xFF;
            if(!marker.hasRemaining())
                marker = null;
            return b;
        }else
            return super.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException{
        if(marker!=null){
            int read = Math.min(marker.remaining(), len);
            System.arraycopy(marker.array(), marker.position(), b, off, read);
            if(read==marker.remaining())
                marker = null;
            else
                marker.position(marker.position()+read);
            return read;
        }
        return super.read(b, off, len);
    }

    public InputStreamReader createReader(){
        if(encoding==null)
            return new InputStreamReader(this);
        else
            return new InputStreamReader(this, Charset.forName(encoding));
    }
}
