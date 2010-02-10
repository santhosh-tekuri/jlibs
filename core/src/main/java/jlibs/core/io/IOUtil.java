/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.core.io;

import java.io.*;

/**
 * @author Santhosh Kumar T
 */
public class IOUtil{
    /*-------------------------------------------------[ Pumping ]---------------------------------------------------*/
    
    public static ByteArrayOutputStream pump(InputStream is, boolean closeIn) throws IOException{
        return pump(is, new ByteArrayOutputStream(), closeIn, true);
    }

    /**
     * Reads data from <code>is</code> and writes it into <code>os</code>.
     * <code>is</code> and <code>os</code> are closed if <code>closeIn</code> and <code>closeOut</code>
     * are true respectively.
     */
    public static <T extends OutputStream> T pump(InputStream is, T os, boolean closeIn, boolean closeOut) throws IOException{
        byte buff[] = new byte[1024];
        int len;
        try{
            while((len=is.read(buff))!=-1)
                os.write(buff, 0, len);
        }finally{
            try{
                if(closeIn)
                    is.close();
            }finally{
                if(closeOut)
                    os.close();
            }
        }
        return os;
    }

    public static CharArrayWriter pump(Reader reader, boolean closeReader) throws IOException{
        return pump(reader, new CharArrayWriter(), closeReader, true);
    }

    /**
     * Reads data from <code>reader</code> and writes it into <code>writer</code>.
     * <code>reader</code> and <code>writer</code> are closed if <code>closeReader</code> and <code>closeWriter</code>
     * are true respectively.
     */
    public static <T extends Writer> T pump(Reader reader, T writer, boolean closeReader, boolean closeWriter) throws IOException{
        char buff[] = new char[1024];
        int len;
        try{
            while((len=reader.read(buff))!=-1)
                writer.write(buff, 0, len);
        }finally{
            try{
                if(closeReader)
                    reader.close();
            }finally{
                if(closeWriter)
                    writer.close();
            }
        }
        return writer;
    }

    /*-------------------------------------------------[ Read-Fully ]---------------------------------------------------*/
    
    public static int readFully(InputStream in, byte b[]) throws IOException {
    	return readFully(in, b, 0, b.length);
    }

    public static int readFully(InputStream in, byte b[], int off, int len) throws IOException{
	    if(len<0)
	        throw new IndexOutOfBoundsException();
	    int n = 0;
	    while(n<len){
	        int count = in.read(b, off+n, len-n);
	        if(count<0)
		        return n;
	        n += count;
	    }
        return n;
    }
}
