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

package jlibs.xml.sax.sniff;

/**
 * @author Santhosh Kumar T
 */
public class Debugger{
    public int indent;
    private boolean indentationPrinted;
    private void printIndentation(boolean printed){
        if(!indentationPrinted){
            for(int i=0; i<indent; i++)
                System.out.print("  |");
            indentationPrinted = printed;
        }
    }

    public void println(String format, Object... args){
        printIndentation(false);
        System.out.format(format+"%n", args);
    }
    
    public void print(String format, Object... args){
        printIndentation(true);
        System.out.format(format, args);
    }
}
