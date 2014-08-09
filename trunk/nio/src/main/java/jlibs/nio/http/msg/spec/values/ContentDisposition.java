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

package jlibs.nio.http.msg.spec.values;

import jlibs.nio.http.msg.Version;
import jlibs.nio.http.msg.spec.Parser;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static jlibs.nio.http.msg.Headers.CONTENT_LENGTH;
import static jlibs.nio.http.msg.Headers.LAST_MODIFIED;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ContentDisposition{
    public static final String TYPE_ATTACHMENT = "attachment";
    public static final String TYPE_FORM_DATA = "form-data";

    public String type;
    public Map<String, String> params = new HashMap<>();

    public ContentDisposition(String type){
        this.type = type;
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

    public static final String PARAM_NAME = "name";
    public String getName(){
        return params.get(PARAM_NAME);
    }

    public void setName(String name){
        putParam(PARAM_NAME, name);
    }

    public static final String PARAM_FILE_NAME = "filename";
    public String getFileName(){
        return params.get(PARAM_FILE_NAME);
    }

    public void setFileName(String fileName){
        putParam(PARAM_FILE_NAME, fileName);
    }

    public static final String PARAM_CREATION_DATE = "creation-date";
    public Date getCreationDate(){
        return LAST_MODIFIED.parse(params.get(PARAM_CREATION_DATE), Version.HTTP_1_1);
    }

    public void setCreationDate(Date date){
        putParam(PARAM_CREATION_DATE, LAST_MODIFIED.format(date, Version.HTTP_1_1));
    }

    public static final String PARAM_READ_DATE = "read-date";
    public Date getReadDate(){
        return LAST_MODIFIED.parse(params.get(PARAM_READ_DATE), Version.HTTP_1_1);
    }

    public void setReadDate(Date date){
        putParam(PARAM_READ_DATE, LAST_MODIFIED.format(date, Version.HTTP_1_1));
    }

    public static final String PARAM_SIZE = "size";
    public long getSize(){
        return CONTENT_LENGTH.parse(params.get(PARAM_SIZE), Version.HTTP_1_1);
    }

    public void setSize(long size){
        putParam(PARAM_SIZE, size<0 ? null : CONTENT_LENGTH.format(size, Version.HTTP_1_1));
    }

    @Override
    public String toString(){
        StringBuilder buffer = new StringBuilder();
        buffer.append(type);
        for(Map.Entry<String, String> param: params.entrySet()){
            buffer.append(';');
            Parser.appendValue(buffer, param.getKey(), param.getValue());
        }
        return buffer.toString();
    }
}
