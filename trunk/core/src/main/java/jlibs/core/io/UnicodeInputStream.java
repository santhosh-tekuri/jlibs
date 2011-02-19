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
import java.util.Arrays;

/**
 * @author Santhosh Kumar T
 */
public class UnicodeInputStream extends FilterInputStream{
    public final boolean hasBOM;
    public final BOM bom;

    public UnicodeInputStream(InputStream delegate) throws IOException{
        super(delegate);

        int len = IOUtil.readFully(delegate, marker);
        if(len<4)
            marker = Arrays.copyOf(marker, len);
        BOM bom = BOM.get(marker, true);
        if(bom!=null)
            imarker = bom.with().length;
        else
            bom = BOM.get(marker, false);
        this.bom = bom;
        hasBOM = imarker>0;
    }


    private byte marker[] = new byte[4];
    private int imarker;

    @Override
    public int read() throws IOException{
        if(marker!=null){
            int b = marker[imarker++];
            if(imarker==marker.length)
                marker = null;
            return b;
        }else
            return super.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException{
        int read = 0;
        while(marker!=null && len>0){
            b[off] = (byte)read();
            off++;
            len--;
            read++;
        }
        int r = super.read(b, off, len);
        if(read==0)
            return r;
        else
            return r==-1 ? read : read+r;
    }
}
