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
import jlibs.nio.http.SocketPayload;
import jlibs.nio.http.util.ContentDisposition;
import jlibs.nio.http.util.ContentEncoding;
import jlibs.nio.http.util.MediaType;
import jlibs.nio.http.util.Parser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class Message{
    public Version version = Version.HTTP_1_1;
    public final Headers headers = new Headers();
    public Headers trailers;

    public abstract void putLineInto(ByteBuffer buffer);

    public abstract Status badMessageStatus();
    public RuntimeException badMessage(String message){
        return badMessageStatus().with(message);
    }
    public RuntimeException badMessage(Throwable thr){
        return badMessageStatus().with(thr);
    }
    public abstract Status timeoutStatus();

    /*-------------------------------------------------[ Payload ]---------------------------------------------------*/

    private Payload payload = EmptyPayload.INSTANCE;

    public Payload getPayload(){
        return payload;
    }

    public void setPayload(Payload payload) throws IOException{
        if(this.payload instanceof SocketPayload)
            ((SocketPayload)this.payload).socket().close();
        this.payload = payload;
    }

    /*-------------------------------------------------[ Connection ]---------------------------------------------------*/

    public static final AsciiString CONNECTION = new AsciiString("Connection");
    public static final AsciiString PROXY_CONNECTION = new AsciiString("Proxy-Connection");
    public static final String CLOSE = "close";
    public static final String KEEP_ALIVE = "keep-alive";

    public boolean isKeepAlive(){
        String value = headers.value(CONNECTION);
        if(version.keepAliveDefault)
            return !(value!=null && CLOSE.equalsIgnoreCase(value));
        else
            return value!=null && KEEP_ALIVE.equalsIgnoreCase(value);
    }

    public void setKeepAlive(boolean keepAlive){
        String value;
        if(version.keepAliveDefault)
            value = keepAlive ? null : CLOSE;
        else
            value = keepAlive ? KEEP_ALIVE : null;
        if(value==null)
            headers.remove(CONNECTION);
        else
            headers.set(CONNECTION, value);
    }

    /*-------------------------------------------------[ Content-Length ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.13
    public static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");

    public long getContentLength(){
        String value = headers.value(CONTENT_LENGTH);
        return value==null? -1 : Util.parseLong(value);
    }

    public void setContentLength(long length){
        if(length<0)
            headers.remove(CONTENT_LENGTH);
        else{
            headers.remove(TRANSFER_ENCODING);
            headers.set(CONTENT_LENGTH, length==0 ? "0" : Long.toString(length));
        }
    }

    /*-------------------------------------------------[ Transfer-Encoding ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.41
    public static final AsciiString TRANSFER_ENCODING = new AsciiString("Transfer-Encoding");
    public static final String IDENTITY = "identity";
    public static final String CHUNKED = "chunked";

    public boolean isChunked(){
        String value = headers.value(TRANSFER_ENCODING);
        return value!=null && !IDENTITY.equalsIgnoreCase(value);
    }

    public void setChunked(){
        headers.remove(CONTENT_LENGTH);
        headers.set(TRANSFER_ENCODING, CHUNKED);
    }

    /*-------------------------------------------------[ Content-Encoding ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.11
    public static final AsciiString CONTENT_ENCODING = new AsciiString("Content-Encoding");

    private static final Function<Parser, ContentEncoding> encodingDelegate = Parser.lvalueDelegate(ContentEncoding::valueOf);
    public List<ContentEncoding> getContentEncodings(){
        return headers.getListValue(CONTENT_ENCODING, encodingDelegate, true);
    }

    public void setContentEncodings(Collection<ContentEncoding> encodings){
        headers.setListValue(CONTENT_ENCODING, encodings, null, true);
    }

    /*-------------------------------------------------[ Content-Type ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17
    public static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");

    public MediaType getMediaType(){
        return headers.getSingleValue(CONTENT_TYPE, MediaType::new);
    }

    public void setMediaType(MediaType mt){
        headers.setSingleValue(CONTENT_TYPE, mt, null);
    }


    /*-------------------------------------------------[ Content-Disposition ]---------------------------------------------------*/

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.5.1
    public static final AsciiString CONTENT_DISPOSITION = new AsciiString("Content-Disposition");

    public ContentDisposition getContentDisposition(){
        return headers.getSingleValue(CONTENT_DISPOSITION, ContentDisposition::new);
    }

    public void setContentDisposition(ContentDisposition cd){
        headers.setSingleValue(CONTENT_DISPOSITION, cd, null);
    }

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9
    public static final AsciiString CACHE_CONTROL = new AsciiString("Cache-Control");

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.15
    public static final AsciiString CONTENT_MD5 = new AsciiString("Content-MD5");
}
