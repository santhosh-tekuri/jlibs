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

package jlibs.nio.http;

import jlibs.nio.Input;
import jlibs.nio.filters.TrackingInput;
import jlibs.nio.http.msg.Payload;
import jlibs.nio.http.util.Encoding;
import jlibs.nio.util.Buffers;

import java.util.List;

/**
 * @author Santhosh Kumar Tekuri
 */
public class SocketPayload extends Payload{
    private long contentLength;
    protected List<Encoding> encodings;
    protected Input in;

    public SocketPayload(long contentLength, String contentType, Input in, List<Encoding> encodings){
        super(contentType);
        this.contentLength = contentLength;
        this.in = in;
        this.encodings = encodings;
    }

    @Override
    public long getContentLength(){
        return contentLength;
    }

    public List<Encoding> getEncodings(){
        return encodings;
    }

    public boolean retain;
    public Buffers buffers;
    public Input socket(){
        if(encodings.isEmpty())
            return in;

        contentLength = -1;
        TrackingInput trackingInput = null;
        if(in instanceof TrackingInput){
            trackingInput = (TrackingInput)in;
            in = trackingInput.detachInput();
        }
        try{
            while(!encodings.isEmpty()){
                Encoding encoding = encodings.remove(encodings.size()-1);
                in = encoding.wrap(in);
            }
        }finally{
            if(trackingInput!=null){
                trackingInput.reattach();
                in = trackingInput;
            }
        }
        return in;
    }
}
