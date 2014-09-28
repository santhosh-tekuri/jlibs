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

import jlibs.core.lang.StringUtil;
import jlibs.nio.Reactor;
import jlibs.nio.http.expr.Bean;
import jlibs.nio.http.expr.UnresolvedException;

import java.util.Objects;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Cookie implements Bean{
    public static final int VERSION_RFC2109 = 1;

    public final int version;
    public final String name;
    public final String value;
    public final String path;
    public final String domain;

    public Cookie(int version, String name, String value, String path, String domain){
        this.version = version;
        this.name = Objects.requireNonNull(name, "name==null");
        this.value = value;
        this.path = path;
        this.domain = domain;
    }

    public Cookie(String cookie){
        this(new Parser(true, cookie));
    }

    public Cookie(Parser parser){
        int version = -1;
        String name = null;
        String value = null;
        String path = null;
        String domain = null;

        while(true){
            String lvalue = parser.lvalue();
            if(lvalue==null)
                break;
            String rvalue = parser.rvalue();
            if("$Version".equalsIgnoreCase(lvalue))
                version = Integer.parseInt(rvalue);
            else if("$Domain".equalsIgnoreCase(lvalue))
                domain = rvalue;
            else if("$Path".equalsIgnoreCase(lvalue))
                path = rvalue;
            else{
                name = lvalue;
                value = rvalue;
            }
        }

        if(name==null)
            throw new NullPointerException("name==null");
        this.version = version;
        this.name = name;
        this.value = value;
        this.path = path;
        this.domain = domain;
    }

    @Override
    public int hashCode() {
        int h1 = name.toLowerCase().hashCode();
        int h2 = domain==null ? 0 : domain.toLowerCase().hashCode();
        int h3 = path==null ? 0 : path.hashCode();
        return h1 + h2 + h3;
    }

    @Override
    public boolean equals(Object obj){
        if(obj==this)
            return true;
        if(obj instanceof Cookie){
            Cookie that = (Cookie)obj;
            return StringUtil.equalsIgnoreCase(this.name, that.name) &&
                    StringUtil.equalsIgnoreCase(this.domain, that.domain) &&
                    Objects.equals(this.path, that.path);

        }else
            return false;
    }

    public String toString(){
        StringBuilder builder = Reactor.stringBuilder();
        if(version!=-1)
            builder.append("$Version=").append(version).append(';');
        Parser.appendValue(builder, name, value);
        if(domain!=null){
            builder.append(';');
            Parser.appendValue(builder, "$Domain", domain);
        }
        if(path!=null){
            builder.append(';');
            Parser.appendValue(builder, "$Path", path);
        }
        return Reactor.free(builder);
    }

    @Override
    @SuppressWarnings("StringEquality")
    public Object getField(String name) throws UnresolvedException{
        if(name=="name")
            return name;
        else if(name=="value")
            return value;
        else if(name=="version")
            return version;
        else if(name=="domain")
            return domain;
        else if(name=="path")
            return path;
        else
            throw new UnresolvedException(name);
    }
}
