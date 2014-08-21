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

import jlibs.nio.async.ExecutionContext;
import jlibs.nio.channels.ListenerUtil;
import jlibs.nio.util.Bytes;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class EncodablePayload<T> extends Payload{
    public final T source;
    public final Encoder<T> encoder;

    public EncodablePayload(String contentType, T source, Encoder<T> encoder){
        super(contentType);
        this.source = source;
        this.encoder = encoder;
    }

    @Override
    public void transferTo(OutputStream out, ExecutionContext context){
        try{
            encoder.encodeTo(source, out);
            ListenerUtil.resume(context, null, false);
        }catch(Throwable thr){
            ListenerUtil.resume(context, thr, false);
        }
    }

    public RawPayload toRawPayload() throws IOException{
        Bytes bytes = new Bytes();
        try(OutputStream out=bytes.new OutputStream()){
            encoder.encodeTo(source, out);
        }
        return new RawPayload(contentType, bytes);
    }

    public static EncodablePayload<Encodable> newInstance(String contentType, Encodable encodable){
        return new EncodablePayload<>(contentType, encodable, Encodable.ENCODER);
    }
}
