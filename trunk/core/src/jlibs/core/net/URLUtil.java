/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.core.net;

import jlibs.core.io.FileUtil;
import jlibs.core.lang.StringUtil;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.ArrayUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Santhosh Kumar T
 */
public class URLUtil{
    public static URI toURI(URL url){
        try{
            return url.toURI();
        }catch(URISyntaxException ex){
            throw new RuntimeException(ex);
        }
    }

    public static URL toURL(String systemID){
        if(StringUtil.isWhitespace(systemID))
            return null;
        systemID = systemID.trim();
        try{
            return new URL(systemID);
        }catch(MalformedURLException ex){
            return FileUtil.toURL(new File(systemID));
        }
    }

    public static String toSystemID(URL url){
        try{
            if("file".equals(url.getProtocol()))
                return new File(url.toURI()).getAbsolutePath();
            else
                return url.toString();
        }catch(URISyntaxException ex){
            throw new ImpossibleException(ex);
        }
    }

    public static URI resolve(URL base, URL url){
        return toURI(base).resolve(toURI(url));
    }

    /**
     * returns Query Parameters in specified uri as <code>Map</code>.
     * key will be param name and value wil be param value.
     *
     * @param uri       The string to be parsed into a URI
     * @param encoding  if null, <code>UTF-8</code> will be used
     * 
     * @throws URISyntaxException               in case of invalid uri
     * @throws UnsupportedEncodingException     if named character encoding is not supported
     */
    public static Map<String, String> getQueryParams(String uri, String encoding) throws URISyntaxException, UnsupportedEncodingException{
        if(encoding==null)
            encoding = "UTF-8";
        
        String query = new URI(uri).getRawQuery();
        String params[] = Pattern.compile("&", Pattern.LITERAL).split(query);
        Map<String, String> map = new HashMap<String, String>(params.length);
        for(String param: params){
            int equal = param.indexOf('=');
            String name = param.substring(0, equal);
            String value = param.substring(equal+1);
            name = URLDecoder.decode(name, encoding);
            value = URLDecoder.decode(value, encoding);
            map.put(name, value);
        }
        return map;
    }

    public static String suggestFile(URI uri, String... extensions){
        if(extensions==null || extensions.length==0)
            throw new IllegalArgumentException("atleast one extension must be specified");
        
        String path = uri.getPath();
        String tokens[] = StringUtil.getTokens(path, "/", true);
        String file = tokens[tokens.length-1];
        int dot = file.indexOf(".");
        if(dot==-1){
            String query = uri.getQuery();
            if(query!=null){
                query = query.toLowerCase();
                if(ArrayUtil.contains(extensions, query))
                    return file+'.'+query;
            }
            return file+'.'+extensions[0];
        }else if(ArrayUtil.contains(extensions, file.substring(dot+1)))
            return file;
        else
            return file.substring(0, dot+1)+extensions[0];
    }

    public static void main(String[] args) throws Exception{
        System.out.println(getQueryParams("http://www.google.co.in/search?hl=en&client=firefox-a&rls=org.mozilla%3Aen-US%3Aofficial&hs=Jvw&q=java%26url+encode&btnG=Search&meta=&aq=f&oq=", null));
    }
}
