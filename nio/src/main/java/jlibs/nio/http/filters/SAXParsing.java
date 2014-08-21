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

package jlibs.nio.http.filters;

import jlibs.core.io.ByteArrayOutputStream2;
import jlibs.core.lang.NotImplementedException;
import jlibs.nio.async.XMLFeedTask;
import jlibs.nio.http.HTTPTask;
import jlibs.nio.http.msg.*;
import jlibs.nio.http.msg.spec.values.MediaType;
import jlibs.nio.util.Bytes;
import jlibs.nio.util.NIOUtil;
import jlibs.xml.sax.async.AsyncXMLReader;
import jlibs.xml.sax.async.ChannelInputSource;
import jlibs.xml.sax.async.XMLFeeder;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class SAXParsing implements HTTPTask.ResponseFilter<HTTPTask>, HTTPTask.RequestFilter<HTTPTask>{
    protected boolean parseRequest;
    public SAXParsing(boolean parseRequest){
        this.parseRequest = parseRequest;
    }

    @Override
    public void filter(HTTPTask task) throws Exception{
        Message message = parseRequest ? task.getRequest() : task.getResponse();
        Payload payload = message.getPayload();

        MediaType mt = null;
        String charset = null;
        boolean xmlPayload = false;
        if(payload.getContentLength()!=0 && payload.contentType!=null){
            mt = new MediaType(payload.contentType);
            xmlPayload = mt.isXML();
            charset = mt.getCharset(null);
        }

        if(!xmlPayload || !shouldParse(task, message, mt)){
            task.resume();
            return;
        }


        InputSource is;
        if(payload instanceof RawPayload){
            RawPayload rawPayload = (RawPayload)payload;
            rawPayload.removeEncodings();
            if(rawPayload.channel==null || !rawPayload.channel.isOpen())
                is = new InputSource(rawPayload.bytes.new InputStream());
            else{
                ReadableByteChannel channel = rawPayload.channel;
                boolean retain = retain(rawPayload);
                if(retain){
                    if(rawPayload.bytes==null)
                        rawPayload.bytes = new Bytes();
                }
                if(rawPayload.bytes!=null)
                    channel = new Input(rawPayload.bytes, retain, channel);
                is = new ChannelInputSource(channel);
                is.setEncoding(charset);
                AsyncXMLReader xmlReader = new AsyncXMLReader();
                XMLFeeder feeder = xmlReader.createFeeder(is);
                new XMLFeedTask(feeder).start(rawPayload.channel, (thr, timeout) -> {
                    if(thr!=null)
                        task.resume(parseRequest ? Status.BAD_REQUEST : Status.BAD_RESPONSE, thr);
                    else if(timeout)
                        task.resume(parseRequest ? Status.REQUEST_TIMEOUT : Status.RESPONSE_TIMEOUT);
                    else
                        parsingCompleted(task, xmlReader);
                });
                return;
            }
        }else if(payload instanceof FilePayload){
            FilePayload filePayload = (FilePayload)payload;
            is = new InputSource(filePayload.file.getAbsolutePath());
        }else if(payload instanceof EncodablePayload){
            EncodablePayload encodablePayload = (EncodablePayload)payload;
            ByteArrayOutputStream2 bout = new ByteArrayOutputStream2();
            encodablePayload.encoder.encodeTo(encodablePayload.source, bout);
            is = new InputSource(bout.toByteSequence().asInputStream());
        }else
            throw new NotImplementedException(payload.getClass().getName());

        is.setEncoding(charset);
        AsyncXMLReader xmlReader = new AsyncXMLReader();
        XMLFeeder feeder = xmlReader.createFeeder(is);
        try{
            while(feeder!=null)
                feeder.feed();
            parsingCompleted(task, xmlReader);
        }catch(Throwable thr){
            task.resume(parseRequest ? Status.BAD_REQUEST : Status.BAD_RESPONSE, thr);
        }
    }

    protected boolean retain(RawPayload payload){
        return payload.retain;
    }

    protected boolean shouldParse(HTTPTask task, Message message, MediaType xmlMediaType){
        return true;
    }

    protected abstract void addHandlers(AsyncXMLReader xmlReader) throws Exception;

    protected void parsingCompleted(HTTPTask task, AsyncXMLReader xmlReader){
        task.resume();
    }

    private static class Input implements ReadableByteChannel{
        private ReadableByteChannel channel;
        private Input(Bytes bytes, boolean retain, ReadableByteChannel channel){
            this.channel = channel;

            if(bytes!=null && !bytes.isEmpty())
                iterator = bytes.iterator();
            if(retain)
                bout = bytes.new OutputStream();
        }

        private Iterator<ByteBuffer> iterator;
        private ByteBuffer buffer;

        private Bytes.OutputStream bout;

        @Override
        public int read(ByteBuffer dst) throws IOException{
            int pos = dst.position();

            while(dst.hasRemaining()){
                if(buffer!=null && buffer.hasRemaining())
                    NIOUtil.copy(buffer, dst);

                if(iterator==null)
                    break;
                else if(iterator.hasNext())
                    buffer = iterator.next().duplicate();
                else{
                    iterator = null;
                    break;
                }
            }

            int channelRead = 0;
            if(dst.hasRemaining()){
                try{
                    channelRead = channel.read(dst);
                }catch(IOException ex){
                    if(bout!=null)
                        bout.close();
                    throw ex;
                }
                if(bout!=null){
                    if(channelRead==-1)
                        bout.close();
                    else if(channelRead>0)
                        bout.write(dst.array(), dst.arrayOffset()+dst.position()-channelRead, channelRead);
                }
            }

            int read = dst.position()-pos;
            return read==0 && channelRead==-1 ? -1 : read;
        }

        private boolean open;
        @Override
        public boolean isOpen(){
            return open;
        }

        @Override
        public void close() throws IOException{
            if(open){
                open = false;
                if(bout!=null)
                    bout.close();
                channel.close();
            }
        }
    }
}
