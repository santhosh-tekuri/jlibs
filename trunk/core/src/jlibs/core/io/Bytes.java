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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author Santhosh Kumar T
 */
public class Bytes{
    private ByteArrayOutputStream bout;

    public Bytes(){
        bout = new MyByteArrayOutputStream();
    }

    public Bytes(int size){
        bout = new MyByteArrayOutputStream(size);
    }

    public ByteArrayOutputStream out(){
        return bout;
    }

    public ByteArrayInputStream in(){
        return new ByteArrayInputStream(bout.toByteArray(), 0, bout.size());
    }

    private static class MyByteArrayOutputStream extends ByteArrayOutputStream{
        MyByteArrayOutputStream(){
        }

        MyByteArrayOutputStream(int size){
            super(size);
        }

        @Override
        public byte[] toByteArray(){
            return buf;
        }
    }
}
