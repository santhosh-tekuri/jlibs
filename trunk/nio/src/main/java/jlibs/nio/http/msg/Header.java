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
public final class Header{
    AsciiString name;
    String value;

    Header(AsciiString name){
        this.name = name;
    }

    public AsciiString getName(){ return name; }
    public String getValue(){ return value; }
    public void setValue(String value){
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public String toString(){ return name+": "+value; }

    Header sameNext;
    Header samePrev = this;

    public Header sameNext(){ return sameNext; }

    Header next;
    Header prev = this;

    public Header next(){ return next; }
}
