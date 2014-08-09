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

import jlibs.nio.http.msg.Message;
import jlibs.nio.http.msg.Version;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class SingleValueHeaderSpec<T> extends HeaderSpec{
    protected SingleValueHeaderSpec(String name){
        super(name);
    }

    protected abstract T _parse(String value, Version version);
    public T parse(String value, Version version){
        if(value!=null && value.startsWith("\""))
            value = new Parser(false, value).value();
        return _parse(value, version);
    }

    public abstract String format(T value, Version version);

    public T get(Message message){
        return parse(message.headers.value(name), message.version);
    }

    public String set(Message message, T value){
        String stringValue = format(value, message.version);
        if(stringValue==null)
            message.headers.remove(name);
        else
            message.headers.set(name, stringValue);
        return stringValue;
    }
}
