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

import jlibs.nio.util.Line;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class Version implements Comparable<Version>{
    public final String text;
    public final String protocol;
    public final int majorVersion;
    public final int minorVersion;
    public final boolean keepAliveDefault;

    private Version(String text, boolean keepAliveDefault){
        this.text = text;
        this.keepAliveDefault = keepAliveDefault;

        int slash = text.indexOf('/');
        protocol = text.substring(0, slash);
        int dot = text.indexOf('.', slash+1);
        majorVersion = Integer.parseInt(text.substring(slash+1, dot));
        minorVersion = Integer.parseInt(text.substring(dot+1));
    }

    @Override
    public int compareTo(Version that){
        if(that==null)
            return -1;
        int result = Integer.compare(this.majorVersion, that.majorVersion);
        return result==0 ? Integer.compare(this.minorVersion, that.minorVersion) : result;
    }

    @Override
    public boolean equals(Object that){
        return that==this || (that instanceof Version && text.equalsIgnoreCase(((Version)that).text));
    }

    @Override
    public String toString(){
        return text;
    }

    public static final Version HTTP_1_0 = new Version("HTTP/1.0", false);
    public static final Version HTTP_1_1 = new Version("HTTP/1.1", true);

    public static Version valueOf(String text){
        if(HTTP_1_1.text.equalsIgnoreCase(text))
            return HTTP_1_1;
        else if(HTTP_1_0.text.equalsIgnoreCase(text))
            return HTTP_1_0;
        else
            return new Version(text, true);
    }

    public static Version valueOf(Line line, int start, int end){
        if(end-start==8 && line.equalsIgnoreCase(start, end-1, "HTTP/1.")){
            char minorVersion = line.array()[end-1];
            if(minorVersion=='1')
                return HTTP_1_1;
            else if(minorVersion=='0')
                return HTTP_1_0;
        }
        return new Version(line.substring(start, end), true);
    }
}
