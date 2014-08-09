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
import jlibs.nio.http.msg.spec.values.BasicCredentials;
import jlibs.nio.http.msg.spec.values.Credentials;
import jlibs.nio.http.msg.spec.values.DigestCredentials;
import jlibs.nio.http.msg.spec.values.UnknownCredentials;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Authorization extends SingleValueHeaderSpec<Credentials>{
    public Authorization(String name){
        super(name);
    }

    @Override
    protected Credentials _parse(String value, Version version){
        if(value==null)
            return null;
        int space = value.indexOf(' ');
        String scheme = value.substring(0, space);
        value = value.substring(space+1);
        if(BasicCredentials.SCHEME.equals(scheme))
            return new BasicCredentials(value);
        else if(DigestCredentials.SCHEME.equalsIgnoreCase(scheme))
            return new DigestCredentials(value);
        else
            return new UnknownCredentials(scheme, value);
    }

    @Override
    public String format(Credentials value, Version version){
        return value==null ? null : value.toString();
    }
}
