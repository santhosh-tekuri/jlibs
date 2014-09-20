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
import jlibs.nio.Reactor;
import jlibs.nio.Writable;
import jlibs.nio.http.msg.*;
import jlibs.nio.http.util.Encoding;
import jlibs.nio.listeners.Task;
import jlibs.nio.util.Buffers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import static java.nio.channels.SelectionKey.OP_WRITE;
import static jlibs.nio.Debugger.HTTP;
import static jlibs.nio.Debugger.println;
import static jlibs.nio.http.WriteMessage.State.*;
import static jlibs.nio.http.util.USAscii.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class WriteMessage extends Task{
    public WriteMessage(){
        super(OP_WRITE);
    }

    enum State{
        WRITE_BUFFER, WRITE_HEAD, FLUSH_HEAD, PREPARE_BUFFERS, WRITE_BUFFERS,
        WRITE_PAYLOAD,
        CLOSE_OUTPUTS
    }

    private State state;

    @Override
    protected boolean process(int readyOp) throws IOException{
        while(true){
            switch(state){
                case FLUSH_HEAD:
                    if(!send(buffer))
                        return false;
                    state = PREPARE_BUFFERS;
                    return true;
                case WRITE_BUFFER:
                    if(write(buffer)){
                        if(header==null){
                            state = PREPARE_BUFFERS;
                            break;
                        }else{
                            buffer.clear();
                            state = WRITE_HEAD;
                        }
                    }else
                        return false;
                case WRITE_HEAD:
                    while(header!=null){
                        if(writeName){
                            AsciiString name = header.getName();
                            do{
                                index = name.putInto(buffer, index);
                                if(buffer.remaining()<2){
                                    buffer.flip();
                                    if(write(buffer))
                                        buffer.clear();
                                    else{
                                        state = WRITE_BUFFER;
                                        return false;
                                    }
                                }
                            }while(index!=name.text.length());
                            buffer.put(COLON);
                            buffer.put(SP);
                            writeName = false;
                            index = 0;
                        }
                        String value = header.getValue();
                        do{
                            int toIndex = Math.min(value.length(), index+buffer.remaining());
                            while(index<toIndex)
                                buffer.put((byte)value.charAt(index++));
                            if(buffer.remaining()<4){
                                buffer.flip();
                                if(write(buffer))
                                    buffer.clear();
                                else{
                                    state = WRITE_BUFFER;
                                    return false;
                                }
                            }
                        }while(index!=value.length());
                        buffer.put(CR);
                        buffer.put(LF);
                        writeName = true;
                        index = 0;
                        header = header.next();
                    }
                    buffer.put(CR);
                    buffer.put(LF);
                    buffer.flip();
                    if(sendPayload)
                        state = PREPARE_BUFFERS;
                    else{
                        state = FLUSH_HEAD;
                        break;
                    }
                case PREPARE_BUFFERS:
                    if(buffers==null){
                        if(buffer.hasRemaining()){
                            state = WRITE_BUFFER;
                            break;
                        }else{
                            state = WRITE_PAYLOAD;
                            break;
                        }
                    }else{
                        if(buffer.hasRemaining()){
                            ByteBuffer array[] = new ByteBuffer[1+buffers.length];
                            array[0] = buffer;
                            System.arraycopy(buffers.array, buffers.offset, array, 1, buffers.length);
                            buffers = new Buffers(array, 0, array.length);
                            prepareFlush(buffers, !retain);
                            if(!retain)
                                buffer = null;
                        }else
                            prepareFlush(buffers, !retain);
                        state = WRITE_BUFFERS;
                    }
                case WRITE_BUFFERS:
                    if(!flushBuffers())
                        return false;
                    state = WRITE_PAYLOAD;
                case WRITE_PAYLOAD:
                    if(writePayload==null)
                        state = CLOSE_OUTPUTS;
                    else{
                        setChild(writePayload);
                        return true;
                    }
                case CLOSE_OUTPUTS:
                    if(error!=null){
                        if(error instanceof IOException)
                            throw (IOException)error;
                        if(error instanceof RuntimeException)
                            throw (RuntimeException)error;
                        throw (Error)error;
                    }
                    return closeOutputs();
            }
        }
    }

    private ByteBuffer buffer;
    private Header header;
    private boolean writeName;
    private int index;
    private boolean retain;
    private Buffers buffers;
    private boolean sendPayload;
    private WritePayload writePayload;
    public void reset(Message message, ByteBuffer continue100Buffer, boolean sendPayload){
        if(buffer==null)
            buffer = Reactor.current().allocator.allocate();
        else
            buffer.clear();
        this.sendPayload = sendPayload;
        writeName = true;
        index = 0;
        retain = false;
        buffers = null;
        writePayload = null;
        error = null;

        Payload payload = message.getPayload();
        message.headers.set(Message.CONTENT_TYPE, payload.contentType);
        if(payload.getContentLength()==0){
            message.headers.remove(Message.CONTENT_ENCODING);

            boolean removeCL = false;
            if(message instanceof Request){
                Request request = (Request)message;
                if(!request.method.requestPayloadAllowed)
                    removeCL = true;
            }else{
                Response response = (Response)message;
                if(response.status.payloadNotAllowed)
                    removeCL = true;
            }
            if(removeCL){
                message.headers.remove(Message.CONTENT_LENGTH);
                message.headers.remove(Message.TRANSFER_ENCODING);
            }else
                message.setContentLength(0);
        }else if(payload instanceof EncodablePayload){
            EncodablePayload encodablePayload = (EncodablePayload)payload;
            buffers = new Buffers();
            OutputStream os = buffers;
            try{
                List<Encoding> encodings = message.getContentEncodings();
                while(!encodings.isEmpty())
                    os = encodings.remove(encodings.size()-1).wrap(os);
                encodablePayload.writeTo(os);
                os.close();
            }catch(IOException ex){
                throw Status.INTERNAL_SERVER_ERROR.with(ex);
            }
            message.setContentLength(buffers.remaining());
        }else if(payload instanceof SocketPayload){
            SocketPayload socketPayload = (SocketPayload)payload;
            if(socketPayload.in.isOpen()){
                writePayload = new WriteSocketPayload(socketPayload);
                long contentLength = socketPayload.getContentLength();
                List<Encoding> encodings = socketPayload.encodings;
                if(encodings!=null && !encodings.isEmpty()){
                    message.setContentEncodings(encodings);
                    if(contentLength!=-1){
                        buffers = socketPayload.buffers;
                        retain = socketPayload.retain;
                        ((WriteSocketPayload)writePayload).ignoreBuffers = true;
                    }
                }else{
                    writePayload.encodings = message.getContentEncodings();
                    if(writePayload.encodings.isEmpty()){
                        if(contentLength!=-1){
                            buffers = socketPayload.buffers;
                            retain = socketPayload.retain;
                            ((WriteSocketPayload)writePayload).ignoreBuffers = true;
                        }
                    }else
                        contentLength = -1;
                }
                if(contentLength==-1){
                    writePayload.chunked = true;
                    message.setChunked();
                }else
                    message.setContentLength(contentLength);
            }else if(socketPayload.buffers==null){
                message.setContentEncodings(null);
                message.setContentLength(-1);
            }else{
                buffers = socketPayload.buffers;
                retain = socketPayload.retain;
                message.setContentEncodings(socketPayload.encodings);
                message.setContentLength(buffers.remaining());
            }
        }else if(payload instanceof FilePayload){
            FilePayload filePayload = (FilePayload)payload;
            writePayload = new WriteFilePayload(filePayload);
            writePayload.encodings = message.getContentEncodings();
            if(writePayload.encodings.isEmpty())
                message.setContentLength(filePayload.getContentLength());
            else{
                writePayload.chunked = true;
                message.setChunked();
            }
        }else
            throw new NotImplementedException("write"+payload.getClass().getSimpleName());

        if(HTTP){
            println("writeMessage{");
            println(message);
            println("}");
        }

        if(continue100Buffer!=null)
            buffer.put(continue100Buffer);
        message.putLineInto(buffer);
        header = message.headers.getFirst();
        if(header==null){
            buffer.put(CR);
            buffer.put(LF);
            buffer.flip();
            state = PREPARE_BUFFERS;
        }else
            state = WRITE_HEAD;
    }

    private Throwable error;
    @Override
    protected int childTaskFinished(Task childTask, Throwable thr){
        error = thr;
        out = ((Writable)out.channel()).out();
        out.channel().makeActive();
        state = CLOSE_OUTPUTS;
        if(HTTP)
            println("state = "+state);
        return OP_WRITE;
    }

    public void dispose(){
        if(buffer!=null){
            Reactor.current().allocator.free(buffer);
            buffer = null;
        }
    }

    @Override
    public String toString(){
        return "WriteMessage";
    }
}
