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
import jlibs.nio.http.msg.Version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class ListHeaderSpec<T> extends MultiValueHeaderSpec<T>{
    protected ListHeaderSpec(String name){
        super(name);
    }

    public List<T> get(Message message){
        Parser parser = null;
        List<T> list = null;

        Header header = message.headers.get(name);
        while(header!=null){
            if(parser==null)
                parser = new Parser(canFold(), header.getValue());
            else
                parser.reset(header.getValue());
            if(!parser.isEmpty()){
                if(list==null)
                    list = new ArrayList<>();
                while(true){
                    list.add(parseSingle(parser, message.version));
                    if(parser.isEmpty())
                        break;
                    else
                        parser.skip();
                }
            }
            header = header.sameNext();
        }
        if(list==null)
            list = Collections.emptyList();
        return list;
    }

    public List<T> parse(String value, Version version){
        List<T> list = null;
        Parser parser = new Parser(canFold(), value);
        if(!parser.isEmpty()){
            list = new ArrayList<>();
            while(true){
                list.add(parseSingle(parser, version));
                if(parser.isEmpty())
                    break;
                else
                    parser.skip();
            }
        }
        if(list==null)
            list = Collections.emptyList();
        return list;
    }
}
