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

package jlibs.nio.http.async;

import jlibs.core.lang.NotImplementedException;
import jlibs.nio.Reactor;
import jlibs.nio.async.ExecutionContext;
import jlibs.nio.async.ReadFromInputStream;
import jlibs.nio.async.WriteBytes;
import jlibs.nio.channels.OutputChannel;
import jlibs.nio.http.msg.Encodable;
import jlibs.nio.http.msg.Encoder;
import jlibs.nio.http.msg.Multipart;
import jlibs.nio.http.msg.MultipartPayload;
import jlibs.nio.util.Bytes;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * @author Santhosh Kumar Tekuri
 */
public class WriteMultipart{
    private Multipart multipart;
    private String boundary;
    private boolean retain;
    private Iterator<Multipart.Part> parts;

    public WriteMultipart(MultipartPayload payload){
        this(payload.multipart, payload.getMediaType().getBoundary(), payload.retain);
    }

    public WriteMultipart(Multipart multipart, String boundary, boolean retain){
        this.multipart = multipart;
        this.retain = retain;
        this.parts = multipart.parts.iterator();
        this.boundary = boundary;
    }

    private OutputChannel out;
    private ExecutionContext context;

    @SuppressWarnings("unchecked")
    public void start(OutputChannel out, ExecutionContext context){
        this.out = out;
        this.context = context;

        try{
            writeSource(this::writePart);
        }catch(Throwable thr){
            context.resume(thr, false);
        }
    }

    private Multipart.Part part;
    private void writePart(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            context.resume(thr, timeout);
            return;
        }

        if(parts.hasNext()){
            part = parts.next();
            state = STATE_PART;
            Bytes bytes = new Bytes();
            ByteBuffer buffer = Reactor.current().bufferPool.borrow(Bytes.CHUNK_SIZE);
            buffer = bytes.append("\r\n--", buffer);
            buffer = bytes.append(boundary, buffer);
            buffer = bytes.append("\r\n", buffer);
            buffer = part.headers.encode(bytes, buffer);
            buffer.flip();
            bytes.append(buffer);
            new WriteBytes(bytes, true).start(out, this::writePartPayload);
        }else{
            state = STATE_EPILOGUE;
            try{
                writeSource(this::finish);
            }catch(Throwable thr1){
                context.resume(thr1, false);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void writePartPayload(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            context.resume(thr, timeout);
            return;
        }

        try{
            writeSource(this::writePart);
        }catch(Throwable thr1){
            context.resume(thr1, false);
        }
    }

    private void finish(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            context.resume(thr, timeout);
            return;
        }

        Bytes bytes = new Bytes();
        ByteBuffer buffer = Reactor.current().bufferPool.borrow(Bytes.CHUNK_SIZE);
        buffer = bytes.append("\r\n--", buffer);
        buffer = bytes.append(boundary, buffer);
        buffer = bytes.append("--\r\n", buffer);
        buffer.flip();
        bytes.append(buffer);
        new WriteBytes(bytes, true).start(out, context);
    }

    private int state = STATE_PREAMBLE;
    private static final int STATE_PREAMBLE = 0;
    private static final int STATE_PART = 1;
    private static final int STATE_EPILOGUE = 2;

    private Object getSource(){
        if(state==STATE_PREAMBLE)
            return multipart.preamble;
        else if(state==STATE_PART)
            return part.payload;
        else
            return multipart.epilogue;
    }

    private Encoder getEncoder(){
        if(state==STATE_PREAMBLE)
            return multipart.preambleEncoder;
        else if(state==STATE_PART)
            return part.payloadEncoder;
        else
            return multipart.epilogueEncoder;
    }

    private void setSource(Bytes bytes){
        if(state==STATE_PREAMBLE)
            multipart.preamble = bytes;
        else if(state==STATE_PART)
            part.payload = bytes;
        else
            multipart.epilogue = bytes;
    }

    @SuppressWarnings("unchecked")
    private void writeSource(ExecutionContext context) throws Exception{
        Object source = getSource();
        Encoder encoder = getEncoder();

        if(source==null)
            context.resume(null, false);
        else if(encoder==null){
            if(source instanceof Bytes)
                new WriteBytes((Bytes)source, !retain).start(out, context);
            else if(source instanceof Encodable){
                Bytes bytes = new Bytes();
                try(Bytes.OutputStream bout=bytes.new OutputStream()){
                    ((Encodable)source).encodeTo(bout);
                }
                new WriteBytes(bytes, true).start(out, context);
            }else if(source instanceof InputStream){
                InputStream is = (InputStream)source;
                ReadFromInputStream ris;
                if(retain){
                    Bytes bytes = new Bytes();
                    setSource(bytes);
                    ris = new ReadFromInputStream(is, bytes);
                }else
                    ris = new ReadFromInputStream(is, Bytes.CHUNK_SIZE);
                ris.start(out, context);
            }else
                throw new NotImplementedException(source.getClass().getName());
        }else{
            Bytes bytes = new Bytes();
            try(Bytes.OutputStream bout=bytes.new OutputStream()){
                encoder.encodeTo(source, bout);
            }
            new WriteBytes(bytes, true).start(out, context);
        }
    }
}
