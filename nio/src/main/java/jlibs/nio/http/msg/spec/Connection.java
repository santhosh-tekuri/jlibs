/*
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

package jlibs.nio.http.msg.spec;

import jlibs.nio.http.msg.Version;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Connection extends SingleValueHeaderSpec<Boolean>{
    public static final String CLOSE = "close";
    public static final String KEEP_ALIVE = "keep-alive";

    public Connection(String name){
        super(name);
    }

    @Override
    protected Boolean _parse(String value, Version version){
        if(version.keepAliveDefault)
            return !(value!=null && CLOSE.equalsIgnoreCase(value));
        else
            return value!=null && KEEP_ALIVE.equalsIgnoreCase(value);
    }

    @Override
    public String format(Boolean value, Version version){
        if(version.keepAliveDefault)
            return value ? null : CLOSE;
        else
            return value ? KEEP_ALIVE : null;
    }
}
