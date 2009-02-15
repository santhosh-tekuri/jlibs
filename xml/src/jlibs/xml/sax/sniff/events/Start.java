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

package jlibs.xml.sax.sniff.events;

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.sniff.engine.data.LocationStack;

/**
 * @author Santhosh Kumar T
 */
public class Start extends Event{
    public Start(DocumentOrder documentOrder, LocationStack locationStack){
        super(documentOrder, locationStack);
    }

    @Override
    public int type(){
        return START;
    }

    @Override
    protected String location(){
        throw new ImpossibleException();
    }

    @Override
    protected String value(){
        throw new ImpossibleException();
    }

    @Override
    public boolean hasChildren(){
        return true;
    }

    public void setData(){
        hit();
    }

    @Override
    public String toString(){
        return "";
    }
}