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

package jlibs.core.nio.channels;

/**
 * Uses Knuth-Morris-Pratt Algorithm for Pattern Matching
 * url: http://www.fmi.uni-sofia.bg/fmi/logic/vboutchkova/sources/KMPMatch_java.html
 *
 * @author Santhosh Kumar T
 */
public class BytePatternMatcher{
    private byte[] pattern;
    private int[] failure;

    public BytePatternMatcher(byte... pattern){
        this.pattern = pattern;

         // Computes the failure function using a boot-strapping process,
         // where the pattern is matched against itself.
        failure = new int[pattern.length];
        int j = 0;
        for(int i=1; i<pattern.length; i++){
            while(j>0 && pattern[j]!=pattern[i])
                j = failure[j-1];
            if(pattern[j]==pattern[i])
                j++;
            failure[i] = j;
        }
    }

    private int j = 0;
    public boolean matches(byte nextByte){
        while(j>0 && pattern[j]!=nextByte)
            j = failure[j - 1];
        if(pattern[j]==nextByte)
            j++;
        return j==pattern.length;
    }

    public void reset(){
        j = 0;
    }
}
