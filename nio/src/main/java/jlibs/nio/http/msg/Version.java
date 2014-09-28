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

package jlibs.nio.http.msg;

import jlibs.nio.http.expr.Bean;
import jlibs.nio.http.expr.UnresolvedException;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class Version extends AsciiString implements Bean{
    public final int major;
    public final int minor;
    public final boolean keepAliveDefault;
    public final boolean expectSupported;

    private Version(int major, int minor){
        super("HTTP/"+major+"."+minor);
        this.major = major;
        this.minor = minor;
        keepAliveDefault = major>=1 && minor>=1;
        expectSupported = major>=1 && minor>=1;
    }

    @Override
    @SuppressWarnings("StringEquality")
    public Object getField(String name) throws UnresolvedException{
        if(name=="major")
            return major;
        else if(name=="minor")
            return minor;
        else
            throw new UnresolvedException(name);
    }

    public static final Version HTTP_1_0 = new Version(1, 0);
    public static final Version HTTP_1_1 = new Version(1, 1);

    public static Version valueOf(int major, int minor){
        if(major==1){
            if(minor==1)
                return HTTP_1_1;
            if(minor==0)
                return HTTP_1_0;
        }
        return new Version(major, minor);
    }
}
