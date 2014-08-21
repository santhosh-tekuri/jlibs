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

package jlibs.nio.http.filters;

import jlibs.core.io.ByteArrayOutputStream2;
import jlibs.core.io.IOUtil;
import jlibs.nio.http.HTTPTask;
import jlibs.nio.http.encoders.FormURLEncoder;
import jlibs.nio.http.msg.*;
import jlibs.nio.http.msg.spec.values.MediaType;

import java.net.URLDecoder;
import java.util.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public class FormURLEncodedUnmarshalling implements HTTPTask.RequestFilter<HTTPTask>, HTTPTask.ResponseFilter<HTTPTask>{
    public static final FormURLEncodedUnmarshalling INSTANCE_REQUEST = new FormURLEncodedUnmarshalling(true);
    public static final FormURLEncodedUnmarshalling INSTANCE_RESPONSE = new FormURLEncodedUnmarshalling(false);
    private boolean parseRequest;

    public FormURLEncodedUnmarshalling(boolean parseRequest){
        this.parseRequest = parseRequest;
    }

    public FormURLEncodedUnmarshalling(){
        this(true);
    }

    @Override
    public void filter(HTTPTask task) throws Exception{
        Message message = parseRequest ? task.getRequest() : task.getResponse();
        Payload payload = message.getPayload();

        final String charset;
        if(payload.getContentLength()!=0 && payload.contentType!=null){
            MediaType mt = new MediaType(payload.contentType);
            charset = mt.isCompatible(MediaType.APPLICATION_FORM_URLENCODED) ? mt.getCharset(IOUtil.UTF_8.name()) : null;
        }else
            charset = null;

        if(charset==null){
            task.resume();
            return;
        }

        if(payload instanceof RawPayload)
            ((RawPayload)payload).removeEncodings();
        ByteArrayOutputStream2 bout = new ByteArrayOutputStream2();
        payload.transferTo(bout, (thr, timeout) -> {
            if(thr!=null)
                task.resume(parseRequest ? Status.BAD_REQUEST : Status.BAD_RESPONSE, thr);
            else if(timeout)
                task.resume(parseRequest ? Status.REQUEST_TIMEOUT : Status.RESPONSE_TIMEOUT);
            else{
                Map<String, List<String>> map = new HashMap<>();
                try{
                    StringTokenizer stok = new StringTokenizer(bout.toString(charset), "&");
                    while(stok.hasMoreTokens()){
                        String token = stok.nextToken();
                        int equals = token.indexOf('=');
                        String key = URLDecoder.decode(token.substring(0, equals), charset);
                        String value = URLDecoder.decode(token.substring(equals+1), charset);
                        List<String> list = map.get(key);
                        if(list==null)
                            map.put(key, list=new ArrayList<>(2));
                        list.add(value);
                    }
                }catch(Throwable thr1){
                    task.resume(parseRequest ? Status.BAD_REQUEST : Status.BAD_RESPONSE, thr1);
                    return;
                }
                try{
                    message.setPayload(new EncodablePayload<>(FormURLEncoder.MEDIA_TYPE.toString(), map, FormURLEncoder.INSTANCE), true);
                }catch(Throwable thr1){
                    task.resume(thr1);
                    return;
                }
                task.resume();
            }
        });
    }
}
