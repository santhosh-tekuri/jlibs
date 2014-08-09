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
import jlibs.core.lang.NotImplementedException;
import jlibs.nio.Client;
import jlibs.nio.async.ExecutionContext;
import jlibs.nio.async.ReadBytes;
import jlibs.nio.async.WriteToOutputStream;
import jlibs.nio.channels.InputChannel;
import jlibs.nio.channels.ListenerUtil;
import jlibs.nio.channels.filters.TrackingInputFilter;
import jlibs.nio.http.msg.spec.values.Encoding;
import jlibs.nio.http.msg.spec.values.MediaType;
import jlibs.nio.util.Bytes;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Payload{
    public static final Payload NO_PAYLOAD = new Payload();

    public final long contentLength;
    public final String contentType;
    public final List<Encoding> encodings;

    public Payload(){
        this(0, null, null, null, null);
    }

    public Payload(long contentLength, String contentType, List<Encoding> encodings, InputChannel inputChannel){
        this(contentLength, contentType, encodings, inputChannel, null);
    }

    public Payload(long contentLength, String contentType, List<Encoding> encodings, InputStream inputStream){
        this(contentLength, contentType, encodings, inputStream, null);
    }

    public Payload(long contentLength, String contentType, List<Encoding> encodings, Object source, Encoder encoder){
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.encodings = encodings;
        this.source = source;
        this.encoder = encoder;
    }

    private MediaType mediaType;
    public MediaType getMediaType(){
        if(contentType==null)
            return null;
        if(mediaType==null)
            mediaType = new MediaType(contentType);
        return mediaType;
    }

    public boolean retain;
    public Bytes bytes;

    private Object source;
    public Object getSource(){
        return source;
    }

    private Encoder encoder;
    @SuppressWarnings("unchecked")
    public Encoder<Object> getEncoder(){
        return (Encoder<Object>)encoder;
    }

    public void close() throws IOException{
        if(source instanceof Closeable)
            ((Closeable)source).close();
        clearSource();
    }

    public void clearSource(){
        source = null;
        encoder = null;
    }

    public void removeEncodings() throws Exception{
        if(encodings==null || encodings.isEmpty())
            return;

        if(source instanceof InputChannel){
            Client client = ((InputChannel)source).getClient();
            TrackingInputFilter trackingFilter = null;
            if(source instanceof TrackingInputFilter){
                trackingFilter = (TrackingInputFilter)source;
                client.inPipeline.pop();
            }

            try{
                while(!encodings.isEmpty())
                    client.inPipeline.push(encodings.remove(encodings.size()-1).createInputFilter());
            }finally{
                if(trackingFilter!=null)
                    client.inPipeline.push(trackingFilter);
            }
        }else if(source instanceof InputStream){
            while(!encodings.isEmpty())
                source = encodings.remove(encodings.size()-1).apply((InputStream)source);
        }else
            throw new NotImplementedException();
    }

    public void readFromSource(long limit, ExecutionContext context){
        try{
            if(bytes==null)
                bytes = new Bytes();

            if(source instanceof InputChannel){
                InputChannel inputChannel = (InputChannel)source;
                inputChannel.setLimit(limit);
                if(inputChannel.isOpen()){
                    new ReadBytes(bytes).start(inputChannel, context);
                    return;
                }
            }else if(source instanceof InputStream){
                try{
                    IOUtil.pump((InputStream)source, bytes.new OutputStream(), true, true);
                }finally{
                    clearSource();
                }
            }
            ListenerUtil.resume(context, null, false);
        }catch(Throwable thr){
            ListenerUtil.resume(context, thr, false);
        }
    }

    @SuppressWarnings("unchecked")
    public void writePayloadTo(OutputStream out, ExecutionContext context){
        try{
            if(bytes!=null)
                IOUtil.pump(bytes.new InputStream(), out, true, false);

            if(source instanceof InputChannel){
                InputChannel inputChannel = (InputChannel)source;
                if(inputChannel.isOpen()){
                    //todo: retain part
                    WriteToOutputStream writeToOutputStream;
                    if(retain){
                        if(bytes==null)
                            bytes = new Bytes();
                        writeToOutputStream = new WriteToOutputStream(out, bytes);
                    }else
                        writeToOutputStream = new WriteToOutputStream(out, Bytes.CHUNK_SIZE);
                    writeToOutputStream.start(inputChannel, context);
                    return;
                }
            }else if(encoder!=null)
                encoder.encodeTo(source, out);
            else if(source instanceof Encodable)
                ((Encodable)source).encodeTo(out);
            else if(source instanceof InputStream){
                InputStream in = (InputStream)source;
                if(retain){
                    if(bytes==null)
                        bytes = new Bytes();
                    bytes.pumpWithBackup(in, out);
                }else
                    IOUtil.pump(in, out, true, false);
                clearSource();
            }

            ListenerUtil.resume(context, null, false);
        }catch(Throwable thr){
            ListenerUtil.resume(context, thr, false);
        }
    }
}
