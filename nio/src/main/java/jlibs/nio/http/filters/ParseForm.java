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
import jlibs.nio.Reactor;
import jlibs.nio.http.*;
import jlibs.nio.http.msg.Message;
import jlibs.nio.http.msg.parser.FormParser;
import jlibs.nio.http.util.MediaType;
import jlibs.nio.listeners.IOListener;
import jlibs.nio.listeners.Task;
import jlibs.nio.util.BufferAllocator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static java.nio.channels.SelectionKey.OP_READ;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ParseForm extends ParseSocketPayload{
    @Override
    protected boolean isCompatible(MediaType mt){
        return MediaType.APPLICATION_FORM_URLENCODED.isCompatible(mt);
    }

    @Override
    protected boolean parse(Exchange exchange, Message msg, SocketPayload payload, MediaType mt) throws Exception{
        String charset = mt.getCharset(IOUtil.ISO_8859_1.name());
        FormParser parser;
        try{
            parser = new FormParser(Charset.forName(charset));
        }catch(Exception ex){
            throw msg.badMessage("Unsupported Charset");
        }

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
        private FormParser parser;
        private ByteBuffer buffer = Reactor.current().allocator.allocate();
        private ParseTask(Exchange exchange, Message msg, FormParser parser){
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
                buffer.clear();
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
                exchange.resume(thr);
            }
        }
    }
}
