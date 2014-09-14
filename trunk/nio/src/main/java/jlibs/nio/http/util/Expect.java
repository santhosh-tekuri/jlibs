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
public final class Expect{
    public final String value;
    private Expect(String value){
        this.value = Objects.requireNonNull(value, "value==null");
    }

    @Override
    public int hashCode(){
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        return obj==this || (obj instanceof Expect && this.value.equalsIgnoreCase(((Expect)obj).value));
    }

    @Override
    public String toString(){
        return value;
    }

    public static final Expect CONTINUE_100 = new Expect("100-continue");
    public static Expect valueOf(String value){
        if(CONTINUE_100.value.equalsIgnoreCase(value))
            return CONTINUE_100;
        else
            return new Expect(value);
    }
}
