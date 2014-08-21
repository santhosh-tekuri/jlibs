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

import jlibs.core.lang.ImpossibleException;
import jlibs.nio.http.HTTPServer;
import jlibs.nio.http.HTTPTask;
import jlibs.nio.http.msg.*;
import jlibs.nio.http.msg.spec.values.MediaType;
import jlibs.nio.http.msg.spec.values.QualityItem;
import jlibs.nio.util.Bytes;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.BiConsumer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class CreateXMLError implements HTTPTask.ResponseFilter<HTTPServer.Task>{
    private static XMLOutputFactory factory = XMLOutputFactory.newInstance();

    protected boolean showStackTrace;
    public CreateXMLError(boolean showStackTrace){
        this.showStackTrace = showStackTrace;
    }

    @Override
    public void filter(HTTPServer.Task task) throws Exception{
        Request request = task.getRequest();
        MediaType mt = request.getPayload().getMediaType();
        if(mt==null || !mt.isXML()){
            mt = null;
            for(QualityItem<MediaType> acceptable: request.getAcceptableMediaTypes()){
                if(acceptable.item.isXML()){
                    mt = acceptable.item;
                    break;
                }
            }
            if(mt!=null)
                createResponse(task, mt.withCharset("UTF-8").toString(), this::createXMLError);
        }else{
            if(mt.isCompatible(MediaType.TEXT_XML) && request.getSOAPAction()!=null)
                createResponse(task, "text/xml; charset=UTF-8", this::createSoap11Error);
            else if(mt.isCompatible(MediaType.SOAP_1_2))
                createResponse(task, "application/soap+xml; charset=UTF-8", this::createSoap12Error);
            else
                createResponse(task, "text/xml; charset=UTF-8", this::createXMLError);
        }

        task.resume();
    }

    private void createResponse(HTTPServer.Task task, String mediaType, BiConsumer<HTTPServer.Task, OutputStream> consumer) throws IOException{
        Response response = task.getResponse();
        if(response==null){
            task.setResponse(response=new Response());
            response.statusCode = task.getErrorCode();
            response.reasonPhrase = task.getErrorPhrase();
        }
        Bytes bytes = new Bytes();
        consumer.accept(task, bytes.new OutputStream());
        response.setPayload(new RawPayload(mediaType, bytes), true);
    }

    protected String getErrorCode(HTTPServer.Task task){
        return String.valueOf(task.getErrorCode());
    }

    protected void createXMLError(HTTPServer.Task task, OutputStream out){
        XMLStreamWriter xml = null;
        try{
            xml = factory.createXMLStreamWriter(out);
            xml.writeStartDocument();
            xml.writeStartElement("fault");
            xml.writeStartElement("faultstring");
            xml.writeCharacters(task.getErrorPhrase());
            xml.writeEndElement();

            xml.writeStartElement("detail");
            xml.writeStartElement("errorcode");
            xml.writeCharacters(getErrorCode(task));
            xml.writeEndElement();

            if(task.getError()!=null && showStackTrace){
                xml.writeStartElement("trace");
                xml.writeCharacters(getStackTrace(task.getError()));
                xml.writeEndElement();
            }

            xml.writeEndElement();
            xml.writeEndElement();
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

    protected String getFaultActor(HTTPServer.Task task){
        return "";
    }

    protected static final String SOAP11_PREFIX = "soap";
    protected static final String SOAP11_URI = "http://schemas.xmlsoap.org/soap/envelope/";
    protected void createSoap11Error(HTTPServer.Task task, OutputStream out){
        String faultCode = Status.isClientError(task.getErrorCode()) ? "soap:Client" : "soap:Server";
        String faultString = task.getErrorPhrase();
        String faultActor = getFaultActor(task);
        String errorCode = getErrorCode(task);

        XMLStreamWriter xml = null;
        try{
            xml = factory.createXMLStreamWriter(out);
            xml.writeStartDocument();

            xml.writeStartElement(SOAP11_PREFIX, "Envelope", SOAP11_URI);
            xml.setPrefix(SOAP11_PREFIX, SOAP11_URI);
            xml.writeNamespace(SOAP11_PREFIX, SOAP11_URI);
            xml.writeAttribute(SOAP11_PREFIX, SOAP11_URI, "encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");
            xml.writeStartElement(SOAP11_PREFIX, "Body", SOAP11_URI);
            xml.writeStartElement(SOAP11_PREFIX, "Fault", SOAP11_URI);
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

            if(task.getError()!=null && showStackTrace){
                xml.writeStartElement("trace");
                xml.writeCharacters(getStackTrace(task.getError()));
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

    protected static final String SOAP12_PREFIX = "env";
    protected static final String SOAP12_URI = "http://www.w3.org/2003/05/soap-envelope";
    protected void createSoap12Error(HTTPServer.Task task, OutputStream out){
        String codeValue = Status.isClientError(task.getErrorCode()) ? "env:Sender" : "env:Receiver";
        String reasonString = task.getErrorPhrase();
        String subcodeValue = getErrorCode(task);

        XMLStreamWriter xml = null;
        try{
            xml = factory.createXMLStreamWriter(out);
            xml.writeStartDocument();
            xml.writeStartElement(SOAP12_PREFIX, "Envelope", SOAP12_URI);
            xml.setPrefix(SOAP12_PREFIX, SOAP12_URI);
            xml.writeNamespace(SOAP12_PREFIX, SOAP12_URI);
            xml.writeStartElement(SOAP12_PREFIX, "Body", SOAP12_URI);
            xml.writeStartElement(SOAP12_PREFIX, "Fault", SOAP12_URI);
            xml.writeStartElement(SOAP12_PREFIX, "Code", SOAP12_URI);

            xml.writeStartElement(SOAP12_PREFIX, "Value", SOAP12_URI);
            xml.writeCharacters(codeValue);
            xml.writeEndElement(/*Value*/);

            xml.writeStartElement(SOAP12_PREFIX, "Subcode", SOAP12_URI);
            xml.writeStartElement(SOAP12_PREFIX, "Value", SOAP12_URI);
            xml.writeCharacters(subcodeValue);
            xml.writeEndElement(/*Value*/);
            xml.writeEndElement(/*SubCode*/);
            xml.writeEndElement(/*Code*/);

            xml.writeStartElement(SOAP12_PREFIX, "Reason", SOAP12_URI);
            xml.writeStartElement(SOAP12_PREFIX, "Text", SOAP12_URI);
            xml.writeAttribute("xml", "http://www.w3.org/XML/1998/namespace", "lang", "en");
            xml.writeCharacters(reasonString);
            xml.writeEndElement(/*Text*/);
            xml.writeEndElement(/*Reason*/);

            if(task.getError()!=null && showStackTrace){
                xml.writeStartElement(SOAP12_PREFIX, "Detail", SOAP12_URI);
                xml.writeCharacters(getStackTrace(task.getError()));
                xml.writeEndElement();
            }

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

    private static String getStackTrace(Throwable thr) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bout);
        thr.printStackTrace(ps);
        ps.close();
        return bout.toString();
    }
}
