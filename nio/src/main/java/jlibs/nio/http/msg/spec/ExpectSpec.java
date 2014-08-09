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
import jlibs.nio.http.msg.spec.values.Expect;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ExpectSpec extends SingleValueHeaderSpec<Expect>{
    public static final Expect CONTINUE = Expect.CONTINUE_100;

    public ExpectSpec(){
        super("Expect");
    }

    @Override
    protected Expect _parse(String value, Version version){
        return value==null ? null : Expect.valueOf(value);
    }

    @Override
    public String format(Expect value, Version version){
        return value==null ? null : value.toString();
    }
}
