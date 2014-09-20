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
import jlibs.nio.http.util.*;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;

import static jlibs.nio.http.util.USAscii.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Request extends Message{
    public Method method = Method.GET;
    public String uri = "/";

    @Override
    public void putLineInto(ByteBuffer buffer){
        method.putInto(buffer);
        USAscii.append(buffer, uri);
        buffer.put(SP);
        version.putInto(buffer);
        buffer.put(CR);
        buffer.put(LF);
    }

    @Override
    public Status badMessageStatus(){
        return Status.BAD_REQUEST;
    }

    @Override
    public Status timeoutStatus(){
        return Status.REQUEST_TIMEOUT;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(method.name).append(' ')
                .append(uri).append(' ')
                .append(version)
                .append("\r\n")
                .append(headers);
        return builder.toString();
    }

    /*-------------------------------------------------[ Host ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.23
    public static final AsciiString HOST = new AsciiString("Host");

    public HostPort getHost(){
        return headers.getSingleValue(HOST, HostPort::valueOf);
    }

    public void setHost(HostPort host){
        headers.setSingleValue(HOST, host, null);
    }

    /*-------------------------------------------------[ X-Forwarded-For ]---------------------------------------------------*/

    // http://en.wikipedia.org/wiki/X-Forwarded-For
    public static final AsciiString X_FORWARDED_FOR = new AsciiString("X-Forwarded-For");

    public List<String> getXForwardedFor(){
        return headers.getListValue(X_FORWARDED_FOR, Parser.LVALUE_FUNCTION, true);
    }

    public void setXForwardedFor(Collection<String> clients){
        headers.setListValue(X_FORWARDED_FOR, clients, null, true);
    }

    /*-------------------------------------------------[ Expect ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.20
    public static final AsciiString EXPECT = new AsciiString("Expect");

    public Expect getExpectation(){
        return headers.getSingleValue(EXPECT, Expect::valueOf);
    }

    public void setExpectation(Expect expect){
        headers.setSingleValue(EXPECT, expect, null);
    }

    /*-------------------------------------------------[ Authorization ]---------------------------------------------------*/

    public static final AsciiString AUTHORIZATION = new AsciiString("Authorization");

    public Credentials getCredentials(){
        return Credentials.parse(headers.value(AUTHORIZATION));
    }

    public void setCredentials(Credentials credentials){
        headers.setSingleValue(AUTHORIZATION, credentials, null);
    }

    /*-------------------------------------------------[ Proxy-Authorization ]---------------------------------------------------*/

    public static final AsciiString PROXY_AUTHORIZATION = new AsciiString("Proxy-Authorization");

    public Credentials getProxyCredentials(){
        return Credentials.parse(headers.value(PROXY_AUTHORIZATION));
    }

    public void setProxyCredentials(Credentials credentials){
        headers.setSingleValue(PROXY_AUTHORIZATION, credentials, null);
    }

    /*-------------------------------------------------[ Credentials ]---------------------------------------------------*/

    public Credentials getCredentials(boolean proxy){
        return proxy ? getProxyCredentials() : getCredentials();
    }

    public void setCredentials(Credentials credentials, boolean proxy){
        if(proxy)
            setProxyCredentials(credentials);
        else
            setCredentials(credentials);
    }

    /*-------------------------------------------------[ Accept ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1
    public static final AsciiString ACCEPT = new AsciiString("Accept");

    private static final Function<Parser, QualityItem<MediaType>> ACCEPT_PARSER = parser -> {
        String name = parser.lvalue();
        int slash = name.indexOf('/');
        String type = name.substring(0, slash);
        String subType = name.substring(slash+1);
        parser.rvalue();

        double quality = 1;
        Map<String, String> params = null;
        while(true){
            String paramName = parser.lvalue();
            if(paramName==null)
                break;
            if(QualityItem.QUALITY.equals(paramName))
                quality = Double.parseDouble(parser.rvalue());
            else{
                if(params==null)
                    params = new HashMap<>();
                params.put(paramName, parser.rvalue());
            }
        }
        return new QualityItem<>(new MediaType(type, subType, params), quality);
    };

    private static final Comparator<QualityItem<MediaType>> ACCEPT_COMPARATOR = new Comparator<QualityItem<MediaType>>(){
        private int score(MediaType mt){
            if("*".equals(mt.type))
                return 0;
            if("*".equals(mt.subType))
                return 1;
            else
                return 2;
        }

        @Override
        public int compare(QualityItem<MediaType> o1, QualityItem<MediaType> o2){
            int score1 = score(o1.item);
            int score2 = score(o2.item);
            if(score1==score2){
                if(o1.item.type.equals(o2.item.type) && o1.item.subType.equals(o2.item.subType)){
                    int paramScore1 = o1.item.params.size();
                    int paramScore2 = o2.item.params.size();
                    if(paramScore1==paramScore2)
                        return Double.compare(o2.quality, o1.quality);
                    else
                        return Integer.compare(paramScore2, paramScore1);
                }else
                    return -1;
            }else
                return Integer.compare(score2, score1);
        }
    };

    public List<QualityItem<MediaType>> getAcceptableMediaTypes(){
        List<QualityItem<MediaType>> list = headers.getListValue(ACCEPT, ACCEPT_PARSER, true);
        list.sort(ACCEPT_COMPARATOR);
        return list;
    }

    public static double getQuality(MediaType mt, List<QualityItem<MediaType>> accept){
        for(QualityItem<MediaType> qualityItem: accept){
            if(mt.isCompatible(qualityItem.item)){
                boolean paramsMatched = true;
                for(Map.Entry<String, String> param: qualityItem.item.params.entrySet()){
                    String value = mt.params.get(param.getKey());
                    if(!param.getValue().equals(value)){
                        paramsMatched = false;
                        break;
                    }
                }
                if(paramsMatched)
                    return qualityItem.quality;
            }
        }
        return 0;
    }

    public MediaType getAcceptableMediaType(Iterable<MediaType> mediaTypes){
        List<QualityItem<MediaType>> acceptable = getAcceptableMediaTypes();
        for(MediaType mediaType: mediaTypes){
            if(getQuality(mediaType, acceptable)>0)
                return mediaType;
        }
        return null;
    }

    public void setAcceptableMediaTypes(Collection<QualityItem<MediaType>> acceptable){
        headers.setListValue(ACCEPT, acceptable, null, true);
    }

    /*-------------------------------------------------[ Accept-Charset ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.2
    public static final AsciiString ACCEPT_CHARSET = new AsciiString("Accept-Charset");

    private static final Function<Parser, QualityItem<String>> QUALITY_ITEM_PARSER = parser -> {
        String item = parser.lvalue();
        parser.rvalue();
        double quality = 1;
        while(true){
            String paramName = parser.lvalue();
            if(paramName==null)
                break;
            if(QualityItem.QUALITY.equals(paramName))
                quality = Double.parseDouble(parser.rvalue());
        }

        return new QualityItem<>(item, quality);
    };

    public List<QualityItem<String>> getAcceptableCharsets(){
        return headers.getListValue(ACCEPT_CHARSET, QUALITY_ITEM_PARSER, true);
    }

    public static double getCharsetQuality(String charset, List<QualityItem<String>> acceptableCharsets){
        if(acceptableCharsets==null || acceptableCharsets.isEmpty())
            return 1.0;

        double defaultQuality = 0;
        for(QualityItem<String> qualityItem: acceptableCharsets){
            if("*".equals(qualityItem.item))
                defaultQuality = qualityItem.quality;
            else if(qualityItem.item.equalsIgnoreCase(charset))
                return qualityItem.quality;
        }

        return IOUtil.ISO_8859_1.name().equalsIgnoreCase(charset) ? 1 : defaultQuality;
    }

    public String getAcceptableCharset(Iterable<String> charsets){
        List<QualityItem<String>> acceptable = getAcceptableCharsets();
        for(String charset: charsets){
            if(getCharsetQuality(charset, acceptable)>0)
                return charset;
        }
        return null;
    }

    public void setAcceptableCharsets(Collection<QualityItem<String>> acceptable){
        headers.setListValue(ACCEPT_CHARSET, acceptable, null, true);
    }

    /*-------------------------------------------------[ If-Modified-Since ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.25
    public static final AsciiString IF_MODIFIED_SINCE = new AsciiString("If-Modified-Since");

    public Date getIfModifiedSince(){
        return headers.getSingleValue(IF_MODIFIED_SINCE, HTTPDate.getInstance()::parse);
    }

    public void setIfModifiedSince(Date date){
        headers.setSingleValue(IF_MODIFIED_SINCE, date, HTTPDate.getInstance()::format);
    }

    /*-------------------------------------------------[ If-Unmodified-Since ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.28
    public static final AsciiString IF_UNMODIFIED_SINCE = new AsciiString("If-Unmodified-Since");

    public Date getIfUnmodifiedSince(){
        return headers.getSingleValue(IF_UNMODIFIED_SINCE, HTTPDate.getInstance()::parse);
    }

    public void setIfUnmodifiedSince(Date date){
        headers.setSingleValue(IF_UNMODIFIED_SINCE, date, HTTPDate.getInstance()::format);
    }

    /*-------------------------------------------------[ SOAPAction ]---------------------------------------------------*/

    // http://www.w3.org/TR/2000/NOTE-SOAP-20000508/#_Toc478383528
    public static final AsciiString SOAP_ACTION = new AsciiString("SOAPAction");

    public String getSOAPAction(){
        return headers.getSingleValue(SOAP_ACTION, String::valueOf);
    }

    public void setSOAPAction(String soapAction){
        headers.set(SOAP_ACTION, soapAction);
    }

    /*-------------------------------------------------[ Referer ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.36
    public static final AsciiString REFERER = new AsciiString("Referer");

    public String getReferer(){
        return headers.getSingleValue(REFERER, String::valueOf);
    }

    public void setReferer(String referer){
        headers.set(REFERER, referer);
    }

    /*-------------------------------------------------[ Cookie ]---------------------------------------------------*/

    // http://tools.ietf.org/html/rfc6265#section-4.2
    public static final AsciiString COOKIE = new AsciiString("Cookie");

    public Map<String, Cookie> getCookies(){
        return headers.getMapValue(COOKIE, Cookie::new, cookie -> cookie.name, true);
    }

    public void setCookies(Collection<Cookie> cookies){
        headers.setListValue(COOKIE, cookies, null, true);
    }

    /*-------------------------------------------------[ User-Agent ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.43
    public static final AsciiString USER_AGENT = new AsciiString("User-Agent");

    public String getUserAgent(){
        return headers.getSingleValue(USER_AGENT, String::valueOf);
    }

    public void setUserAgent(String userAgent){
        headers.set(USER_AGENT, userAgent);
    }


    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.3
    public static final AsciiString ACCEPT_ENCODING = new AsciiString("Accept-Encoding");

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.4
    public static final AsciiString ACCEPT_LANGUAGE = new AsciiString("Accept-Language");

    /*-------------------------------------------------[ Origin ]---------------------------------------------------*/

    // http://tools.ietf.org/html/rfc6454#section-7
    public static final AsciiString ORIGIN = new AsciiString("Origin");

    public Origins getOrigins(){
        return headers.getSingleValue(ORIGIN, Origins::valueOf);
    }

    public void setOrigins(Origins origins){
        headers.setSingleValue(ORIGIN, origins, null);
    }

    /*-------------------------------------------------[ Access-Control-Request-Method ]---------------------------------------------------*/

    // http://www.w3.org/TR/cors/#access-control-request-method-request-header
    public static final AsciiString ACCESS_CONTROL_REQUEST_METHOD = new AsciiString("Access-Control-Request-Method");

    public Method getAccessControlRequestMethod(){
        return headers.getSingleValue(ACCESS_CONTROL_REQUEST_METHOD, Method::valueOf);
    }

    public void setAccessControlRequestMethod(Method method){
        headers.setSingleValue(ACCESS_CONTROL_REQUEST_METHOD, method, null);
    }
}
