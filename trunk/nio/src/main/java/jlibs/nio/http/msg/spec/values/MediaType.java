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

import jlibs.nio.http.msg.spec.Parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
public class MediaType{
    public static final String CHARSET = "charset";
    public static final MediaType WILDCARD = new MediaType("*", "*");

    public static final MediaType TEXT_PLAIN = new MediaType("text", "plain");
    public static final MediaType TEXT_XML = new MediaType("text", "xml");
    public static final MediaType TEXT_HTML = new MediaType("text", "html");

    public static final MediaType APPLICATION_OCTET_STREAM = new MediaType("application", "octet-stream");
    public static final MediaType APPLICATION_XML = new MediaType("application", "xml");
    public static final MediaType APPLICATION_ATOM_XML = new MediaType("application", "atom+xml");
    public static final MediaType APPLICATION_XHTML_XML = new MediaType("application", "xhtml+xml");
    public static final MediaType APPLICATION_SVG_XML = new MediaType("application", "svg+xml");
    public static final MediaType APPLICATION_JSON = new MediaType("application", "json");
    public static final MediaType APPLICATION_FORM_URLENCODED = new MediaType("application", "x-www-form-urlencoded");
    public static final MediaType MULTIPART_FORM_DATA = new MediaType("multipart", "form-data");

    public static final MediaType SOAP_1_1 = TEXT_XML;
    public static final MediaType SOAP_1_2 = new MediaType("application", "soap+xml");

    public final String type;
    public final String subType;
    public final Map<String, String> params;

    public MediaType(String type, String subType){
        this.type = type;
        this.subType = subType;
        validate();
        params = Collections.emptyMap();
    }

    public MediaType(String type, String subType, String charset){
        this.type = type;
        this.subType = subType;
        validate();
        params = Collections.singletonMap(CHARSET, charset);
    }

    public MediaType(String type, String subType, Map<String, String> params){
        this.type = type;
        this.subType = subType;
        validate();
        this.params = params==null ? Collections.emptyMap() : Collections.unmodifiableMap(params);
    }

    public MediaType(String value){
        this(new Parser(true, value));
    }

    public MediaType(Parser parser){
        String name = parser.lvalue();
        int slash = name.indexOf('/');
        type = name.substring(0, slash);
        subType = name.substring(slash+1);
        validate();
        parser.rvalue();
        Map<String, String> params = null;
        while(true){
            String paramName = parser.lvalue();
            if(paramName==null)
                break;
            if(params==null)
                params = new HashMap<>();
            params.put(paramName, parser.rvalue());
        }
        if(params==null)
            this.params = Collections.emptyMap();
        else
            this.params = Collections.unmodifiableMap(params);
    }

    private void validate(){
        if(type==null)
            throw new NullPointerException("type==null");
        if(subType==null)
            throw new NullPointerException("subType==null");
        if("*".equals(type) && !"*".equals(subType))
            throw new IllegalArgumentException("type==* && subType!=*");
    }

    public String getCharset(String defaultCharset){
        return params.getOrDefault(CHARSET, defaultCharset);
    }

    public MediaType withCharset(String charset){
        Map<String, String> params = new HashMap<>();
        params.putAll(this.params);
        params.put(CHARSET, charset);
        return new MediaType(type, subType, params);
    }

    private boolean isCompatible(String str1, String str2){
        return str1.equals(str2) || "*".equals(str1) || "*".equals(str2);
    }

    public boolean isCompatible(MediaType that){
        if(that!=null && isCompatible(this.type, that.type)){
            if(isCompatible(this.subType, that.subType))
                return true;
            int plus1 = this.subType.indexOf('+');
            int plus2 = that.subType.indexOf('+');
            if(plus1==-1 && plus2!=-1)
                return isCompatible(this.subType, that.subType.substring(plus2+1));
            else if(plus1!=-1 && plus2==-1)
                return isCompatible(this.subType.substring(plus1+1), that.subType);
        }
        return false;
    }

    public boolean isAbstract(){
        return "*".equals(type) || "*".equals(subType);
    }

    public boolean isAny(){
        return "*".equals(type) && "*".equals(subType);
    }

    public boolean isText(){
        return "text".equals(type);
    }

    public boolean isXML(){
        return "xml".equals(subType) || subType.endsWith("+xml");
    }

    @Override
    public boolean equals(Object obj){
        if(obj==this)
            return true;
        if(obj instanceof MediaType){
            MediaType that = (MediaType)obj;
            return this.type.equalsIgnoreCase(that.type)
                    && this.subType.equalsIgnoreCase(that.type)
                    && this.params.equals(that.params);
        }else
            return false;
    }

    private String toString;
    @Override
    public String toString(){
        if(toString==null){
            StringBuilder builder = new StringBuilder();
            builder.append(type).append('/').append(subType);
            for(Map.Entry<String, String> entry: params.entrySet())
                builder.append(';').append(entry.getKey()).append('=').append(entry.getValue());
            toString = builder.toString();
        }
        return toString;
    }
}
