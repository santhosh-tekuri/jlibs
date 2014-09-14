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

import jlibs.nbp.Feeder;
import jlibs.nio.Input;
import jlibs.nio.Reactor;
import jlibs.nio.http.Exchange;
import jlibs.nio.http.SocketPayload;
import jlibs.nio.http.msg.Message;
import jlibs.nio.http.util.MediaType;
import jlibs.nio.listeners.IOListener;
import jlibs.nio.listeners.Task;
import jlibs.nio.util.BufferAllocator;
import jlibs.nio.util.Buffers;
import jlibs.nio.util.UnpooledBufferAllocator;
import jlibs.xml.sax.async.AsyncXMLReader;
import jlibs.xml.sax.async.ChannelInputSource;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import static java.nio.channels.SelectionKey.OP_READ;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ParseXML extends ParseSocketPayload{
    @Override
    protected boolean isCompatible(MediaType mt){
        return mt.isXML();
    }

    @Override
    protected boolean parse(Exchange exchange, Message msg, SocketPayload payload, MediaType mt) throws Exception{
        String charset = mt.getCharset(null);

        InputSource is;
        Input socket = payload.socket();
        if(socket.isOpen()){
            ReadableByteChannel channel = socket;
            boolean retain = retain(payload);
            if(retain){
                if(payload.buffers==null)
                    payload.buffers = new Buffers();
            }
            if(payload.buffers!=null)
                channel = new Reader(payload.buffers, retain, channel);
            is = new ChannelInputSource(channel);
        }else
            is = new InputSource(payload.buffers.new Input()); // todo optimize

        is.setEncoding(charset);
        new IOListener().start(new XMLFeedTask(exchange, msg, is), socket, null);
        return false;
    }

    protected boolean retain(SocketPayload payload){
        return payload.retain;
    }

    protected void addHandlers(AsyncXMLReader xmlReader) throws Exception{}

    protected void parsingCompleted(Exchange exchange, Message msg, AsyncXMLReader xmlReader){
        exchange.resume();
    }

    private class XMLFeedTask extends Task{
        private Exchange exchange;
        private Message msg;
        private AsyncXMLReader xmlReader;
        private Feeder feeder;
        private XMLFeedTask(Exchange exchange, Message msg, InputSource is) throws Exception{
            super(OP_READ);
            this.exchange = exchange;
            this.msg = msg;
            xmlReader = new AsyncXMLReader();
            addHandlers(xmlReader);
            feeder = xmlReader.createFeeder(is);
        }

        @Override
        protected boolean process(int readyOp) throws IOException{
            feeder = feeder.feed();
            if(feeder==null)
                return true;
            else{
                in.addReadInterest();
                return false;
            }
        }

        @Override
        protected void cleanup(Throwable thr){
            if(thr==null)
                parsingCompleted(exchange, msg, xmlReader);
            else
                exchange.resume(thr);
        }
    }

    private static class Reader implements ReadableByteChannel{
        private Buffers buffers;
        private BufferAllocator allocator;
        private ReadableByteChannel channel;
        private Buffers backup;
        private Reader(Buffers buffers, boolean retain, ReadableByteChannel channel){
            this.channel = channel;

            if(buffers.hasRemaining()){
                if(retain){
                    this.buffers = buffers.copy();
                    allocator = UnpooledBufferAllocator.HEAP;
                }else{
                    this.buffers = buffers;
                    allocator = Reactor.current().allocator;
                }
            }

            if(retain)
                backup = buffers;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException{
            if(buffers!=null){
                int read = buffers.read(dst, allocator);
                if(buffers.length==0)
                    buffers = null;
                return read;
            }

            int dstPos = dst.position();
            int read = channel.read(dst);
            if(read>0 && backup!=null){
                int dstLimit = dst.limit();
                dst.position(dstPos);
                dst.limit(dstPos+read);
                backup.write(dst);
                dst.limit(dstLimit);
            }
            return read;
        }

        @Override
        public boolean isOpen(){
            return channel.isOpen();
        }

        @Override
        public void close() throws IOException{
            channel.close();
        }
    }
}

