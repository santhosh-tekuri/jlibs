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

import java.util.Date;

/**
 * @author Santhosh Kumar Tekuri
 */
public class NewCookie{
    public static final int DEFAULT_MAX_AGE = -1;

    public final String comment;
    public final int maxAge;
    public final Date expiry;
    public final boolean secure;
    public final boolean httpOnly;
    public final Cookie cookie;

    public NewCookie(Cookie cookie, String comment, int maxAge, Date expiry, boolean secure, boolean httpOnly){
        this.cookie = cookie;
        this.comment = comment;
        this.maxAge = maxAge;
        this.expiry = expiry;
        this.secure = secure;
        this.httpOnly = httpOnly;
    }

    public NewCookie(String newCookie){
        this(new Parser(false, newCookie));
    }

    public NewCookie(Parser parser){
        int version = -1;
        String name = null;
        String value = null;
        String path = null;
        String domain = null;
        String comment = null;
        int maxAge = DEFAULT_MAX_AGE;
        Date expiry = null;
        boolean secure = false;
        boolean httpOnly = false;

        while(true){
            String lvalue = parser.lvalue();
            if(lvalue==null)
                break;
            String rvalue = parser.rvalue();
            if("Version".equalsIgnoreCase(lvalue))
                version = Integer.parseInt(rvalue);
            else if("Comment".equalsIgnoreCase(lvalue))
                domain = rvalue;
            else if("Domain".equalsIgnoreCase(lvalue))
                domain = rvalue;
            else if("Path".equalsIgnoreCase(lvalue))
                path = rvalue;
            else if("Max-Age".equalsIgnoreCase(lvalue))
                maxAge = Integer.parseInt(rvalue);
            else if("Secure".equalsIgnoreCase(lvalue))
                secure = true;
            else if("HttpOnly".equalsIgnoreCase(lvalue))
                httpOnly = true;
            else if("Expires".equalsIgnoreCase(lvalue))
                expiry = HTTPDate.getInstance().parse(rvalue);
            else{
                name = lvalue;
                value = rvalue;
            }
        }

        this.cookie = new Cookie(version, name, value, path, domain);
        this.comment = comment;
        this.maxAge = maxAge;
        this.expiry = expiry;
        this.secure = secure;
        this.httpOnly = httpOnly;
    }

    @Override
    public int hashCode(){
        return cookie.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(obj==this)
            return true;
        if(obj instanceof NewCookie){
            NewCookie that = (NewCookie)obj;
            return this.cookie.equals(that.cookie);
        }else
            return false;
    }

    public String toString(){
        StringBuilder builder = Reactor.stringBuilder();
        Parser.appendValue(builder, cookie.name, cookie.value);
        if(cookie.version!=-1)
            builder.append(";Version=").append(cookie.version);
        if(comment!=null){
            builder.append(';');
            Parser.appendValue(builder, "Comment", comment);
        }
        if(cookie.domain!=null){
            builder.append(';');
            Parser.appendValue(builder, "Domain", cookie.domain);
        }
        if(cookie.path!=null){
            builder.append(';');
            Parser.appendValue(builder, "Path", cookie.path);
        }
        if(maxAge!=-1)
            builder.append(";Max-Age=").append(maxAge);
        if(secure)
            builder.append(";Secure");
        if(httpOnly)
            builder.append(";HttpOnly");
        if(expiry!=null){
            builder.append(";Expires=");
            builder.append(HTTPDate.getInstance().format(expiry));
        }
        return Reactor.free(builder);
    }
}