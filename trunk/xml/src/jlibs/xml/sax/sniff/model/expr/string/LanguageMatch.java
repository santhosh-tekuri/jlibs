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
public class LanguageMatch extends Function{
    public LanguageMatch(Node contextNode){
        super(contextNode, Datatype.BOOLEAN, Datatype.STRING, Datatype.STRING);
    }

    @Override
    protected Object evaluate(Object[] args){
        String sublang = (String)args[0];
        String lang = (String)args[1];
        if(sublang.equalsIgnoreCase(lang))
            return true;

        int len = lang.length();
        return
            sublang.length() > len &&
            sublang.charAt(len) == '-' &&
            sublang.substring(0, len).equalsIgnoreCase(lang);
    }
}
