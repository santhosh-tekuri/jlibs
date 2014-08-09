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
import jlibs.nio.http.msg.spec.values.BasicChallenge;
import jlibs.nio.http.msg.spec.values.Challenge;
import jlibs.nio.http.msg.spec.values.DigestChallenge;
import jlibs.nio.http.msg.spec.values.UnknownChallenge;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Authenticate extends SingleValueHeaderSpec<Challenge>{
    public Authenticate(String name){
        super(name);
    }

    @Override
    protected Challenge _parse(String value, Version version){
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

    @Override
    public String format(Challenge value, Version version){
        return value==null ? null : value.toString();
    }
}
