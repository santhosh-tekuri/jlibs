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
import jlibs.nio.Client;
import jlibs.nio.async.ExecutionContext;
import jlibs.nio.async.ReadBytes;
import jlibs.nio.async.WriteToOutputStream;
import jlibs.nio.channels.InputChannel;
import jlibs.nio.channels.ListenerUtil;
import jlibs.nio.channels.filters.TrackingInputFilter;
import jlibs.nio.http.msg.spec.values.Encoding;
import jlibs.nio.util.Bytes;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Santhosh Kumar Tekuri
 */
public class RawPayload extends Payload implements Closeable{
    public RawPayload(long contentLength, String contentType, List<Encoding> encodings, InputChannel channel){
        super(contentType);
        this.contentLength = contentLength;
        this.encodings = encodings;
        this.channel = channel;
    }

    public RawPayload(String contentType, Bytes bytes){
        this(bytes.size(), contentType, null, null);
        this.bytes = bytes;
    }

    private long contentLength;
    public boolean retain;
    public Bytes bytes;
    public InputChannel channel;

    public final List<Encoding> encodings;

    @Override
    public long getContentLength(){
        return contentLength;
    }

    public void removeEncodings() throws Exception{
        if(encodings==null || encodings.isEmpty())
            return;

        if(bytes!=null && !bytes.isEmpty())
            throw new UnsupportedOperationException("trying to remove encodings in middle");

        Client client = channel.getClient();
        TrackingInputFilter trackingFilter = null;
        if(channel instanceof TrackingInputFilter){
            trackingFilter = (TrackingInputFilter)channel;
            client.inPipeline.pop();
        }

        try{
            while(!encodings.isEmpty())
                client.inPipeline.push(encodings.remove(encodings.size()-1).createInputFilter());
        }finally{
            if(trackingFilter!=null)
                client.inPipeline.push(trackingFilter);
            contentLength = -1;
        }
    }

    public void channelClosed(){
        if(!channel.isClosed())
            throw new UnsupportedOperationException("channel is not closed yet");
        if(channel.isEOF() && bytes!=null)
            contentLength = bytes.size();
        channel = null;
    }

    public void doBuffer(ExecutionContext context){
        if(channel!=null && channel.isOpen()){
            if(bytes==null)
                bytes = new Bytes();
            new ReadBytes(bytes).start(channel, new ClosedExecutionContext(context));
        }else
            ListenerUtil.resume(context, null, false);
    }

    public void transferTo(OutputStream out, ExecutionContext context){
        try{
            if(bytes!=null)
                IOUtil.pump(bytes.new InputStream(), out, true, false);

            if(channel!=null && channel.isOpen()){
                WriteToOutputStream writeToOutputStream;
                if(retain){
                    if(bytes==null)
                        bytes = new Bytes();
                    writeToOutputStream = new WriteToOutputStream(out, bytes);
                }else
                    writeToOutputStream = new WriteToOutputStream(out, Bytes.CHUNK_SIZE);
                writeToOutputStream.start(channel, new ClosedExecutionContext(context));
            }else
                ListenerUtil.resume(context, null, false);
        }catch(Throwable thr){
            ListenerUtil.resume(context, thr, false);
        }
    }

    private class ClosedExecutionContext implements ExecutionContext{
        private ExecutionContext userContext;
        private ClosedExecutionContext(ExecutionContext userContext){
            this.userContext = userContext;
        }

        @Override
        public void resume(Throwable thr, boolean timeout){
            channelClosed();
            ListenerUtil.resume(userContext, thr, timeout);
        }
    }

    @Override
    public void close() throws IOException{
        if(channel!=null)
            channel.close();
    }
}
