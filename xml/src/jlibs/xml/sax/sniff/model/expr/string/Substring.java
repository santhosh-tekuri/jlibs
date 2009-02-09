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
import jlibs.xml.sax.sniff.model.expr.num.Round;

/**
 * @author Santhosh Kumar T
 */
public class Substring extends Function{
    public Substring(Node contextNode){
        super(contextNode, Datatype.STRING, Datatype.STRING, Datatype.NUMBER, Datatype.NUMBER);
    }

    @Override
    protected Object evaluate(Object[] args){
        String str = (String)args[0];
        if(str==null)
            return "";

        int stringLength = (int)StringLength.evaluate(str);
        if(stringLength==0)
            return "";

        Double d1 = (Double)args[1];
        if(d1.isNaN())
            return "";

        int start = (int)Round.evaluate(d1) - 1; // subtract 1 as Java strings are zero based

        int substringLength = stringLength;
        if(args.length==3){
            Double d2 = (Double)args[2];
            if(!d2.isNaN())
                substringLength = (int)Round.evaluate(d2);
            else
                substringLength = 0;
        }

        if (substringLength<0)
            return "";

        int end = start + substringLength;
        if(args.length==2)
            end = stringLength;

        if(start<0) // negative start is treated as 0
            start = 0;
        else if(start>stringLength)
            return "";

        if(end>stringLength)
            end = stringLength;
        else if(end<start)
            return "";

        if(stringLength==str.length()) // // easy case; no surrogate pairs
            return str.substring(start, end);
        else
            return unicodeSubstring(str, start, end);
    }

    private static String unicodeSubstring(String s, int start, int end){
        StringBuffer result = new StringBuffer(s.length());
        for(int jChar=0, uChar=0; uChar<end; jChar++, uChar++){
            char c = s.charAt(jChar);
            if(uChar>=start)
                result.append(c);
            if(c>=0xD800){ // get the low surrogate
                // ???? we could check here that this is indeed a low surroagte
                // we could also catch StringIndexOutOfBoundsException
                jChar++;
                if(uChar>=start)
                    result.append(s.charAt(jChar));
            }
        }
        return result.toString();
    }
}
