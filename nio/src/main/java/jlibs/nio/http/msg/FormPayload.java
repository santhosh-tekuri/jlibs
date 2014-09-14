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

package jlibs.nio.http.msg;

import jlibs.core.io.IOUtil;
import jlibs.nio.http.util.MediaType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
public class FormPayload extends EncodablePayload{
    private static final String CONTENT_TYPE = MediaType.APPLICATION_FORM_URLENCODED.withCharset(IOUtil.UTF_8.name()).toString();

    public final Map<String, List<String>> map;
    public FormPayload(Map<String, List<String>> map){
        super(CONTENT_TYPE);
        this.map = map;
    }

    public FormPayload(){
        this(new HashMap<>());
    }

    public void addParam(String name, String value){
        List<String> list = map.get(name);
        if(list==null)
            map.put(name, list=new ArrayList<>());
        list.add(value);
    }

    public String getParam(String name){
        List<String> list = map.get(name);
        return list==null || list.isEmpty() ? null : list.get(0);
    }

    public void setParam(String name, String value){
        List<String> list = map.get(name);
        if(list==null)
            map.put(name, list=new ArrayList<>());
        list.clear();
        list.add(value);
    }

    @Override
    public void writeTo(OutputStream out) throws IOException{
        boolean first = true;
        for(Map.Entry<String, List<String>> entry: map.entrySet()){
            String key = URLEncoder.encode(entry.getKey(), IOUtil.UTF_8.name());
            for(String value: entry.getValue()){
                if(first){
                    out.write('&');
                    first = false;
                }
                write(out, key);
                out.write('=');
                write(out, URLEncoder.encode(value, IOUtil.UTF_8.name()));
            }
        }
    }

    private static void write(OutputStream out, String str) throws IOException{
        int len = str.length();
        for(int i=0; i<len; i++)
            out.write(str.charAt(i));
    }
}
