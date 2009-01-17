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

package jlibs.xml.sax.sniff.model.functions;

import jlibs.xml.sax.sniff.events.Event;

/**
 * @author Santhosh Kumar T
 */
public class Count extends ConsumableHitFunction{
    @Override
    public String getName(){
        return "count()";
    }

    @Override
    public String evaluate(Event event, String lastResult){
        if(lastResult==null)
            return "1";
        else
            return String.valueOf(Integer.parseInt(lastResult)+1);
    }
}
