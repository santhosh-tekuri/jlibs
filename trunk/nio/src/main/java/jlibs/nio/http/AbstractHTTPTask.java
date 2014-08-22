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

import jlibs.core.lang.NotImplementedException;
import jlibs.core.util.StackedIterator;
import jlibs.nio.Client;
import jlibs.nio.Debugger;
import jlibs.nio.Reactor;
import jlibs.nio.async.*;
import jlibs.nio.channels.OutputChannel;
import jlibs.nio.channels.filters.ChunkedInputFilter;
import jlibs.nio.channels.filters.ChunkedOutputFilter;
import jlibs.nio.channels.filters.FixedLengthInputFilter;
import jlibs.nio.channels.filters.IdentityOutputFilter;
import jlibs.nio.http.async.WriteMultipart;
import jlibs.nio.http.msg.*;
import jlibs.nio.http.msg.spec.values.Encoding;
import jlibs.nio.util.Bytes;
import jlibs.nio.util.Line;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static jlibs.nio.http.msg.Headers.*;
import static jlibs.nio.http.msg.Method.*;

/**
 * @author Santhosh Kumar Tekri
 */
public abstract class AbstractHTTPTask<T extends AbstractHTTPTask> implements HTTPTask<T>{
    protected AbstractHTTPTask(int initialLineLimit, int headerLimit, int headersLimit,
                               AccessLog accessLog, boolean supportsProxyConnectionHeader,
                               LinkedList<RequestFilter> requestFilters,
                               LinkedList<ResponseFilter> responseFilters,
                               LinkedList<ResponseFilter> errorFilters
    ){
        this.initialLineLimit = initialLineLimit;
        this.headerLimit = headerLimit;
        this.headersLimit = headersLimit;
        this.accessLog = accessLog;
        if(accessLog!=null)
            accessLogRecord = accessLog.new Record();
        this.supportsProxyConnectionHeader = supportsProxyConnectionHeader;
        this.requestFilters = requestFilters;
        this.responseFilters = responseFilters;
        this.errorFilters = errorFilters;
    }

    /*-------------------------------------------------[ Flow ]---------------------------------------------------*/

    private LinkedList<RequestFilter> requestFilters = new LinkedList<>();
    private LinkedList<ResponseFilter> responseFilters = new LinkedList<>();
    private LinkedList<ResponseFilter> errorFilters = new LinkedList<>();

    protected enum Stage{
        BEGIN,
        PREPARE_REQUEST,
        FILTER_REQUEST,
        PREPARE_RESPONSE,
        FILTER_RESPONSE,
        DELIVER_RESPONSE,
        FILTER_ERROR,
    }
    protected Stage stage = Stage.BEGIN;
    protected Iterator<? extends Filter> filters = null;

    public void resume(int errorCode, String reasonPhrase, Throwable thr){
        if(Debugger.HTTP)
            Debugger.println(this+".resume("+thr+", "+errorCode+", "+reasonPhrase+")");
        if(stage==Stage.DELIVER_RESPONSE)
            throw new IllegalStateException("nothing to resume");
        this.thr = thr;
        this.errorCode = errorCode;
        this.errorPhrase = reasonPhrase;
        if(isOpen()){
            if(stage==Stage.FILTER_ERROR){
                client.makeActive();
                stage = Stage.DELIVER_RESPONSE;
                if(Debugger.HTTP)
                    Debugger.println(this+".stage = "+stage);
                filters = null;
                deliverResponse();
            }else{
                stage = Stage.FILTER_ERROR;
                if(Debugger.HTTP)
                    Debugger.println(this+".stage = "+stage);
                filters = errorFilters.isEmpty() ? Collections.emptyIterator() : errorFilters.iterator();
                resume();
            }
        }else
            notifyUser(thr, -1);
    }

    public void resume(){
        if(client!=null)
            client.makeActive();
        if(Debugger.HTTP)
            Debugger.println(this+".resume()");
        if(stage==Stage.BEGIN){
            stage = Stage.PREPARE_REQUEST;
            if(Debugger.HTTP)
                Debugger.println(this+".stage = "+stage);
            filters = null;
            prepareRequest();
            return;
        }

        if(stage==Stage.FILTER_REQUEST){
            if(response!=null){
                stage = Stage.FILTER_RESPONSE;
                if(Debugger.HTTP)
                    Debugger.println(this+".stage = "+stage);
                filters = responseFilters.isEmpty() ? Collections.emptyIterator() : responseFilters.iterator();
            }else if(filters.hasNext()){
                processFilter();
                return;
            }else{
                stage = Stage.PREPARE_RESPONSE;
                if(Debugger.HTTP)
                    Debugger.println(this+".stage = "+stage);
                filters = null;
                prepareResponse();
                return;
            }
        }

        if(stage==Stage.FILTER_RESPONSE || stage==Stage.FILTER_ERROR){
            if(filters.hasNext())
                processFilter();
            else{
                stage = Stage.DELIVER_RESPONSE;
                if(Debugger.HTTP)
                    Debugger.println(this+".stage = "+stage);
                filters = null;
                deliverResponse();
            }
        }
    }

    protected void _resume(){
        if(stage==Stage.PREPARE_REQUEST){
            stage = Stage.FILTER_REQUEST;
            if(Debugger.HTTP)
                Debugger.println(this+".stage = "+stage);
            filters = requestFilters.isEmpty() ? Collections.emptyIterator() : requestFilters.iterator();
            resume();
        }else if(stage==Stage.PREPARE_RESPONSE){
            stage = Stage.FILTER_RESPONSE;
            if(Debugger.HTTP)
                Debugger.println(this+".stage = "+stage);
            filters = responseFilters.isEmpty() ? Collections.emptyIterator() : responseFilters.iterator();
            resume();
        }
    }

    protected void restart(){
        stage = Stage.PREPARE_REQUEST;
        if(Debugger.HTTP)
            Debugger.println(this+".stage = "+stage);
        filters = null;
        thr = null;
        errorCode = -1;
        errorPhrase = null;
        beginTime = endTime = -1;
        requestHeadSize = requestPayloadSize = -1;
        responseHeadSize = responsePayloadSize = -1;
        connectionStatus = null;
        prepareRequest();
    }

    @SuppressWarnings("unchecked")
    private void processFilter(){
        Filter filter = filters.next();
        try{
            if(Debugger.HTTP)
                Debugger.println(filter.getClass().getSimpleName()+".filter("+getClass().getSimpleName()+")");
            filter.filter(this);
        }catch(Throwable thr){
            resume(thr);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void pushFilters(Iterator<? extends Filter> filters){
        if(this.filters!=null){
            if(!(this.filters instanceof StackedIterator))
                this.filters = new StackedIterator(this.filters);
            ((StackedIterator)this.filters).push(filters);
        }
    }

    protected abstract void prepareRequest();
    protected abstract void prepareResponse();
    protected abstract void deliverResponse();

    /*-------------------------------------------------[ Reading ]---------------------------------------------------*/

    private int initialLineLimit;
    private int headerLimit;
    private int headersLimit;
    private Line line;
    protected Message message;
    private final boolean supportsProxyConnectionHeader;
    private boolean hasProxyConnectionHeader;

    protected final void readMessage(Message message){
        if(Debugger.HTTP)
            Debugger.println(client.in()+".readMessage("+message.getClass().getSimpleName()+")");
        this.message = message;
        client.in().startInputMetric();
        if(line==null)
            line = new Line();
        line.reset(headersLimit, initialLineLimit, headerLimit);
        new ReadLines(line, message)
                .ignoreEOF(message instanceof Request)
                .start(client.in(), this::initPayload);
    }

    private void initPayload(Throwable thr, boolean timeout){
        Message message = this.message;
        this.message = null;
        if(message instanceof Request)
            requestHeadSize = client.in().stopInputMetric();
        else
            responseHeadSize = client.in().stopInputMetric();

        try{
            if(accessLog!=null)
                accessLogRecord.process(this, message, AccessLog.Type.ORIGINAL);
        }catch(Exception ex){
            Reactor.current().handleException(ex);
        }
        if(message instanceof Request){
            if(supportsProxyConnectionHeader){
                Header header = request.headers.remove(PROXY_CONNECTION);
                if(header!=null){
                    hasProxyConnectionHeader = true;
                    request.headers.set(CONNECTION, header.getValue());
                }
            }
            assert keepAlive;
            requestVersion = message.version;
            requestMethod = ((Request)message).method;
        }

        keepAlive = keepAlive && message.isKeepAlive();

        if(thr!=null || timeout){
            if(Debugger.HTTP)
                Debugger.println(client.in()+".initPayload("+thr+", "+timeout+")");
            readMessageCompleted(thr, timeout);
            return;
        }

        if(Debugger.HTTP){
            Debugger.println(client.in()+".initPayload(){");
            Debugger.println(message);
        }

        String contentType = message.headers.value(CONTENT_TYPE);
        List<Encoding> encodings = message.getEncodings();

        long contentLength;
        if(message instanceof Response && requestMethod==HEAD)
            contentLength = 0;
        else{
            contentLength = message.getContentLength();
            if(contentLength==-1 && encodings.isEmpty()){
                if(message instanceof Request)
                    contentLength = 0;
                else if(message instanceof Response){
                    Response response = (Response)message;
                    if(Status.isPayloadNotAllowed(response.statusCode) || response.isKeepAlive())
                        contentLength = 0;
                }
            }
        }

        try{
            Payload payload = Payload.NO_PAYLOAD;
            if(contentLength!=0){
                client.in().startInputMetric();
                if(contentLength!=-1)
                    client.inPipeline.push(new FixedLengthInputFilter(contentLength));
                else if(!encodings.isEmpty()){
                    if(encodings.get(encodings.size()-1).equals(Encoding.CHUNKED))
                        client.inPipeline.push(encodings.remove(encodings.size()-1).createInputFilter());
                    else{
                        if(message instanceof Request || message.isKeepAlive())
                            client.inPipeline.push(encodings.remove(encodings.size()-1).createInputFilter());
                    }
                }
                if(client.in() instanceof ChunkedInputFilter)
                    ((ChunkedInputFilter)client.in()).setLineConsumer(message.headers);
                addTrackingFilters();
                payload = new RawPayload(contentLength, contentType, encodings, client.in());
            }
            if(Debugger.HTTP)
                Debugger.println("payload: "+client.in());
            message.setPayload(payload, true);
        }catch(Throwable thr1){
            readMessageCompleted(thr1, false);
            return;
        }
        readMessageCompleted(null, false);
        if(Debugger.HTTP)
            Debugger.println("}");
    }

    protected abstract void addTrackingFilters();

    protected abstract void readMessageCompleted(Throwable thr, boolean timeout);

    protected void drainInputFilters(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            while(!client.inPipeline.empty())
                client.inPipeline.pop();
            drainedInputFilters(thr, timeout);
            return;
        }
        if(client.in().isClosed())
            client.inPipeline.pop();
        if(client.inPipeline.empty())
            drainedInputFilters(null, false);
        else
            new DrainInput().start(client.in(), this::drainInputFilters);
    }

    protected abstract void drainedInputFilters(Throwable thr, boolean timeout);

    /*-------------------------------------------------[ Writing ]---------------------------------------------------*/

    private boolean chunkedOutput;
    private OutputChannel socketOutput;
    private Payload writePayload;
    protected final void writeMessage(Message message){
        try{
            this.message = message;
            chunkedOutput = false;

            message.headers.set(CONTENT_TYPE, message.getPayload().contentType);
            writePayload = message.getPayload();
            if(writePayload instanceof RawPayload){
                RawPayload rawPayload = (RawPayload)message.getPayload();
                List<Encoding> encodings = rawPayload.encodings;
                if(encodings!=null && !encodings.isEmpty())
                    message.setContentEncodings(encodings);
            }

            if(writePayload.getContentLength()>0)
                message.setContentLength(writePayload.getContentLength());
            else if(writePayload.getContentLength()==0){
                boolean removeContentLength = false;
                if(message instanceof Request){
                    Method method = ((Request)message).method;
                    if(method==GET || method==TRACE)
                        removeContentLength = true;
                }else{
                    int statusCode = ((Response)message).statusCode;
                    if(Status.isPayloadNotAllowed(statusCode))
                        removeContentLength = true;
                }
                message.setContentLength(removeContentLength ? -1 : 0);
                if(removeContentLength)
                    message.headers.remove(TRANSFER_ENCODING);
                message.headers.remove(CONTENT_ENCODING);
            }else if(message.getPayload() instanceof EncodablePayload){
                writePayload = ((EncodablePayload)message.getPayload()).toRawPayload();
                message.setContentLength(writePayload.getContentLength());
            }else{
                chunkedOutput = true;
                message.setChunked();
            }

            if(hasProxyConnectionHeader && message instanceof Response){
                Header header = response.headers.remove(CONNECTION);
                if(header!=null)
                    response.headers.set(PROXY_CONNECTION, header.getValue());
            }

            if(Debugger.HTTP){
                Debugger.println(client.out()+".writeMessage("+message.getClass().getSimpleName()+"){");
                Debugger.println(message.toString().trim());
                Debugger.println("}");
            }

            client.out().startOutputMetric();
            new WriteBytes(message).start(client.out(), this::writePayload);
        }catch(Throwable thr){
            _writeMessageCompleted(thr, false);
        }
    }

    protected void writePayload(Throwable thr, boolean timeout){
        if(message instanceof Request)
            requestHeadSize = client.out().stopOutputMetric();
        else
            responseHeadSize = client.out().stopOutputMetric();
        socketOutput = client.out();
        client.out().startOutputMetric();
        if(thr!=null || timeout){
            if(Debugger.HTTP)
                Debugger.println(client.out()+".writePayload("+thr+", "+timeout+")");
            _writeMessageCompleted(thr, timeout);
            return;
        }
        if(Debugger.HTTP)
            Debugger.println(client.out()+".writePayload()");
        if(writePayload.getContentLength()==0 || (message instanceof Request && ((Request)message).method==HEAD)){
            _writeMessageCompleted(null, false);
            return;
        }

        try{
            if(chunkedOutput)
                client.outPipeline.push(new ChunkedOutputFilter());
            if(writePayload instanceof RawPayload){
                RawPayload rawPayload = (RawPayload)writePayload;
                List<Encoding> encodings = rawPayload.encodings;
                if(encodings==null || encodings.isEmpty()){
                    encodings = message.getContentEncodings();
                    while(!encodings.isEmpty())
                        client.outPipeline.push(encodings.remove(encodings.size()-1).createOutputFilter());
                }
            }
        }catch(Throwable thr1){
            _writeMessageCompleted(thr1, false);
            return;
        }
        if(writePayload instanceof RawPayload){
            RawPayload rawPayload = (RawPayload)writePayload;
            if(rawPayload.bytes!=null){
                new WriteBytes(rawPayload.bytes, !rawPayload.retain).start(client.out(), this::writePayloadSource);
                return;
            }
        }
        writePayloadSource(null, false);
    }

    private void writePayloadSource(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            if(Debugger.HTTP)
                Debugger.println(client.out()+".writePayloadSource("+thr+", "+timeout+")");
            _writeMessageCompleted(thr, timeout);
            return;
        }
        try{
            if(Debugger.HTTP)
                Debugger.println(client.out()+".writePayloadSource()");
            if(writePayload instanceof RawPayload){
                RawPayload rawPayload = (RawPayload)writePayload;
                if(rawPayload.channel==null)
                    closeOutputFilters(null, false);
                else{
                    if(client.outPipeline.empty())
                        client.outPipeline.push(new IdentityOutputFilter());

                    Pump pump;
                    if(rawPayload.retain){
                        if(rawPayload.bytes==null)
                            rawPayload.bytes = new Bytes();
                        pump = new Pump(rawPayload.channel, client.out(), rawPayload.bytes);
                    }else
                        pump = new Pump(rawPayload.channel, client.out());
                    pump.start(this::closeOutputFilters);
                }
            }else if(writePayload instanceof MultipartPayload)
                new WriteMultipart((MultipartPayload)writePayload).start(client.out(), this::closeOutputFilters);
            else if(writePayload instanceof FilePayload){
                FilePayload filePayload = (FilePayload)writePayload;
                new ReadFromInputStream(new FileInputStream(filePayload.file), Bytes.CHUNK_SIZE).start(client.out(), this::closeOutputFilters);
            }else
                _writeMessageCompleted(new NotImplementedException(writePayload.getClass().getName()), false);
        }catch(Throwable thr1){
            _writeMessageCompleted(thr1, false);
        }
    }

    private void closeOutputFilters(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            assert !(message.getPayload() instanceof RawPayload) || !((RawPayload)message.getPayload()).channel.isOpen();
            _writeMessageCompleted(thr, timeout);
            return;
        }
        if(client.out().isClosed())
            client.outPipeline.pop();
        if(client.outPipeline.empty())
            _writeMessageCompleted(null, false);
        else
            CloseOutput.INSTANCE.start(client.out(), this::closeOutputFilters);
    }

    private void _writeMessageCompleted(Throwable thr, boolean timeout){
        if(Debugger.HTTP)
            Debugger.println(client.out()+"._writeMessageCompleted("+thr+", "+timeout+")");
        if(message instanceof Request){
            requestPayloadSize = socketOutput.stopOutputMetric();
            if(requestPayloadSize==0)
                requestPayloadSize = -1;
        }else if(message instanceof Response){
            responsePayloadSize = socketOutput.stopOutputMetric();
            if(responsePayloadSize==0)
                responsePayloadSize = -1;
        }
        try{
            if(accessLog!=null)
                accessLogRecord.process(this, message, AccessLog.Type.FINAL);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        message = null;
        socketOutput = null;
        writePayload = null;
        chunkedOutput = false;
        writeMessageCompleted(thr, timeout);
    }

    protected abstract void writeMessageCompleted(Throwable thr, boolean timeout);

    /*-------------------------------------------------[ UserContext ]---------------------------------------------------*/

    protected Throwable thr;
    protected int errorCode = -1;
    protected String errorPhrase;

    public boolean isSuccess(){
        return thr==null && errorCode==-1;
    }

    public Throwable getError(){
        return thr;
    }

    public int getErrorCode(){
        if(errorCode==-1)
            return thr instanceof HTTPException ? ((HTTPException)thr).statusCode : Status.INTERNAL_SERVER_ERROR;
        else
            return errorCode;
    }

    public String getErrorPhrase(){
        if(errorPhrase==null){
            if(thr==null)
                return Status.message(getErrorCode());
            else{
                if(thr.getMessage()!=null)
                    return thr.getMessage();
                if(thr instanceof HTTPException)
                    return Status.message(((HTTPException)thr).statusCode);
                else
                    return thr.getClass().getSimpleName();
            }
        }

        return errorPhrase;
    }

    protected FinishListener finishListener;

    @SuppressWarnings("unchecked")
    protected void notifyUser(Throwable thr, int timeoutCode){
        if(Debugger.HTTP)
            Debugger.println(client+".notifyUser("+thr+", "+errorCode+"){");
        if(thr!=null && finishListener==null)
            Reactor.current().handleException(thr);
        FinishListener finishListener = this.finishListener;
        this.finishListener = null;
        this.thr = thr;
        errorCode = timeoutCode;

        try{
            if(finishListener!=null)
                finishListener.finished(this);
        }catch(Throwable thr1){
            Reactor.current().handleException(thr1);
        }

        if(Debugger.HTTP)
            Debugger.println("}");
    }

    protected AccessLog accessLog;
    protected AccessLog.Record accessLogRecord;

    /*-------------------------------------------------[ Misc ]---------------------------------------------------*/

    protected Client client;

    @Override
    public Client stealClient(){
        Client client = this.client;
        this.client = null;
        if(client.acceptedFrom()==null)
            client.reactor.clientPool.remove(client);
        return client;
    }

    @Override
    public Reactor getReactor(){
        return client==null ? Reactor.current() : client.reactor;
    }

    public boolean isOpen(){
        return client!=null && client.isOpen();
    }

    public void close(){
        if(client!=null)
            client.close();
    }

    protected Version requestVersion;
    protected Method requestMethod;
    protected boolean keepAlive = true;

    protected Request request;
    @Override public Request getRequest(){ return request; }

    protected Response response;
    @Override public Response getResponse(){ return response; }

    private Object attachment;
    @Override public void attach(Object attachment){ this.attachment = attachment; }
    @Override @SuppressWarnings("unchecked") public <A> A attachment(){ return (A)attachment; }

    protected long beginTime = -1;
    @Override public long getBeginTime(){ return beginTime; }

    protected long endTime = -1;
    @Override public long getEndTime(){ return endTime; }

    protected long requestHeadSize = -1;
    @Override public long getRequestHeadSize(){ return requestHeadSize; }

    protected long responseHeadSize = -1;
    @Override public long getResponseHeadSize(){ return responseHeadSize; }

    protected long requestPayloadSize = -1;
    @Override public long getRequestPayloadSize(){ return requestPayloadSize; }

    protected long responsePayloadSize = -1;
    @Override public long getResponsePayloadSize(){ return responsePayloadSize; }

    protected ConnectionStatus connectionStatus;

    @Override
    public ConnectionStatus getConnectionStatus(){
        return connectionStatus;
    }
}
