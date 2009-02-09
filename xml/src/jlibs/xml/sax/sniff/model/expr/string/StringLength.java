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

package jlibs.xml.sax.sniff.model.expr.string;

import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.expr.Function;

/**
 * @author Santhosh Kumar T
 */
public class StringLength extends Function{
    public StringLength(Node contextNode){
        super(contextNode, Datatype.NUMBER, Datatype.STRING);
    }

    @Override
    protected Object evaluate(Object[] args){
        return evaluate((String)args[0]);
    }

    // String.length() counts UTF-16 code points; not Unicode characters
    public static double evaluate(String str){
        int length = 0;
        for(int i=0; i<str.length(); i++){
            char c = str.charAt(i);
            length++;
            // if this is a high surrogate; assume the next character is
            // is a low surrogate and skip it
            if(c>=0xD800){
                try{
                    char low = str.charAt(i+1);
                    if (low < 0xDC00 || low > 0xDFFF)
                        throw new IllegalArgumentException("Bad surrogate pair in string " + str);
                    i++; // increment past low surrogate
                }catch(StringIndexOutOfBoundsException ex){
                    throw new IllegalArgumentException("Bad surrogate pair in string " + str);
                }
            }
        }
        return (double)length;
    }
}