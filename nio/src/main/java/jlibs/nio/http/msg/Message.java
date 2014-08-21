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

import jlibs.nio.http.HTTPException;
import jlibs.nio.http.msg.spec.HTTPEncoding;
import jlibs.nio.http.msg.spec.values.Encoding;
import jlibs.nio.http.msg.spec.values.MediaType;
import jlibs.nio.util.Bytes;
import jlibs.nio.util.Line;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static jlibs.nio.http.msg.Headers.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class Message implements Line.Consumer, Encodable, Bytes.Encodable{
    public Version version = Version.HTTP_1_1;
    public final Headers headers = new Headers();

    private boolean linePopulated;

    @Override
    public final void consume(Line line){
        if(!linePopulated){
            linePopulated = true;
            try{
                parseInitialLine(line);
            }catch(Exception ex){
                throw HTTPException.badMessage(this, "Bad Initital Line", ex);
            }
        }else{
            try{
                headers.consume(line);
            }catch(Exception ex){
                throw HTTPException.badMessage(this, headers.populated ? "Bad Trailers" : "Bad Headers", ex);
            }
        }
    }

    protected abstract void parseInitialLine(Line line);

    @Override
    public abstract Bytes encodeTo(Bytes bytes);

    /*-------------------------------------------------[ Payload ]---------------------------------------------------*/

    private Payload payload = Payload.NO_PAYLOAD;

    public Payload getPayload(){
        return payload;
    }

    public void setPayload(Payload payload, boolean closeExistingPayload) throws IOException{
        Payload oldPayload = this.payload;
        this.payload = payload;
        if(closeExistingPayload && oldPayload instanceof Closeable)
            ((Closeable)oldPayload).close();
    }

    /*-------------------------------------------------[ Headers ]---------------------------------------------------*/

    public boolean isKeepAlive(){
        return CONNECTION.get(this);
    }

    public void setKeepAlive(boolean keepAlive){
        CONNECTION.set(this, keepAlive);
    }

    public long getContentLength(){
        Header header = headers.get(CONTENT_LENGTH);
        return header==null? -1 : Long.parseLong(header.value);
    }

    public void setContentLength(long length){
        if(length<0)
            headers.remove(CONTENT_LENGTH);
        else{
            headers.set(CONTENT_LENGTH, String.valueOf(length));
            headers.remove(TRANSFER_ENCODING);
        }
    }

    public List<Encoding> getContentEncodings(){
        return CONTENT_ENCODING.get(this);
    }

    public void setContentEncodings(Collection<Encoding> encodings){
        CONTENT_ENCODING.set(this, encodings);
    }

    public List<Encoding> getTransferEncodings(){
        return TRANSFER_ENCODING.get(this);
    }

    public void setTransferEncodings(Collection<Encoding> encodings){
        TRANSFER_ENCODING.set(this, encodings);
    }

    public void setChunked(){
        headers.set(TRANSFER_ENCODING, HTTPEncoding.CHUNKED.name);
        headers.remove(CONTENT_LENGTH);
    }

    public List<Encoding> getEncodings(){
        List<Encoding> contentEncodings = getContentEncodings();
        List<Encoding> transferEncodings = getTransferEncodings();
        if(contentEncodings.isEmpty())
            return transferEncodings;
        else{
            contentEncodings.addAll(transferEncodings);
            return contentEncodings;
        }
    }

    public MediaType getMediaType(){
        return CONTENT_TYPE.get(this);
    }

    public void setMediaType(MediaType mediaType){
        CONTENT_TYPE.set(this, mediaType);
    }

    public List<String> getTrailers(){
        return TRAILER.get(this);
    }

    public void setTrailers(Collection<String> trailers){
        TRAILER.set(this, trailers);
    }
}
