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

package jlibs.nio.http.msg.parser;

import jlibs.nio.http.msg.*;
import jlibs.nio.http.util.ContentDisposition;
import jlibs.nio.http.util.USAscii;
import jlibs.nio.util.Parser;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import static jlibs.nio.http.msg.parser.MultipartParser.State.*;
import static jlibs.nio.http.util.USAscii.*;

/**
 * @author Santhosh Kumar Tekuri
 *
 * todo: handle Content-Transfer-Encoding
 */
public class MultipartParser implements Parser{
    public final MultipartPayload payload;
    private Status errorStatus;
    public MultipartParser(MultipartPayload payload, Status errorStatus){
        this.payload = payload;
        this.errorStatus = errorStatus;

        String boundary = payload.getMediaType().getBoundary();
        delimiter = ByteBuffer.allocate(4+boundary.length()+2);
        delimiter.put(CR);
        delimiter.put(LF);
        delimiter.put(DASH);
        delimiter.put(DASH);
        USAscii.append(delimiter, boundary);
        delimiter.put(CR);
        delimiter.put(LF);
        delimiter.flip();

        closeDelimiter = ByteBuffer.allocate(4+boundary.length()+4);
        closeDelimiter.put(CR);
        closeDelimiter.put(LF);
        closeDelimiter.put(DASH);
        closeDelimiter.put(DASH);
        USAscii.append(closeDelimiter, boundary);
        closeDelimiter.put(DASH);
        closeDelimiter.put(DASH);
        closeDelimiter.put(CR);
        closeDelimiter.put(LF);
        closeDelimiter.flip();

        state = DELIMITER;
        delimiter.get();
        delimiter.get();
    }

    private ByteBuffer delimiter;
    private ByteBuffer closeDelimiter;

    enum State { HEADERS, CONTENT, DELIMITER, CLOSE_DELIMITER, DRAIN }
    private State state;
    private Part part;
    private FileChannel fileChannel;
    private HeadersParser headersParser = new HeadersParser();

    @Override
    public boolean parse(ByteBuffer buffer, boolean eof) throws IOException{
        char ch;
        int pos = buffer.position();
        while(buffer.hasRemaining()){
            switch(state){
                case HEADERS:
                    if(!headersParser.parse(buffer, eof)){
                        if(eof)
                            throw new EOFException();
                        return false;
                    }
                    state = CONTENT;
                    pos = buffer.position();
                    if(!buffer.hasRemaining())
                        break;

                    Headers headers = headersParser.getHeaders();
                    ContentDisposition cd = headers.getSingleValue(Message.CONTENT_DISPOSITION, ContentDisposition::new);
                    if(cd!=null && cd.getFileName()!=null){
                        File file = File.createTempFile("jlibs", "upload");
                        part = new FilePart(file, headers);
                        fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
                    }
                    if(part==null)
                        part = new DefaultPart(headersParser.getHeaders());
                    payload.parts.add(part);
                case CONTENT:
                    while(buffer.hasRemaining()){
                        ch = (char)buffer.get();
                        if(ch==CR){
                            state = DELIMITER;
                            delimiter.get();
                            break;
                        }
                    }
                    if(!buffer.hasRemaining())
                        break;
                case DELIMITER:
                    while(buffer.hasRemaining()){
                        if(delimiter.position()==delimiter.capacity()-2){
                            ch = (char)buffer.get();
                            if(ch==DASH){
                                delimiter.clear();
                                state = CLOSE_DELIMITER;
                                closeDelimiter.position(closeDelimiter.capacity()-3);
                                break;
                            }else
                                buffer.position(buffer.position()-1);
                        }
                        if(delimiter.hasRemaining()){
                            if(buffer.get()!=delimiter.get()){
                                buffer.position(buffer.position()-1);
                                delimiter.position(delimiter.position()-1);
                                writeMissing(pos, buffer, delimiter);
                                state = CONTENT;
                                break;
                            }
                        }else{
                            writeContent(pos, buffer, delimiter);
                            pos = buffer.position();
                            if(fileChannel!=null){
                                fileChannel.close();
                                fileChannel = null;
                            }
                            state = HEADERS;
                            headersParser.reset(new Headers(), errorStatus);
                            part = null;
                            break;
                        }
                    }
                    break;
                case CLOSE_DELIMITER:
                    while(buffer.hasRemaining() && closeDelimiter.hasRemaining()){
                        if(buffer.get()!=closeDelimiter.get()){
                            buffer.position(buffer.position()-1);
                            closeDelimiter.position(closeDelimiter.position()-1);
                            writeMissing(pos, buffer, closeDelimiter);
                            state = CONTENT;
                            break;
                        }
                    }
                    if(state==CLOSE_DELIMITER && !closeDelimiter.hasRemaining()){
                        writeContent(pos, buffer, closeDelimiter);
                        if(fileChannel!=null){
                            fileChannel.close();
                            fileChannel = null;
                        }
                        pos = buffer.position();
                        state = DRAIN;
                    }else
                        break;
                case DRAIN:
                    buffer.position(buffer.limit());
            }
        }
        if(eof){
            if(state!=DRAIN)
                throw new EOFException();
        }else{
            if(state==CONTENT){
                ByteBuffer src = buffer.duplicate();
                src.position(pos);
                src.limit(buffer.position());
                write(src);
            }else if(state==DELIMITER || state==CLOSE_DELIMITER){
                ByteBuffer _delimiter = state==DELIMITER ? delimiter : closeDelimiter;
                if(buffer.position()-pos>_delimiter.position()){
                    ByteBuffer src = buffer.duplicate();
                    src.position(pos);
                    src.limit(buffer.position()-_delimiter.position());
                    write(src);
                }
            }
        }
        return eof;
    }

    private void writeMissing(int pos, ByteBuffer buffer, ByteBuffer delimiter) throws IOException{
        if(buffer.position()-pos<delimiter.position()){
            delimiter.limit(delimiter.position()-(buffer.position()-pos));
            delimiter.position(0);
            write(delimiter);
        }
        delimiter.clear();
    }

    private void writeContent(int pos, ByteBuffer buffer, ByteBuffer delimiter) throws IOException{
        if(buffer.position()-pos>delimiter.capacity()){
            ByteBuffer src = buffer.duplicate();
            src.position(pos);
            src.limit(buffer.position()-delimiter.capacity());
            write(src);
        }
        delimiter.clear();
    }

    private void write(ByteBuffer src) throws IOException{
        if(part==null)
            src.position(src.limit());
        else if(part instanceof FilePart){
            while(src.hasRemaining())
                fileChannel.write(src);
        }else
            ((DefaultPart)part).buffers.write(src);
    }

    @Override
    public void cleanup(){
        try{
            if(fileChannel!=null){
                fileChannel.close();
                fileChannel = null;
            }
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }
    }
}
