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

package jlibs.nio.util;

import jlibs.core.io.ByteArrayOutputStream2;
import jlibs.core.io.IOUtil;
import jlibs.nio.BufferPool;
import jlibs.nio.Reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Bytes implements Iterable<ByteBuffer>{
    public static final int CHUNK_SIZE = Integer.parseInt(System.getProperty("Bytes.chunkSize", String.valueOf(4*1024))) ; // 4K

    public final int chunkSize;
    public boolean canPool = true;
    private LinkedList<ByteBuffer> list = new LinkedList<>();

    public Bytes(){
        this(CHUNK_SIZE);
    }

    public Bytes(int chunkSize){
        this.chunkSize = chunkSize;
    }

    @Override
    public Iterator<ByteBuffer> iterator(){
        return list.iterator();
    }

    public long size(){
        long size = 0;
        for(ByteBuffer buffer: list)
            size += buffer.remaining();
        return size;
    }

    public boolean isEmpty(){
        if(list.isEmpty())
            return true;
        for(ByteBuffer buffer: list){
            if(buffer.hasRemaining())
                return false;
        }
        return true;
    }

    public ByteBuffer remove(){
        return list.removeFirst();
    }

    public void append(ByteBuffer buffer){
        list.addLast(buffer);
    }

    public void prepend(ByteBuffer buffer){
        list.addFirst(buffer);
    }

    public void clear(){
        list.clear();
    }

    public void pumpWithBackup(java.io.InputStream in, java.io.OutputStream out) throws IOException{
        BufferPool pool = null;
        Reactor reactor = Reactor.current();
        if(reactor!=null)
            pool = reactor.bufferPool;

        ByteBuffer buffer = pool==null ? ByteBuffer.allocate(chunkSize) : pool.borrow(chunkSize);
        try{
            int read;
            while((read=in.read(buffer.array(), buffer.position(), buffer.remaining()))!=-1){
                out.write(buffer.array(), buffer.position(), read);
                buffer.position(buffer.position()+read);
                if(!buffer.hasRemaining()){
                    buffer.flip();
                    append(buffer);
                    buffer = pool==null ? ByteBuffer.allocate(chunkSize) : pool.borrow(chunkSize);
                }
            }
        }finally{
            if(buffer.position()!=0){
                buffer.flip();
                append(buffer);
            }else if(pool!=null)
                pool.returnBack(buffer);
            in.close();
        }
    }

    /*-------------------------------------------------[ InputStream ]---------------------------------------------------*/

    public final class InputStream extends java.io.InputStream{
        private Iterator<ByteBuffer> iter;
        private ByteBuffer buffer;
        private int bufferPos;
        private long userRead = 0;

        public InputStream(){
            iter = iterator();
            if(iter.hasNext()){
                buffer = iter.next();
                bufferPos = buffer.position();
            }
        }

        @Override
        public int read() throws IOException{
            byte b[] = new byte[1];
            int len = read(b, 0, 1);
            return len==1 ? (b[0]&0xff) : -1;
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException{
            int read = 0;
            while(buffer!=null && length>0){
                int min = Math.min(length, buffer.limit()-bufferPos);
                System.arraycopy(buffer.array(), bufferPos, bytes, offset, min);
                read += min;
                userRead += min;
                offset += min;
                length -= min;
                if(bufferPos==buffer.limit()){
                    if(iter.hasNext()){
                        buffer = iter.next();
                        bufferPos = buffer.position();
                    }else
                        buffer = null;
                }

            }
            return read==0 && buffer==null ? -1 : read;
        }

        @Override
        public int available() throws IOException{
            return (int)(size()-userRead);
        }
    }

    /*-------------------------------------------------[ OutputStream ]---------------------------------------------------*/

    public final class OutputStream extends java.io.OutputStream{
        private BufferPool pool;
        private ByteBuffer buffer;

        public OutputStream(BufferPool pool){
            this.pool = pool;
        }

        public OutputStream(){}

        @Override
        public void write(int b) throws IOException{
            write(new byte[]{ (byte)b }, 0, 1);
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException{
            while(length>0){
                if(buffer==null)
                    buffer = pool==null ? ByteBuffer.allocate(chunkSize) : pool.borrow(chunkSize);
                int min = Math.min(length, buffer.remaining());
                buffer.put(bytes, offset, min);
                offset += min;
                length -= min;
                if(!buffer.hasRemaining()){
                    buffer.flip();
                    append(buffer);
                    buffer = null;
                }
            }
        }

        public void write(String str){
            final int len = str.length();
            for(int i=0; i<len; i++){
                if(buffer==null)
                    buffer = pool==null ? ByteBuffer.allocate(chunkSize) : pool.borrow(chunkSize);
                buffer.put((byte)str.charAt(i));
                if(!buffer.hasRemaining()){
                    buffer.flip();
                    append(buffer);
                    buffer = null;
                }
            }
        }

        @Override
        public void close() throws IOException{
            if(buffer!=null){
                buffer.flip();
                append(buffer);
                buffer = null;
            }
        }
    }

    /*-------------------------------------------------[ Encoder ]---------------------------------------------------*/

    public static interface Encodable{
        public Bytes encodeTo(Bytes bytes) throws IOException;
    }

    /*-------------------------------------------------[ Helpers ]---------------------------------------------------*/

    public ByteBuffer append(String str, ByteBuffer buffer){
        final int len = str.length();
        for(int i=0; i<len; i++){
            if(!buffer.hasRemaining()){
                buffer.flip();
                append(buffer);
                buffer = Reactor.current().bufferPool.borrow(chunkSize);
            }
            buffer.put((byte)str.charAt(i));
        }
        return buffer;
    }

    public String toString(Charset charset){
        if(isEmpty())
            return "";
        if(list.size()==1){
            ByteBuffer buffer = list.getFirst();
            return new String(buffer.array(), buffer.arrayOffset()+buffer.position(), buffer.remaining(), charset);
        }else{
            ByteArrayOutputStream2 bout = new ByteArrayOutputStream2();
            for(ByteBuffer buffer: this)
                bout.write(buffer.array(), buffer.arrayOffset()+buffer.position(), buffer.remaining());
            return bout.toByteSequence().toString(charset);
        }
    }

    public String toString(){
        return toString(IOUtil.UTF_8);
    }
}
