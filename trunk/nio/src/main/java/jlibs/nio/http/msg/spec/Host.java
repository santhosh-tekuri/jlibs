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
import jlibs.nio.http.msg.spec.values.HostPort;

/**
 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.23
 *
 * @author Santhosh Kumar Tekuri
 */
public class Host extends SingleValueHeaderSpec<HostPort>{
    public Host(){
        super("Host");
    }

    @Override
    protected HostPort _parse(String value, Version version){
        if(value==null)
            return null;

        if(value.isEmpty())
            return new HostPort("");
        else{
            int colon = value.indexOf(':');
            if(colon==-1)
                return new HostPort(value);
            else
                return new HostPort(value.substring(0, colon), Integer.parseInt(value.substring(colon+1)));
        }
    }

    @Override
    public String format(HostPort value, Version version){
        return value==null ? null : value.toString();
    }
}
