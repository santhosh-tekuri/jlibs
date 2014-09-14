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

package jlibs.nio.util;

/**
 * @author Santhosh Kumar Tekuri
 */
public class BytePattern{
    public byte[] pattern;
    private int[] failure;

    public BytePattern(byte... pattern){
        this.pattern = pattern;

        // Computes the failure function using a boot-strapping process,
        // where the pattern is matched against itself.
        failure = new int[pattern.length];
        int j = 0;
        for(int i=1; i<pattern.length; i++){
            j = match(j, pattern[i]);
            failure[i] = j;
        }
    }

    public int length(){
        return pattern.length;
    }

    public int match(int j, byte nextByte){
        while(j>0 && pattern[j]!=nextByte)
            j = failure[j-1];
        if(pattern[j]==nextByte)
            ++j;
        return j;
    }
}
