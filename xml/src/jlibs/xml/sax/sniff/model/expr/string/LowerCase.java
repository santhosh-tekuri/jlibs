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

import java.util.Locale;

/**
 * @author Santhosh Kumar T
 */
public class LowerCase extends Function{
    public LowerCase(Node contextNode){
        super(contextNode, Datatype.STRING, Datatype.STRING, Datatype.STRING);
    }

    @Override
    protected Object evaluate(Object[] args){
        Locale locale = Locale.ENGLISH;
        if(args.length>1){
            locale = UpperCase.findLocale((String)args[1]);
            if(locale==null)
                locale = Locale.ENGLISH;
        }

        return ((String)args[0]).toLowerCase(locale);
    }
}