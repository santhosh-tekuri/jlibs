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

import jlibs.nio.Reactor;
import jlibs.nio.http.Exchange;
import jlibs.nio.http.SocketPayload;
import jlibs.nio.http.msg.FilePart;
import jlibs.nio.http.msg.Message;
import jlibs.nio.http.msg.MultipartPayload;
import jlibs.nio.http.msg.Part;
import jlibs.nio.http.msg.parser.MultipartParser;
import jlibs.nio.http.util.MediaType;
import jlibs.nio.listeners.IOListener;
import jlibs.nio.listeners.Task;
import jlibs.nio.util.BufferAllocator;

import java.io.IOException;
import java.nio.ByteBuffer;

import static java.nio.channels.SelectionKey.OP_READ;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ParseMultipart extends ParseSocketPayload{
    @Override
    protected boolean isCompatible(MediaType mt){
        return mt.isMultipart();
    }

    @Override
    protected boolean parse(Exchange exchange, Message msg, SocketPayload payload, MediaType mt) throws Exception{
        MultipartParser parser = new MultipartParser(new MultipartPayload(mt.toString()), msg.badMessageStatus());

        try{
            if(payload.buffers!=null){
                while(payload.buffers.hasRemaining())
                    parser.parse(payload.buffers.remove(), false);
            }
            if(!payload.socket().isOpen()){
                parser.parse(BufferAllocator.EMPTY_BUFFER, true);
                msg.setPayload(parser.payload);
                parser.cleanup();
                return true;
            }
        }catch(Exception ex){
            parser.cleanup();
            if(ex instanceof IOException)
                throw msg.badMessage(ex);
            throw ex;
        }

        new IOListener().start(new ParseTask(exchange, msg, parser), payload.socket(), null);
        return false;
    }

    private static class ParseTask extends Task{
        private Exchange exchange;
        private Message msg;
        private MultipartParser parser;
        private ByteBuffer buffer = Reactor.current().allocator.allocate();
        private ParseTask(Exchange exchange, Message msg, MultipartParser parser){
            super(OP_READ);
            this.exchange = exchange;
            this.msg = msg;
            this.parser = parser;
        }

        @Override
        protected boolean process(int readyOp) throws IOException{
            while(true){
                int read = in.read(buffer);
                if(read==0){
                    in.addReadInterest();
                    return false;
                }

                buffer.flip();
                if(parser.parse(buffer, read==-1)){
                    msg.setPayload(parser.payload);
                    return true;
                }
                buffer.compact();
            }
        }

        @Override
        protected void cleanup(Throwable thr){
            if(buffer!=null){
                Reactor.current().allocator.free(buffer);
                buffer = null;
                parser.cleanup();
                try{
                    in.close();
                }catch(IOException ex){
                    Reactor.current().handleException(ex);
                }
                if(msg.getPayload()!=parser.payload){
                    for(Part part: parser.payload.parts){
                        if(part instanceof FilePart)
                            ((FilePart)part).file.delete();
                    }
                }

                exchange.resume(thr);
            }
        }
    }
}