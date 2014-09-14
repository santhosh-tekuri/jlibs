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

import jlibs.core.lang.ImpossibleException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Santhosh Kumar Tekuri
 */
public class SOAP11FaultPayload extends EncodablePayload{
    public final Throwable thr;
    public final boolean showStackTrace;
    public SOAP11FaultPayload(Throwable thr, boolean showStackTrace){
        super("text/xml; charset=UTF-8");
        this.thr = thr;
        this.showStackTrace = showStackTrace;
    }

    private static XMLOutputFactory factory = XMLOutputFactory.newInstance();
    private static final String PREFIX = "soap";
    private static final String URI = "http://schemas.xmlsoap.org/soap/envelope/";

    protected String getFaultActor(){
        return "";
    }

    protected String getErrorCode(Throwable detail){
        return detail.getMessage()==null ? detail.getClass().getSimpleName() : detail.getMessage();
    }

    @Override
    public void writeTo(OutputStream out) throws IOException{
        Status status = thr instanceof Status ? (Status)thr : Status.INTERNAL_SERVER_ERROR;
        Throwable detail = thr;
        if(thr instanceof Status && thr.getCause()!=null)
            detail = thr.getCause();

        String faultCode = status.isClientError() ? "soap:Client" : "soap:Server";
        String faultString = status.getMessage();
        String faultActor = getFaultActor();
        String errorCode = getErrorCode(detail);

        XMLStreamWriter xml = null;
        try{
            xml = factory.createXMLStreamWriter(out);
            xml.writeStartDocument();

            xml.writeStartElement(PREFIX, "Envelope", URI);
            xml.setPrefix(PREFIX, URI);
            xml.writeNamespace(PREFIX, URI);
            xml.writeAttribute(PREFIX, URI, "encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");
            xml.writeStartElement(PREFIX, "Body", URI);
            xml.writeStartElement(PREFIX, "Fault", URI);
            xml.writeStartElement("faultcode");
            xml.writeCharacters(faultCode);
            xml.writeEndElement();

            xml.writeStartElement("faultstring");
            xml.writeCharacters(faultString);
            xml.writeEndElement();

            xml.writeStartElement("faultactor");
            xml.writeCharacters(faultActor);
            xml.writeEndElement();

            xml.writeStartElement("detail");
            xml.writeStartElement("source");
            xml.writeStartElement("errorcode");
            xml.writeCharacters(errorCode);
            xml.writeEndElement(/*errorcode*/);

            if(detail!=null && showStackTrace){
                xml.writeStartElement("trace");
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(bout);
                thr.printStackTrace(ps);
                ps.close();
                xml.writeCharacters(bout.toString());
                xml.writeEndElement();
            }

            xml.writeEndElement(/*source*/);
            xml.writeEndElement(/*detail*/);
            xml.writeEndElement(/*Fault*/);
            xml.writeEndElement(/*Body*/);
            xml.writeEndElement(/*Envelope*/);
            xml.writeEndDocument();
            xml.close();
        }catch(XMLStreamException ex){
            throw new ImpossibleException(ex);
        }finally{
            try{
                if(xml!=null)
                    xml.close();
            }catch(XMLStreamException ex){
                throw new ImpossibleException(ex);
            }
        }
    }
}
