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

package jlibs.wadl.runtime;

import jlibs.core.io.IOUtil;
import jlibs.core.util.RandomUtil;
import jlibs.wadl.Authenticator;
import jlibs.wadl.model.*;
import org.apache.xerces.xs.XSModel;

import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class Path{
    public final Path parent;
    public final List<Path> children = new ArrayList<Path>();

    public final String name;
    public String value;
    public Resource resource;
    public XSModel schema;
    public Authenticator authenticator;

    public Path(Path parent, String name){
        this.parent = parent;
        if(parent!=null)
            parent.children.add(this);
        this.name = name;
    }

    public String variable(){
        if(name.startsWith("{") && name.endsWith("}"))
            return name.substring(1, name.length()-1);
        else
            return null;
    }
    
    public String resolve(){
        return value!=null ? value : name;
    }
    
    public String toString(Path from){
        Deque<Path> queue = new ArrayDeque<Path>();
        Path path = this;
        while(path!=from){
            queue.push(path);
            path = path.parent;
        }
        StringBuilder buff = new StringBuilder();
        while(!queue.isEmpty()){
            path = queue.pop();
            if(path.name!=null){
                if(buff.length()>0)
                    buff.append('/');
                buff.append(path.resolve());
            }
        }
        return buff.length()==0 ? "/" : buff.toString();
    }

    @Override
    public String toString(){
        return toString(null);
    }

    public Path add(String pathString){
        Path path = this;
        StringTokenizer stok = new StringTokenizer(pathString, "/");
        while(stok.hasMoreTokens()){
            String token = stok.nextToken();
            Path childPath = null;
            if(token.startsWith("{") && token.endsWith("}")){
                for(Path child: path.children){
                    if(child.variable()!=null){
                        childPath = child;
                        break;
                    }
                }
                if(childPath==null)
                    childPath = new Path(path, token);
            }else{
                for(Path child: path.children){
                    if(child.variable()==null && child.name.equals(token)){
                        childPath = child;
                        break;
                    }
                }
                if(childPath==null)
                    childPath = new Path(path, token);
            }
            path = childPath;
        }
        return path;
    }
    
    public Path get(String pathString){
        Path path = this;
        StringTokenizer stok = new StringTokenizer(pathString, "/");
        while(stok.hasMoreTokens()){
            String token = stok.nextToken();
            Path p = null;
            if(token.equals("."))
                p = path;
            else if(token.equals(".."))
                p = path.parent;
            else{
                Path varChild = null;
                for(Path child: path.children){
                    String variable = child.variable();
                    if(variable!=null)
                        varChild = child;
                    else if(child.name.equals(token)){
                        p = child;
                        break;
                    }
                }
                if(p==null && varChild!=null){
                    p = varChild;
                    p.value = token;
                }
            }
            if(p==null)
                return null;
            path = p;
        }
        return path;
    }

    public Path getRoot(){
        Path path = this;
        while(path.parent!=null)
            path = path.parent;
        return path;
    }
    
    public Deque<Path> getStack(){
        Deque<Path> stack = new ArrayDeque<Path>();
        Path path = this;
        while(path!=null){
            stack.push(path);
            path = path.parent;
        }
        return stack;
    }
    
    public XSModel getSchema(){
        Path path = this;
        while(path!=null){
            if(path.schema!=null)
                return path.schema;
            path = path.parent;
        }
        return null;
    }
    
    public Authenticator getAuthenticator(){
        Path path = this;
        while(path!=null){
            if(path.authenticator!=null)
                return path.authenticator;
            path = path.parent;
        }
        return null;
    }

    public HttpURLConnection execute(Method method, Map<String, List<String>> vars, File payload) throws Exception{
        String url = toString();

        StringBuilder queryString = new StringBuilder();
        populate(ParamStyle.QUERY, null, queryString, resource.getParam(), vars);
        Request request = method.getRequest();
        if(request!=null)
            populate(ParamStyle.QUERY, null, queryString, request.getParam(), vars);
        if(queryString.length()>0)
            url += "?"+queryString;

        HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
        populate(ParamStyle.HEADER, con, null, resource.getParam(), vars);

        if(request!=null){
            populate(ParamStyle.HEADER, con, null, request.getParam(), vars);
            if(!request.getRepresentation().isEmpty()){
                Representation rep = request.getRepresentation().get(RandomUtil.random(0, request.getRepresentation().size() - 1));
                if(rep.getMediaType()!=null)
                    con.addRequestProperty("Content-Type", rep.getMediaType());
            }
        }
        con.addRequestProperty("Connection", "close");
        con.setRequestMethod(method.getName());

        Authenticator authenticator = getAuthenticator();
        if(authenticator!=null)
            authenticator.authenticate(con);

        if(payload!=null)
            con.setDoOutput(true);
        con.connect();
        if(payload!=null)
            IOUtil.pump(new FileInputStream(payload), con.getOutputStream(), true, false);
        return con;
    }

    private static void populate(ParamStyle style, HttpURLConnection con, StringBuilder queryString, List<Param> params, Map<String, List<String>> vars){
        for(Param param: params){
            if(param.getStyle()==style){
                List<String> values;
                if(param.getFixed()!=null)
                    values = Collections.singletonList(param.getFixed());
                else{
                    values = vars.get(param.getName());
                    if(values==null && param.isRequired())
                        throw new RuntimeException("unresolved queryParam: "+param.getName());
                }
                if(values!=null){
                    for(String value: values){
                        switch(style){
                            case QUERY:
                                if(queryString.length()>0)
                                    queryString.append('&');
                                queryString.append(param.getName());
                                queryString.append('=');
                                queryString.append(value);
                                break;
                            case HEADER:
                                con.addRequestProperty(param.getName(), param.getFixed());
                                break;
                            default:
                                throw new UnsupportedOperationException();
                        }
                    }
                }
            }
        }
    }
}
