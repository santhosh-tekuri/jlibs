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

import jlibs.nio.Reactor;
import jlibs.nio.http.msg.spec.Accept;
import jlibs.nio.http.msg.spec.AcceptCharset;
import jlibs.nio.http.msg.spec.values.*;
import jlibs.nio.util.Bytes;
import jlibs.nio.util.Line;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static jlibs.nio.http.msg.Headers.*;
import static jlibs.nio.http.msg.Method.GET;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Request extends Message{
    public Method method = GET;
    public String uri = "/";

    @Override
    protected void parseInitialLine(Line line){
        int begin1 = line.indexOf(false, 0);
        int end1 = line.indexOf(true, begin1);
        int end3 = line.indexOf(false, -(line.length()-1));
        int begin3 = line.indexOf(true, -end3);
        int begin2 = line.indexOf(false, end1);
        int end2 = line.indexOf(false, -begin3);

        method = Method.valueOf(line, begin1, end1);
        uri = line.substring(begin2, end2+1);
        version = Version.valueOf(line, begin3+1, end3+1);
    }

    @Override
    public String toString(){
        StringBuilder buffer = new StringBuilder();
        buffer.append(method).append(' ').append(uri).append(' ').append(version).append("\r\n");
        buffer.append(headers);
        return buffer.toString();
    }

    @Override
    public Bytes encodeTo(Bytes bytes){
        ByteBuffer buffer = Reactor.current().bufferPool.borrow(Bytes.CHUNK_SIZE);
        buffer = bytes.append(method.name, buffer);
        buffer = bytes.append(" ", buffer);
        buffer = bytes.append(uri, buffer);
        buffer = bytes.append(" ", buffer);
        buffer = bytes.append(version.text, buffer);
        buffer = bytes.append("\r\n", buffer);
        buffer = headers.encode(bytes, buffer);
        buffer.flip();
        bytes.append(buffer);
        return bytes;
    }

    /*-------------------------------------------------[ Headers ]---------------------------------------------------*/

    public HostPort getHostPort(){
        return HOST.get(this);
    }

    public void setHostPort(HostPort hostPort){
        HOST.set(this, hostPort);
    }

    public Date getIfModifiedSince(){
        return IF_MODIFIED_SINCE.get(this);
    }

    public void setIfModifiedSince(Date date){
        IF_MODIFIED_SINCE.set(this, date);
    }

    public Date getIfUnmodifiedSince(){
        return IF_UNMODIFIED_SINCE.get(this);
    }

    public void setIfUnmodifiedSince(Date date){
        IF_UNMODIFIED_SINCE.set(this, date);
    }

    public Credentials getCredentials(){
        return AUTHORIZATION.get(this);
    }

    public void setCredentials(Credentials credentials){
        AUTHORIZATION.set(this, credentials);
    }

    public Credentials getProxyCredentials(){
        return PROXY_AUTHORIZATION.get(this);
    }

    public void setProxyCredentials(Credentials credentials){
        PROXY_AUTHORIZATION.set(this, credentials);
    }

    public Credentials getCredentials(boolean proxy){
        return proxy ? getProxyCredentials() : getCredentials();
    }

    public void setCredentials(boolean proxy, Credentials credentials){
        if(proxy)
            setProxyCredentials(credentials);
        else
            setCredentials(credentials);
    }

    public List<QualityItem<MediaType>> getAcceptableMediaTypes(){
        return ACCEPT.get(this);
    }

    public void setAcceptableMediaTypes(Collection<QualityItem<MediaType>> acceptable){
        ACCEPT.set(this, acceptable);
    }

    public MediaType getAcceptableMediaType(Iterable<MediaType> mediaTypes){
        List<QualityItem<MediaType>> acceptable = getAcceptableMediaTypes();
        for(MediaType mediaType: mediaTypes){
            if(Accept.getQuality(mediaType, acceptable)>0)
                return mediaType;
        }
        return null;
    }

    public List<QualityItem<String>> getAcceptableCharsets(){
        return ACCEPT_CHARSET.get(this);
    }

    public void setAcceptableCharsets(Collection<QualityItem<String>> acceptable){
        ACCEPT_CHARSET.set(this, acceptable);
    }

    public String getAcceptableCharset(Iterable<String> charsets){
        List<QualityItem<String>> acceptable = getAcceptableCharsets();
        for(String charset: charsets){
            if(AcceptCharset.getQuality(charset, acceptable)>0)
                return charset;
        }
        return null;
    }

    public Map<String, Cookie> getCookies(){
        return COOKIE.get(this);
    }

    public void setCookies(Collection<Cookie> cookies){
        COOKIE.set(this, cookies);
    }

    public String getReferer(){
        return REFERER.get(this);
    }

    public void setReferer(String referer){
        REFERER.set(this, referer);
    }

    public Expect getExpectation(){
        return EXPECT.get(this);
    }

    public void setExpection(Expect expect){
        EXPECT.set(this, expect);
    }

    public List<String> getXForwardedFor(){
        return X_FORWARDED_FOR.get(this);
    }

    public void setXForwardedFor(Collection<String> clients){
        X_FORWARDED_FOR.set(this, clients);
    }
}
