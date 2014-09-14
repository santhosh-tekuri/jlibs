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

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class Challenge{
    public static final String REALM = "realm";

    public abstract String scheme();

    public static Challenge parse(String value){
        if(value==null)
            return null;
        int space = value.indexOf(' ');
        String scheme = value.substring(0, space);
        value = value.substring(space+1);
        if(BasicChallenge.SCHEME.equalsIgnoreCase(scheme)){
            Parser parser = new Parser(false, value);
            if(!Challenge.REALM.equalsIgnoreCase(parser.lvalue()))
                throw new IllegalArgumentException("realm not specified");
            return new BasicChallenge(parser.rvalue());
        }else if(DigestChallenge.SCHEME.equalsIgnoreCase(scheme))
            return new DigestChallenge(value);
        else
            return new UnknownChallenge(scheme, value);
    }
}