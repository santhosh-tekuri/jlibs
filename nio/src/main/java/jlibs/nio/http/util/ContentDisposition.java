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

import jlibs.core.lang.Util;
import jlibs.nio.Reactor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ContentDisposition{
    public static final String TYPE_ATTACHMENT = "attachment";
    public static final String TYPE_FORM_DATA = "form-data";

    public String type;
    public Map<String, String> params = new HashMap<>();

    public ContentDisposition(String value){
        this(new Parser(true, value));
    }

    public ContentDisposition(Parser parser){
        type = parser.lvalue();
        parser.rvalue();
        while(true){
            String lvalue = parser.lvalue();
            if(lvalue==null)
                return;
            params.put(lvalue, parser.rvalue());
        }
    }

    public void putParam(String name, String value){
        if(value==null)
            params.remove(name);
        else
            params.put(name, value);
    }

    /*-------------------------------------------------[ name ]---------------------------------------------------*/

    public static final String PARAM_NAME = "name";

    public String getName(){
        return params.get(PARAM_NAME);
    }

    public void setName(String name){
        putParam(PARAM_NAME, name);
    }

    /*-------------------------------------------------[ filename ]---------------------------------------------------*/

    public static final String PARAM_FILE_NAME = "filename";

    public String getFileName(){
        return params.get(PARAM_FILE_NAME);
    }

    public void setFileName(String fileName){
        putParam(PARAM_FILE_NAME, fileName);
    }

    /*-------------------------------------------------[ creation-date ]---------------------------------------------------*/

    public static final String PARAM_CREATION_DATE = "creation-date";

    public Date getCreationDate(){
        return HTTPDate.getInstance().parse(params.get(PARAM_CREATION_DATE));
    }

    public void setCreationDate(Date date){
        putParam(PARAM_CREATION_DATE, HTTPDate.getInstance().format(date));
    }

    /*-------------------------------------------------[ read-date ]---------------------------------------------------*/

    public static final String PARAM_READ_DATE = "read-date";
    public Date getReadDate(){
        return HTTPDate.getInstance().parse(params.get(PARAM_READ_DATE));
    }

    public void setReadDate(Date date){
        putParam(PARAM_READ_DATE, HTTPDate.getInstance().format(date));
    }

    /*-------------------------------------------------[ size ]---------------------------------------------------*/

    public static final String PARAM_SIZE = "size";

    public long getSize(){
        String value = params.get(PARAM_SIZE);
        return value==null ? -1 : Util.parseLong(value);
    }

    public void setSize(long size){
        putParam(PARAM_SIZE, size<0 ? null : Long.toString(size));
    }

    @Override
    public String toString(){
        StringBuilder buffer = Reactor.stringBuilder();
        buffer.append(type);
        for(Map.Entry<String, String> param: params.entrySet()){
            buffer.append(';');
            Parser.appendValue(buffer, param.getKey(), param.getValue());
        }
        return Reactor.free(buffer);
    }
}