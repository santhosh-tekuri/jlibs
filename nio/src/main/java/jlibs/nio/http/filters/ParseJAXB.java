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
import jlibs.nio.http.Exchange;
import jlibs.nio.http.SocketPayload;
import jlibs.nio.http.msg.JAXBPayload;
import jlibs.nio.http.msg.Message;
import jlibs.xml.sax.async.AsyncXMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.UnmarshallerHandler;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ParseJAXB extends ParseXML{
    public final JAXBContext jaxbContext;
    public ParseJAXB(JAXBContext jaxbContext){
        this.jaxbContext = jaxbContext;
    }

    @Override
    protected boolean retain(SocketPayload payload){
        return false;
    }

    @Override
    protected void addHandlers(AsyncXMLReader xmlReader) throws Exception{
        xmlReader.setContentHandler(jaxbContext.createUnmarshaller().getUnmarshallerHandler());
    }

    @Override
    protected void parsingCompleted(Exchange exchange, Message msg, AsyncXMLReader xmlReader){
        UnmarshallerHandler handler = (UnmarshallerHandler)xmlReader.getContentHandler();
        try{
            String contentType = msg.getPayload().getMediaType().withCharset(IOUtil.UTF_8.name()).toString();
            msg.setPayload(new JAXBPayload(contentType, handler.getResult(), jaxbContext));
        }catch(Throwable thr){
            exchange.resume(thr);
            return;
        }
        super.parsingCompleted(exchange, msg, xmlReader);
    }
}
