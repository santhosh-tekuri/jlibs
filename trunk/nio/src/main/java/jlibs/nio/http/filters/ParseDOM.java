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
import jlibs.nio.http.msg.DOMPayload;
import jlibs.nio.http.msg.Message;
import jlibs.nio.http.util.MediaType;
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.sax.async.AsyncXMLReader;
import jlibs.xml.xsl.TransformerUtil;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.TransformerHandler;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ParseDOM extends ParseXML{
    @Override
    protected boolean retain(SocketPayload payload){
        return false;
    }

    @Override
    protected void addHandlers(AsyncXMLReader xmlReader) throws Exception{
        TransformerHandler handler = TransformerUtil.newTransformerHandler(null, false, -1, null);
        SAXUtil.setHandler(xmlReader, handler);
        DOMResult result = createDOMResult();
        handler.setResult(result);
        handler.getTransformer().setParameter(DOMResult.class.getName(), result);
    }

    protected DOMResult createDOMResult(){
        return new DOMResult();
    }

    @Override
    protected void parsingCompleted(Exchange exchange, Message msg, AsyncXMLReader xmlReader){
        TransformerHandler handler = (TransformerHandler)xmlReader.getContentHandler();
        DOMResult result = (DOMResult)handler.getTransformer().getParameter(DOMResult.class.getName());
        MediaType mt = msg.getPayload().getMediaType();
        String contentType = mt.withCharset(IOUtil.UTF_8.name()).toString();
        try{
            msg.setPayload(new DOMPayload(contentType, result.getNode(), false, -1));
        }catch(Throwable thr){
            exchange.resume(thr);
            return;
        }
        super.parsingCompleted(exchange, msg, xmlReader);
    }
}
