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

/**
 * @author Santhosh Kumar Tekuri
 */
public final class Version extends AsciiString{
    public final int major;
    public final int minor;
    public final boolean keepAliveDefault;
    public final boolean expectSupported;

    private Version(int major, int minor, boolean createBytes){
        super("HTTP/"+major+"."+minor);
        this.major = major;
        this.minor = minor;
        keepAliveDefault = major>=1 && minor>=1;
        expectSupported = major>=1 && minor>=1;
    }

    public static final Version HTTP_1_0 = new Version(1, 0, true);
    public static final Version HTTP_1_1 = new Version(1, 1, true);

    public static Version valueOf(int major, int minor){
        if(major==1){
            if(minor==1)
                return HTTP_1_1;
            if(minor==0)
                return HTTP_1_0;
        }
        return new Version(major, minor, false);
    }
}
