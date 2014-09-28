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

import jlibs.core.lang.Util;
import jlibs.nio.http.expr.UnresolvedException;
import jlibs.nio.http.util.*;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Response extends Message{
    public Status status = Status.OK;

    @Override
    public void putLineInto(ByteBuffer buffer){
        status.putInto(buffer, version);
    }

    @Override
    public Status badMessageStatus(){
        return Status.BAD_GATEWAY;
    }

    @Override
    public Status timeoutStatus(){
        return Status.GATEWAY_TIMEOUT;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(version).append(' ')
                .append(status)
                .append("\r\n")
                .append(headers);
        return builder.toString();
    }

    /*-------------------------------------------------[ Bean ]---------------------------------------------------*/

    @Override
    @SuppressWarnings("StringEquality")
    public Object getField(String name) throws UnresolvedException{
        if(name=="status")
            return status;
        else
            return super.getField(name);
    }

    /*-------------------------------------------------[ Date ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.18
    public static final AsciiString DATE = new AsciiString("Date");

    public Date getDate(){
        return headers.getSingleValue(DATE, HTTPDate.getInstance()::parse);
    }

    public void setDate(Date date){
        headers.setSingleValue(DATE, date, HTTPDate.getInstance()::format);
    }

    public void setDate(boolean overwrite){
        if(!overwrite && headers.get(DATE)!=null)
            return;
        headers.set(DATE, HTTPDate.getInstance().currentDate());
    }

    /*-------------------------------------------------[ Age ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.6
    public static final AsciiString AGE = new AsciiString("Age");

    public long getAge(){
        String value = headers.value(AGE);
        return value==null? -1 : Util.parseLong(value);
    }

    public void setAge(long age){
        if(age<0)
            headers.remove(AGE);
        else
            headers.set(AGE, Long.toString(age));
    }

    /*-------------------------------------------------[ Expires ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21
    public static final AsciiString EXPIRES = new AsciiString("Expires");

    public Date getExpiration(){
        return headers.getSingleValue(EXPIRES, HTTPDate.getInstance()::parse);
    }

    public void setExpiration(Date date){
        headers.setSingleValue(EXPIRES, date, HTTPDate.getInstance()::format);
    }

    /*-------------------------------------------------[ Last-Modified ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.29
    public static final AsciiString LAST_MODIFIED = new AsciiString("Last-Modified");

    public Date getLastModified(){
        return headers.getSingleValue(EXPIRES, HTTPDate.getInstance()::parse);
    }

    public void setLastModified(Date date){
        headers.setSingleValue(EXPIRES, date, HTTPDate.getInstance()::format);
    }

    /*-------------------------------------------------[ WWW-Authenticate ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.47
    public static final AsciiString WWW_AUTHENTICATE = new AsciiString("WWW-Authenticate");

    public Challenge getChallenge(){
        return headers.getSingleValue(WWW_AUTHENTICATE, Challenge::parse);
    }

    public void setChallenge(Challenge challenge){
        headers.setSingleValue(WWW_AUTHENTICATE, challenge, null);
    }

    /*-------------------------------------------------[ Proxy-Authenticate ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.33
    public static final AsciiString PROXY_AUTHENTICATE = new AsciiString("Proxy-Authenticate");

    public Challenge getProxyChallenge(){
        return headers.getSingleValue(PROXY_AUTHENTICATE, Challenge::parse);
    }

    public void setProxyChallenge(Challenge challenge){
        headers.setSingleValue(PROXY_AUTHENTICATE, challenge, null);
    }

    /*-------------------------------------------------[ Challenge ]---------------------------------------------------*/

    public Challenge getChallenge(boolean proxy){
        return proxy ? getProxyChallenge() : getChallenge();
    }

    public void setChallenge(Challenge challenge, boolean proxy){
        if(proxy)
            setProxyChallenge(challenge);
        else
            setChallenge(challenge);
    }

    /*-------------------------------------------------[ Set-Cookie ]---------------------------------------------------*/

    // http://tools.ietf.org/html/rfc6265#section-4.1
    public static final AsciiString SET_COOKIE = new AsciiString("Set-Cookie");

    public Map<String, NewCookie> getNewCookies(){
        return headers.getMapValue(SET_COOKIE, NewCookie::new, newCookie -> newCookie.cookie.name, false);
    }

    public void setNewCookies(Collection<NewCookie> newCookies){
        headers.setListValue(SET_COOKIE, newCookies, null, false);
    }

    /*-------------------------------------------------[ Server ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.38
    public static final AsciiString SERVER = new AsciiString("Server");

    public String getServer(){
        return headers.value(SERVER);
    }

    public void setServer(String server){
        headers.set(SERVER, server);
    }

    /*-------------------------------------------------[ Location ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.30
    public static final AsciiString LOCATION = new AsciiString("Location");

    public String getLocation(){
        return headers.getSingleValue(LOCATION, String::valueOf);
    }

    public void setLocation(String location){
        headers.setSingleValue(LOCATION, location, null);
    }
    /*-------------------------------------------------[ Content-Location ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.30
    public static final AsciiString CONTENT_LOCATION = new AsciiString("Content-Location");

    public String getContentLocation(){
        return headers.getSingleValue(CONTENT_LOCATION, String::valueOf);
    }

    public void setContentLocation(String location){
        headers.setSingleValue(CONTENT_LOCATION, location, null);
    }

    /*-------------------------------------------------[ Allow ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.7
    public static final AsciiString ALLOW = new AsciiString("Allow");

    public List<Method> getAllowedMethods(){
        return headers.getListValue(ALLOW, Parser.lvalueDelegate(Method::valueOf), true);
    }

    public void setAllowedMethods(Collection<Method> methods){
        headers.setListValue(ALLOW, methods, null, true);
    }

    /*-------------------------------------------------[ Access-Control-Allow-Origin ]---------------------------------------------------*/

    // http://www.w3.org/TR/cors/#http-access-control-allow-origin
    public static final AsciiString ACCESS_CONTROL_ALLOW_ORIGIN = new AsciiString("Access-Control-Allow-Origin");

    public Origins getAccessControlAllowedOrigins(){
        return headers.getSingleValue(ACCESS_CONTROL_ALLOW_ORIGIN, Origins::valueOf);
    }

    public void setAccessControlAllowedOrigins(Origins origins){
        headers.setSingleValue(ACCESS_CONTROL_ALLOW_ORIGIN, origins, null);
    }

    /*-------------------------------------------------[ Access-Control-Allow-Methods ]---------------------------------------------------*/

    // http://www.w3.org/TR/cors/#access-control-allow-methods-response-header
    public static final AsciiString ACCESS_CONTROL_ALLOW_METHODS = new AsciiString("Access-Control-Allow-Methods");

    public List<Method> getAccessControlAllowedMethods(){
        return headers.getListValue(ACCESS_CONTROL_ALLOW_METHODS, Parser.lvalueDelegate(Method::valueOf), true);
    }

    public void setAccessControlAllowedMethods(Collection<Method> methods){
        headers.setListValue(ACCESS_CONTROL_ALLOW_METHODS, methods, null, true);
    }

    /*-------------------------------------------------[ Access-Control-Allow-Credentials ]---------------------------------------------------*/

    // http://www.w3.org/TR/cors/#access-control-allow-credentials-response-header
    public static final AsciiString ACCESS_CONTROL_ALLOW_CREDENTIALS = new AsciiString("Access-Control-Allow-Credentials");

    public boolean isAccessControlAllowCredentials(){
        Boolean value = headers.getSingleValue(ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean::parseBoolean);
        return value==null ? Boolean.FALSE : value;
    }

    public void setAccessControlAllowCredentials(boolean allow){
        headers.set(ACCESS_CONTROL_ALLOW_CREDENTIALS, allow ? "true" : null);
    }

    /*-------------------------------------------------[ Access-Control-Max-Age ]---------------------------------------------------*/

    // http://www.w3.org/TR/cors/#access-control-max-age-response-header
    public static final AsciiString ACCESS_CONTROL_MAX_AGE = new AsciiString("Access-Control-Max-Age");

    public long getAccessControlMaxAge(){
        String value = headers.value(ACCESS_CONTROL_MAX_AGE);
        return value==null? -1 : Util.parseLong(value);
    }

    public void setAccessControlMaxAge(long age){
        if(age<0)
            headers.remove(ACCESS_CONTROL_MAX_AGE);
        else
            headers.set(ACCESS_CONTROL_MAX_AGE, Long.toString(age));
    }

    static{
        AsciiString.initInterned();
    }
}
