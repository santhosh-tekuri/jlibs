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

package jlibs.nio.http.encoders;

import jlibs.core.io.IOUtil;
import jlibs.nio.http.msg.Encoder;
import jlibs.nio.http.msg.spec.values.MediaType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class FormURLEncoder implements Encoder<Map<?, ?>>{
    public static final MediaType MEDIA_TYPE = MediaType.APPLICATION_FORM_URLENCODED.withCharset(IOUtil.UTF_8.name());
    public static final FormURLEncoder INSTANCE = new FormURLEncoder();

    private FormURLEncoder(){}

    @Override
    @SuppressWarnings("unchecked")
    public void encodeTo(Map<?, ?> map, OutputStream out) throws IOException{
        boolean first = true;
        for(Map.Entry<?, ?> entry: map.entrySet()){
            String key = String.valueOf(entry.getKey());
            key = URLEncoder.encode(key, IOUtil.UTF_8.name());
            if(entry.getValue() instanceof Iterable){
                Iterable<Object> values = (Iterable<Object>)entry.getValue();
                for(Object value: values)
                    first = write(first, out, key, value);
            }else
                first = write(first, out, key, entry.getValue());
        }
    }

    private static boolean write(boolean first, OutputStream out, String key, Object value) throws IOException{
        if(!first)
            out.write('&');
        write(out, key);
        out.write('=');
        write(out, URLEncoder.encode(String.valueOf(value), IOUtil.UTF_8.name()));
        return false;
    }

    private static void write(OutputStream out, String str) throws IOException{
        int len = str.length();
        for(int i=0; i<len; i++)
            out.write(str.charAt(i));
    }
}
