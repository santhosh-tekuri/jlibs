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

import java.util.Collection;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class MultiValueHeaderSpec<T> extends HeaderSpec{
    protected MultiValueHeaderSpec(String name){
        super(name);
    }

    protected abstract T parseSingle(Parser parser, Version version);
    protected abstract String formatSingle(T value, Version version);

    protected String fold(Collection<T> items, Version version){
        if(items==null || items.isEmpty())
            return null;

        StringBuilder buffer = new StringBuilder();
        for(T item: items){
            if(buffer.length()>0)
                buffer.append(',');
            buffer.append(formatSingle(item, version));
        }
        return buffer.toString();
    }

    public boolean canFold(){
        return true;
    }

    public void set(Message message, Collection<T> items){
        if(items==null || items.isEmpty())
            message.headers.remove(name);
        else if(canFold())
            message.headers.set(name, fold(items, message.version));
        else{
            message.headers.remove(name);
            for(T item: items)
                message.headers.add(name, formatSingle(item, message.version));
        }
    }
}
