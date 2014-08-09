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

import jlibs.nio.http.msg.Header;
import jlibs.nio.http.msg.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class MapHeaderSpec<T> extends MultiValueHeaderSpec<T>{
    protected MapHeaderSpec(String name){
        super(name);
    }

    public Map<String, T> get(Message message){
        Parser parser = null;
        Map<String, T> map = null;

        Header header = message.headers.get(name);
        while(header!=null){
            if(parser==null)
                parser = new Parser(canFold(), header.getValue());
            else
                parser.reset(header.getValue());
            if(!parser.isEmpty()){
                if(map==null)
                    map = new HashMap<>();
                while(true){
                    T item = parseSingle(parser, message.version);
                    map.put(getName(item), item);
                    if(parser.isEmpty())
                        break;
                    else
                        parser.skip();
                }
            }
            header = header.sameNext();
        }
        if(map==null)
            map = Collections.emptyMap();
        return map;
    }

    public void set(Message message, Map<String, T> map){
        super.set(message, map==null ? null : map.values());
    }

    protected abstract String getName(T item);
}
