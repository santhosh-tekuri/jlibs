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

package jlibs.xml.sax.sniff.model.computed;

/**
 * @author Santhosh Kumar T
 */
public class NormalizeSpace extends StringComputedResult{
    @Override
    protected String transform(String result){
        return normalize(result);
    }

    public static String normalize(String str){
        char[] buffer = str.toCharArray();
        int write = 0;
        int lastWrite = 0;
        boolean wroteOne = false;
        int read = 0;
        while (read < buffer.length){
            if (isXMLSpace(buffer[read])){
                if (wroteOne)
                    buffer[write++] = ' ';

                do{
                    read++;
                }while(read < buffer.length && isXMLSpace(buffer[read]));
            }else{
                buffer[write++] = buffer[read++];
                wroteOne = true;
                lastWrite = write;
            }
        }

        return new String(buffer, 0, lastWrite);
    }


    private static boolean isXMLSpace(char c) {
        return c==' ' || c=='\n' || c=='\r' || c=='\t';
    }}
