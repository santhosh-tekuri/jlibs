/**
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

package jlibs.examples.core.nio;

import jlibs.core.lang.ByteSequence;
import jlibs.core.nio.ClientChannel;
import jlibs.core.nio.channels.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * @author Santhosh Kumar T
 */
public class HttpMessageReader implements InputHandler {
    public static final BytePattern PATTERN_HEADERS_END = new BytePattern(new byte[]{'\r', '\n', '\r', '\n'});
    public static final BytePattern PATTERN_LINE_END = new BytePattern(new byte[]{'\r', '\n'});

    public HttpMessageReader(ClientChannel client, HTTPMessageListener listener){
        InputChannel input = new PatternInputChannel(new ClientInputChannel(client), PATTERN_HEADERS_END);
        input.setHandler(this);
        this.listener = listener;
        listener.onStart(input);
    }

    ByteBuffer buffer = ByteBuffer.allocate(1024);
    HTTPMessageListener listener;

    @Override
    public void onRead(InputChannel input){
        if(buffer==null){
            listener.onBody(input);
            return;
        }
        try{
            while((input.read(buffer))>0){
                if(!buffer.hasRemaining())
                    buffer = ByteBuffer.wrap(Arrays.copyOf(buffer.array(), buffer.capacity() + 100), buffer.capacity(), 100);
            }
            if(input.isEOF()){
                input = ((FilterInputChannel)input).unwrap();
                byte array[] = buffer.array();
                int pos = 0;
                BytePattern.Matcher matcher = PATTERN_LINE_END.new Matcher();
                while(!matcher.matches(array[pos++]));
                listener.onLine(new ByteSequence(array, 0, pos), input);
                if(!input.client().isOpen() || !input.isOpen())
                    return;
                listener.onHeaders(new ByteSequence(array, pos, buffer.position()-pos), input);
                buffer = null;
            }else
                input.addInterest();
        }catch(IOException ex){
            listener.onIOException(input, ex);
        }
    }

    @Override
    public void onTimeout(InputChannel input){
        listener.onTimeout(input);
    }
}

class HTTPMessageListener{
    public void onStart(InputChannel input){
        try{
            input.addInterest();
        }catch(IOException ex){
            onIOException(input, ex);
        }
    }

    public void onLine(ByteSequence seq, InputChannel input){
        System.out.println("Line: "+seq.slice(0, seq.length()-2));
    }

    public void onHeaders(ByteSequence seq, InputChannel input){
        DefaultHeadersInfo info = new DefaultHeadersInfo();
        HttpHeaderIterator iter = new HttpHeaderIterator(seq);
        seq = null;
        while(iter.hasNext()){
            String[] strings = iter.next();
            System.out.println("Header: "+strings[0]+" = "+strings[1]);
            info.addHeader(strings[0], strings[1]);
        }
        System.out.println("-------------------------------------------------------------");

        if(info.isChunked())
            input = new ChunkedInputChannel(input);
        else
            input = new FixedLengthInputChannel(input, info.getContentLength());

        if(info.isDeflate())
            input = new InflaterInputChannel(input);
        else if(info.isGZip())
            input = new GZIPInputChannel(input);

        body = ByteBuffer.allocate(1024);
        try{
            input.addInterest();
        }catch(IOException ex){
            onIOException(input, ex);
        }
    }

    ByteBuffer body;
    public void onBody(InputChannel input){
        try{
            while(true){
                int read = input.read(body);
                if(read==-1){
                    System.out.println("\n-------------------------------------------------------------");
                    body = null;
                    return;
                }
                if(read>0){
                    System.out.write(body.array(), 0, body.position());
                    body.clear();
                }else{
                    input.addInterest();
                    break;
                }
            }
        }catch(IOException ex){
            onIOException(input, ex);
        }
    }

    public void onTimeout(InputChannel input){
        System.out.println("input timeout occurred");
    }

    public void onIOException(InputChannel input, IOException ex){
        ex.printStackTrace();
        try{
            input.client().close();
        }catch(IOException ignore){
            ignore.printStackTrace();
        }
    }
}

class HttpHeaderIterator implements Iterator<String[]> {
    public HttpHeaderIterator(ByteSequence headers){
        reset(headers);
    }

    private StringTokenizer stok;
    public void reset(ByteSequence headers){
        stok = new StringTokenizer(headers.slice(0, headers.length()-2).toString(), "\r\n");
    }

    @Override
    public boolean hasNext(){
        if(!stok.hasMoreTokens()){
            stok = null;
            array[0] = array[1] = null;
        }
        return stok!=null;
    }

    private String array[] = new String[2];

    @Override
    public String[] next(){
        if(stok==null)
            throw new NoSuchElementException();
        String header = stok.nextToken();
        int colon = header.indexOf(':');
        array[0] = header.substring(0, colon);
        array[1] = header.substring(colon+1).trim();
        return array;
    }

    @Override
    public void remove(){
        throw new UnsupportedOperationException();
    }
}

interface HeadersInfo{
    public boolean isChunked();
    public boolean isDeflate();
    public boolean isGZip();
    public boolean hasContentLength();
    public long getContentLength();
}

class DefaultHeadersInfo implements HeadersInfo{
    private boolean chunked;
    private boolean deflate;
    private boolean gzip;
    private boolean hasContentLength;
    private long contentLength;

    public void addHeader(String name, String value){
        if("Content-Encoding".equalsIgnoreCase(name) || "Transfer-Encoding".equalsIgnoreCase(name)){
            StringTokenizer stok = new StringTokenizer(value, ",");
            while(stok.hasMoreTokens()){
                String token = stok.nextToken().trim();
                if("chunked".equals(token))
                    chunked = true;
                else if("deflate".equals(token))
                    deflate = true;
                else if("gzip".equals(token))
                    gzip = true;
            }
        }else if("Content-Length".equalsIgnoreCase(name)){
            hasContentLength = true;
            contentLength = Long.parseLong(value);
        }
    }

    @Override
    public boolean isChunked(){
        return chunked;
    }

    @Override
    public boolean isDeflate(){
        return deflate;
    }

    @Override
    public boolean isGZip(){
        return gzip;
    }

    @Override
    public boolean hasContentLength(){
        return hasContentLength;
    }

    @Override
    public long getContentLength(){
        return contentLength;
    }
}