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
public class HTTPLong extends SingleValueHeaderSpec<Long>{
    public HTTPLong(String name){
        super(name);
    }

    @Override
    protected Long _parse(String value, Version version){
        return value==null ? null : parseLong(value);
    }

    @Override
    public String format(Long value, Version version){
        return value==null ? null : value.toString();
    }

    public static long parseLong(String value){
        int len = value.length();
        if(len==0)
            throw new NumberFormatException("empty string");

        int i = 0;
        boolean negative = false;
        char firstChar = value.charAt(0);
        if(firstChar<'0'){
            if(firstChar=='-')
                negative = true;
            else if(firstChar!='+')
                throw new NumberFormatException("for input string \""+value+"\"");
            if(len==1)
                throw new NumberFormatException("cannot have lone + or -");
            ++i;
        }

        int result = 0;
        while(i<len){
            char ch = value.charAt(i++);
            if(ch>='0' && ch<='9')
                result = result*10 + (ch-'0');
            else
                throw new NumberFormatException("for input string \""+value+"\"");
        }
        return negative ? -result : result;
    }
}
