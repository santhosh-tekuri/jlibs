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

import jlibs.core.io.IOUtil;
import jlibs.nio.http.HTTPTask;
import jlibs.nio.http.encoders.JAXBEncoder;
import jlibs.nio.http.msg.EncodablePayload;
import jlibs.nio.http.msg.Message;
import jlibs.nio.http.msg.Payload;
import jlibs.nio.http.msg.RawPayload;
import jlibs.xml.sax.async.AsyncXMLReader;

import javax.xml.bind.UnmarshallerHandler;

/**
 * @author Santhosh Kumar Tekuri
 */
public class JAXBUnmarshalling extends SAXParsing{
    protected JAXBEncoder encoder;
    public JAXBUnmarshalling(boolean parseRequest, JAXBEncoder encoder){
        super(parseRequest);
        this.encoder = encoder;
    }

    @Override
    protected boolean retain(RawPayload payload){
        return false;
    }

    @Override
    protected void addHandlers(AsyncXMLReader xmlReader) throws Exception{
        xmlReader.setContentHandler(encoder.jaxbContext.createUnmarshaller().getUnmarshallerHandler());
    }

    @Override
    protected void parsingCompleted(HTTPTask task, AsyncXMLReader xmlReader){
        UnmarshallerHandler handler = (UnmarshallerHandler)xmlReader.getContentHandler();
        try{
            parsingCompleted(task, handler.getResult());
        }catch(Exception ex){
            task.resume(ex);
        }
    }

    protected void parsingCompleted(HTTPTask task, Object jaxbObject) throws Exception{
        Message message = parseRequest ? task.getRequest() : task.getResponse();
        Payload payload = message.getPayload();
        String contentType = payload.getMediaType().withCharset(IOUtil.UTF_8.name()).toString();
        payload = new EncodablePayload<>(contentType, jaxbObject, encoder);
        message.setPayload(payload, true);
        task.resume();
    }
}
