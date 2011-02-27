/**
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

package jlibs.core.net;

import jlibs.core.io.FileUtil;
import jlibs.core.io.IOUtil;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.StringUtil;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * @author Santhosh Kumar T
 */
public class URLUtil{
    /**
     * Constructs <code>URI</code> from given string.
     *
     * The <code>URISyntaxException</code> is rethrown as <code>IllegalArgumentException</code>
     */
    public static URI toURI(String str){
        try{
            return new URI(str);
        }catch(URISyntaxException ex){
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Constructs <code>URI</code> from given <code>URL</code>.
     *
     * The <code>URISyntaxException</code> is rethrown as <code>IllegalArgumentException</code>
     */
    public static URI toURI(URL url){
        try{
            return url.toURI();
        }catch(URISyntaxException ex){
            throw new IllegalArgumentException(ex);
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
            encoding = IOUtil.UTF_8.name();

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

    public static String suggestFile(URI uri, String extension){
        String path = uri.getPath();
        String tokens[] = StringUtil.getTokens(path, "/", true);
        String file = tokens[tokens.length-1];
        int dot = file.indexOf(".");
        if(dot==-1)
            return file+'.'+extension;
        else
            return file.substring(0, dot+1)+extension;
    }

    public static String suggestPrefix(Properties suggested, String uri){
        String prefix = suggested.getProperty(uri);
        if(prefix!=null)
            return prefix;
        else{
            try{
                URI _uri = new URI(uri);

                // suggest prefix from path
                String path = _uri.getPath();
                if(path!=null){
                    StringTokenizer stok = new StringTokenizer(path, "/");
                    while(stok.hasMoreTokens())
                        prefix = stok.nextToken();
                }
                if(prefix!=null)
                    return prefix;
                else{
                    // suggest prefix from host
                    String host = _uri.getHost();
                    if(host!=null){
                        StringTokenizer stok = new StringTokenizer(host, ".");
                        String curPrefix = null;
                        while(stok.hasMoreTokens()){
                            prefix = curPrefix;
                            curPrefix = stok.nextToken();
                        }
                    }

                    if(prefix!=null)
                        return prefix;
                }
            }catch(URISyntaxException ignore){
                // xml spec doesn't guarantee that namespace uri should be valid uri
            }
        }
        return "ns";
    }

    private static SSLContext sc;

    /**
     * Creates connection to the specified url. If the protocol is <code>https</code> the connection
     * created doesn't validate any certificates.
     *
     * @param url   url to which connection has to be created
     * @param proxy proxy to be used. can be null
     * @return <code>URLConnection</code>. the connection is not yet connected
     *
     * @throws IOException if an I/O exception occurs
     */
    public static URLConnection createUnCertifiedConnection(URL url, Proxy proxy) throws IOException{
        if(sc==null){
            try{
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, SSLUtil.DUMMY_TRUST_MANAGERS, new SecureRandom());
                URLUtil.sc = sc;
            }catch(Exception ex){
                throw new ImpossibleException(ex);
            }
        }

        URLConnection con = proxy==null ? url.openConnection() : url.openConnection(proxy);
        if("https".equals(url.getProtocol())){
            HttpsURLConnection httpsCon = (HttpsURLConnection)con;
            httpsCon.setSSLSocketFactory(sc.getSocketFactory());
            httpsCon.setHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String urlHostName, SSLSession session){
                    return true;
                }
            });
        }
        return con;
    }
    
    public static void main(String[] args) throws Exception{
        System.out.println(getQueryParams("http://www.google.co.in/search?hl=en&client=firefox-a&rls=org.mozilla%3Aen-US%3Aofficial&hs=Jvw&q=java%26url+encode&btnG=Search&meta=&aq=f&oq=", null));
        System.out.println(suggestPrefix(new Properties(), "urn:xxx:yyy"));
    }
}
