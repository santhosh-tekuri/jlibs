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

import java.util.Objects;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class Method{
    public final String name;
    private Method(String name){
        this.name = Objects.requireNonNull(name, "name==null");
    }

    @Override
    public int hashCode(){
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        return obj==this || (obj instanceof Method && this.name.equalsIgnoreCase(((Method)obj).name));
    }

    @Override
    public String toString(){
        return name;
    }

    public static final Method GET = new Method("GET");
    public static final Method POST = new Method("POST");
    public static final Method DELETE = new Method("DELETE");
    public static final Method HEAD = new Method("HEAD");
    public static final Method PUT = new Method("PUT");
    public static final Method CONNECT = new Method("CONNECT");
    public static final Method PATCH = new Method("PATCH");
    public static final Method TRACE = new Method("TRACE");
    public static final Method OPTIONS = new Method("OPTIONS");

    public static Method valueOf(String str){
        if(GET.name.equalsIgnoreCase(str))
            return GET;
        else if(POST.name.equalsIgnoreCase(str))
            return POST;
        else if(DELETE.name.equalsIgnoreCase(str))
            return DELETE;
        else if(HEAD.name.equalsIgnoreCase(str))
            return HEAD;
        else if(PUT.name.equalsIgnoreCase(str))
            return PUT;
        else if(CONNECT.name.equalsIgnoreCase(str))
            return CONNECT;
        else if(PATCH.name.equalsIgnoreCase(str))
            return PATCH;
        else if(TRACE.name.equalsIgnoreCase(str))
            return TRACE;
        else if(OPTIONS.name.equalsIgnoreCase(str))
            return OPTIONS;
        else
            return new Method(str);
    }
}
