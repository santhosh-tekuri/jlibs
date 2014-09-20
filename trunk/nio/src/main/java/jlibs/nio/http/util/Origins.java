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

import jlibs.nio.Reactor;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author Santhosh Kumar Tekuri
 */
public interface Origins{
    public static Origins NULL = new Origins(){
        @Override
        public String toString(){
            return "null";
        }
    };

    public static Origins ANY = new Origins(){
        @Override
        public String toString(){
            return "*";
        }
    };

    public static class List extends ArrayList<String> implements Origins{
        @Override
        public String toString(){
            if(isEmpty())
                return "null";
            if(size()==1)
                return get(0);

            StringBuilder builder = Reactor.stringBuilder();
            builder.append(get(0));
            for(int i=1; i<size(); ++i)
                builder.append(get(i));
            return Reactor.free(builder);
        }
    }

    public static Origins valueOf(String value){
        if(value==null || "null".equalsIgnoreCase(value))
            return Origins.NULL;
        if("*".equals(value))
            return Origins.ANY;

        List list = new List();
        StringTokenizer stok = new StringTokenizer(value);
        while(stok.hasMoreTokens())
            list.add(stok.nextToken());
        return list;
    }
}
