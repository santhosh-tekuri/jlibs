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

import java.util.Objects;

/**
 * @author Santhosh Kumar Tekuri
 */
public class BasicChallenge extends Challenge{
    public static final String SCHEME = "Basic";

    public final String realm;

    public BasicChallenge(String realm){
        this.realm = Objects.requireNonNull(realm, "realm==null");
    }

    @Override
    public String scheme(){
        return SCHEME;
    }

    private String toString;

    @Override
    public String toString(){
        if(toString==null){
            StringBuilder buffer = new StringBuilder();
            buffer.append(SCHEME).append(' ');
            Parser.appendQuotedValue(buffer, REALM, realm);
            toString = buffer.toString();
        }
        return toString;
    }
}