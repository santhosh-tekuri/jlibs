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

package jlibs.nio.http.util;

import jlibs.core.io.IOUtil;

import java.util.Base64;

import static java.util.Objects.requireNonNull;

/**
 * @author Santhosh Kumar Tekuri
 */
public class BasicCredentials implements Credentials{
    public static final String SCHEME = "Basic";

    public final String user;
    public final String password;

    public BasicCredentials(String user, String password){
        this.user = requireNonNull(user, "user==null");
        this.password = requireNonNull(password, "password==null");
    }

    @Override
    public String scheme(){
        return SCHEME;
    }

    private String toString;
    @Override
    public String toString(){
        if(toString==null)
            toString = SCHEME+' '+Base64.getEncoder().encodeToString((user+':'+password).getBytes(IOUtil.US_ASCII));
        return toString;
    }

    public static BasicCredentials valueOf(String value){
        value = new String(Base64.getDecoder().decode(value), IOUtil.US_ASCII);
        int colon = value.indexOf(':');
        String user = value.substring(0, colon);
        String password = value.substring(colon+1);
        return new BasicCredentials(user, password);
    }
}