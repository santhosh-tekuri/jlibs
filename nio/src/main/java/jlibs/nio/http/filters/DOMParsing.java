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
import jlibs.nio.http.encoders.DOMEncoder;
import jlibs.nio.http.msg.EncodablePayload;
import jlibs.nio.http.msg.Message;
import jlibs.nio.http.msg.RawPayload;
import jlibs.nio.http.msg.spec.values.MediaType;
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.sax.async.AsyncXMLReader;
import jlibs.xml.xsl.TransformerUtil;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.TransformerHandler;

/**
 * @author Santhosh Kumar Tekuri
 */
public class DOMParsing extends SAXParsing{
    public DOMParsing(boolean parseRequest){
        super(parseRequest);
    }

    @Override
    protected boolean retain(RawPayload payload){
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
    protected void parsingCompleted(HTTPTask task, AsyncXMLReader xmlReader){
        TransformerHandler handler = (TransformerHandler)xmlReader.getContentHandler();
        DOMResult result = (DOMResult)handler.getTransformer().getParameter(DOMResult.class.getName());
        Message message = parseRequest ? task.getRequest() : task.getResponse();
        MediaType mediaType = message.getPayload().getMediaType().withCharset(IOUtil.UTF_8.name());
        try{
            message.setPayload(new EncodablePayload<>(mediaType.toString(), result.getNode(), DOMEncoder.INSTANCE), true);
        }catch(Throwable thr){
            task.resume(thr);
            return;
        }
        super.parsingCompleted(task, xmlReader);
    }
}
