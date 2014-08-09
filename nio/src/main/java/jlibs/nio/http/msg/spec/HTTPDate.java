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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#DateFormats
 *
 * @author Santhosh Kumar Tekuri
 */
public class HTTPDate extends SingleValueHeaderSpec<Date>{
    private static final TimeZone GMT= new SimpleTimeZone(0, "GMT");
    private static final ThreadLocal<SimpleDateFormat[]> FORMATS = ThreadLocal.withInitial(HTTPDate::formats);
    static SimpleDateFormat[] formats(){
        SimpleDateFormat formats[] = {
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'"), // RFC 822, updated by RFC 1123
            new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss 'GMT'"), // RFC 850, obsoleted by RFC 1036
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy") // ANSI C's asctime() format
        };
        for(SimpleDateFormat sdf: formats)
            sdf.setTimeZone(GMT);
        return formats;
    }

    public HTTPDate(String name){
        super(name);
    }

    @Override
    protected Date _parse(String value, Version version){
        if(value==null)
            return null;

        ParseException pe = null;
        for(SimpleDateFormat sdf: FORMATS.get()){
            try{
                return sdf.parse(value);
            }catch(ParseException ex){
                pe = ex;
            }
        }
        throw new RuntimeException(pe);
    }

    @Override
    public String format(Date value, Version version){
        return value==null ? null : FORMATS.get()[0].format(value);
    }
}
