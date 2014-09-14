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

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HTTPDate{
    private static final ThreadLocal<HTTPDate> THREAD_LOCAL = ThreadLocal.withInitial(HTTPDate::new);
    public static HTTPDate getInstance(){
        return THREAD_LOCAL.get();
    }

    private static final TimeZone GMT= new SimpleTimeZone(0, "GMT");
    private static final FieldPosition DONT_CARE_FIELD_POSITION;
    static{
        FieldPosition fieldPositions[] = new FieldPosition[1];
        new SimpleDateFormat(){
            @Override
            public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos){
                fieldPositions[0] = pos;
                return super.format(date, toAppendTo, pos);
            }
        }.format(new Date());
        DONT_CARE_FIELD_POSITION = fieldPositions[0];
    }

    private StringBuffer buffer = new StringBuffer(35);
    private SimpleDateFormat formats[] = {
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'"), // RFC 822, updated by RFC 1123
            new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss 'GMT'"), // RFC 850, obsoleted by RFC 1036
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy") // ANSI C's asctime() format
    };

    private HTTPDate(){
        for(SimpleDateFormat sdf: formats)
            sdf.setTimeZone(GMT);
    }

    public Date parse(String value){
        if(value==null)
            return null;
        ParseException pe = null;
        for(SimpleDateFormat sdf: formats){
            try{
                return sdf.parse(value);
            }catch(ParseException ex){
                pe = ex;
            }
        }
        throw new RuntimeException(pe);
    }

    public String format(Date date){
        if(date==null)
            return null;
        buffer.setLength(0);
        return formats[0].format(date, buffer, DONT_CARE_FIELD_POSITION).toString();
    }

    private String currentDate;
    private long currentTime;
    public String currentDate(){
        long time = System.currentTimeMillis();
        if(currentDate==null || time-currentTime>=1000)
            currentDate = format(new Date(currentTime=time));
        return currentDate;
    }
}
