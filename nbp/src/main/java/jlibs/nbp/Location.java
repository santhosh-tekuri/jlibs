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

package jlibs.nbp;

/**
 * @author Santhosh Kumar T
 */
public class Location{
    private int line, col, offset;
    public Location(){
        reset();
    }

    public int getLineNumber(){ return line; }
    public int getColumnNumber(){ return col; }
    public int getCharacterOffset(){ return offset; }

    private boolean skipLF;
    public void consume(char ch){
        offset++;
        if(skipLF && ch=='\n')
            skipLF = false;
        else{
            skipLF = false;
            switch(ch){
                case '\r':
                    skipLF = true;
                case '\n':
                    line++;
                    col = 0;
                    break;
                default:
                    col++;
            }
        }
    }

    public void reset(){
        line = col = offset = 0;
        skipLF = false;
    }
}
