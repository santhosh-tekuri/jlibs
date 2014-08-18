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
import jlibs.nio.http.msg.spec.values.Challenge;
import jlibs.nio.http.msg.spec.values.ContentDisposition;
import jlibs.nio.http.msg.spec.values.NewCookie;
import jlibs.nio.util.Bytes;
import jlibs.nio.util.Line;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static jlibs.nio.http.msg.Headers.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Response extends Message{
    @Override
    protected void parseInitialLine(Line line){
        int part0Begin = line.indexOf(false, 0);
        int part0End = line.indexOf(true, part0Begin);
        int part1Begin = line.indexOf(false, part0End);
        int part1End = line.indexOf(true, part1Begin);
        int part2Begin = line.indexOf(false, part1End);
        int part2End = line.indexOf(false, -(line.length()-1));

        version = Version.valueOf(line, part0Begin, part0End);
        statusCode = line.parseInt(part1Begin, part1End);
        reasonPhrase = Status.message(statusCode);
        if(!line.equalsIgnoreCase(part2Begin, part2End+1, reasonPhrase))
            reasonPhrase = line.substring(part2Begin, part2End+1);
    }

    @Override
    public String toString(){
        StringBuilder buffer = new StringBuilder();
        buffer.append(version).append(' ').append(statusCode).append(' ').append(reasonPhrase).append("\r\n");
        buffer.append(headers);
        return buffer.toString();
    }

    @Override
    public Bytes encodeTo(Bytes bytes){
        ByteBuffer buffer = Reactor.current().bufferPool.borrow(Bytes.CHUNK_SIZE);
        buffer = bytes.append(version.text, buffer);
        buffer = bytes.append(" ", buffer);
        buffer = bytes.append(String.valueOf(statusCode), buffer);
        buffer = bytes.append(" ", buffer);
        buffer = bytes.append(reasonPhrase, buffer);
        buffer = bytes.append("\r\n", buffer);
        buffer = headers.encode(bytes, buffer);
        buffer.flip();
        bytes.append(buffer);
        return bytes;
    }

    /*-------------------------------------------------[ Status ]---------------------------------------------------*/

    public int statusCode = 200;
    public String reasonPhrase = "OK";

    public void setStatus(int status){
        statusCode = status;
        reasonPhrase = Status.message(status);
    }

    public void setStatus(int status, String message){
        statusCode = status;
        reasonPhrase = Status.message(status, message);
    }

    public void setStatus(int status, Throwable thr){
        statusCode = status;
        reasonPhrase = thr.getMessage()==null ? thr.getClass().getSimpleName() : thr.getMessage();
    }

    public boolean isInformational(){
        return Status.isInformational(statusCode);
    }

    public boolean isSuccessful(){
        return Status.isSuccessful(statusCode);
    }

    public boolean isRedirection(){
        return Status.isRedirection(statusCode);
    }

    public boolean isClientError(){
        return Status.isClientError(statusCode);
    }

    public boolean isServerError(){
        return Status.isServerError(statusCode);
    }

    public boolean isError(){
        return Status.isError(statusCode);
    }

    /*-------------------------------------------------[ Headers ]---------------------------------------------------*/

    public Date getDate(){
        return DATE.get(this);
    }

    public void setDate(Date date){
        DATE.set(this, date);
    }

    public long getAge(){
        Header header = headers.get(AGE);
        return header==null? -1 : Long.parseLong(header.value);
    }

    public void setAge(long age){
        if(age<0)
            headers.remove(AGE);
        else
            headers.set(AGE, String.valueOf(age));
    }

    public Date getExpiration(){
        return EXPIRES.get(this);
    }

    public void setExpiration(Date date){
        EXPIRES.set(this, date);
    }

    public Date getLastModified(){
        return LAST_MODIFIED.get(this);
    }

    public void setLastModified(Date date){
        LAST_MODIFIED.set(this, date);
    }

    public List<Method> getAllowed(){
        return ALLOW.get(this);
    }

    public void setAllowed(Collection<Method> methods){
        ALLOW.set(this, methods);
    }

    public Challenge getChallenge(){
        return WWW_AUTHENTICATE.get(this);
    }

    public void setChallenge(Challenge challenge){
        WWW_AUTHENTICATE.set(this, challenge);
    }

    public Challenge getProxyChallenge(){
        return PROXY_AUTHENTICATE.get(this);
    }

    public void setProxyChallenge(Challenge challenge){
        PROXY_AUTHENTICATE.set(this, challenge);
    }

    public Challenge getChallenge(boolean proxy){
        return proxy ? getProxyChallenge() : getChallenge();
    }

    public void setChallenge(boolean proxy, Challenge challenge){
        if(proxy)
            setProxyChallenge(challenge);
        else
            setChallenge(challenge);
    }

    public Map<String, NewCookie> getNewCookies(){
        return SET_COOKIE.get(this);
    }

    public void setNewCookies(Collection<NewCookie> newCookies){
        SET_COOKIE.set(this, newCookies);
    }

    public String getLocation(){
        return LOCATION.get(this);
    }

    public void setLocation(String location){
        LOCATION.set(this, location);
    }

    public String getContentLocation(){
        return CONTENT_LOCATION.get(this);
    }

    public void setContentLocation(String location){
        CONTENT_LOCATION.set(this, location);
    }

    public ContentDisposition getContentDisposition(){
        return CONTENT_DISPOSITION.get(this);
    }

    public void setContentDisposition(ContentDisposition contentDisposition){
        CONTENT_DISPOSITION.set(this, contentDisposition);
    }
}
