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

import jlibs.core.io.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class Buffers extends OutputStream{
    public ByteBuffer array[];
    public int offset;
    public int length;

    public Buffers(ByteBuffer array[], int offset, int length){
        this.array = array;
        this.offset = offset;
        this.length = length;
    }

    public Buffers(){
        this(new ByteBuffer[10], 0, 0);
    }

    public void append(ByteBuffer buffer){
        if(offset+length>=array.length){
            if(offset!=0)
                System.arraycopy(array, offset, array, 0, length-1);
            else
                array = Arrays.copyOf(array, 2*array.length);
            offset = 0;
        }
        array[offset+length] = buffer;
        ++length;
    }

    public void append(Buffers buffers){
        if(offset+length+buffers.length>=array.length){
            array = Arrays.copyOf(array, length+buffers.length);
            offset = 0;
        }
        System.arraycopy(buffers.array, buffers.offset, array, length, buffers.length);
        length += buffers.length;
    }

    public ByteBuffer remove(){
        if(length==0)
            throw new NoSuchElementException();
        ByteBuffer buffer = array[offset];
        array[offset] = null;
        --length;
        if(length==0)
            offset = 0;
        else
            ++offset;
        return buffer;
    }

    public ByteBuffer peek(){
        if(length==0)
            throw new NoSuchElementException();
        return array[offset];
    }

    public ByteBuffer removeLast(){
        if(length==0)
            throw new NoSuchElementException();
        --length;
        ByteBuffer buffer = array[offset+length];
        array[offset+length] = null;
        return buffer;
    }

    public ByteBuffer peekLast(){
        if(length==0)
            throw new NoSuchElementException();
        return array[offset+length-1];
    }

    public void removeEmpty(BufferAllocator allocator){
        while(length>0){
            if(array[offset].hasRemaining())
                break;
            else{
                allocator.free(array[offset]);
                ++offset;
                --length;
            }
        }
    }

    public long remaining(){
        long remaining = 0;
        for(int i=0; i<length; i++)
            remaining += array[offset+i].remaining();
        return remaining;
    }

    public boolean hasRemaining(){
        for(int i=0; i<length; i++){
            if(array[i].hasRemaining())
                return true;
        }
        return false;
    }

    public Buffers copy(){
        ByteBuffer array[] = new ByteBuffer[length];
        for(int i=0; i<array.length; i++)
            array[i] = this.array[offset+i].duplicate();
        return new Buffers(array, 0, array.length);
    }

    @Override
    public void write(int b){
        write((byte)b);
    }

    @Override
    public void write(byte[] bytes){
        write(bytes, 0, bytes.length);
    }

    @Override
    public void write(byte[] bytes, int offset, int length){
        if(length>0){
            ByteBuffer buffer = this.length==0 ? null : array[this.offset+this.length-1];
            do{
                if(buffer==null || buffer.limit()==buffer.capacity())
                    append(buffer=BufferAllocator.current().allocate());
                else
                    NIOUtil.compact(buffer);
                int min = Math.min(length, buffer.remaining());
                buffer.put(bytes, offset, min);
                buffer.flip();
                offset += min;
                length -= min;
                buffer = null;
            }while(length>0);
        }
    }

    public void write(byte b){
        ByteBuffer buffer = this.length==0 ? null : array[this.offset+this.length-1];
        if(buffer==null || buffer.limit()==buffer.capacity())
            append(buffer=BufferAllocator.current().allocate());
        else
            NIOUtil.compact(buffer);
        buffer.put(b);
        buffer.flip();
    }

    public void write(ByteBuffer src){
        ByteBuffer buffer = this.length==0 ? null : array[this.offset+this.length-1];
        while(src.hasRemaining()){
            if(buffer==null || buffer.limit()==buffer.capacity())
                append(buffer=BufferAllocator.current().allocate());
            else
                NIOUtil.compact(buffer);
            int min = Math.min(src.remaining(), buffer.remaining());
            int srcLimit = src.limit();
            src.limit(src.position()+min);
            buffer.put(src);
            src.limit(srcLimit);
            buffer.flip();
            buffer = null;
        }
    }

    public void write(String ascii){
        int len = ascii.length();
        int i = 0;

        ByteBuffer buffer = this.length==0 ? null : array[this.offset+this.length-1];
        while(i<len){
            if(buffer==null || buffer.limit()==buffer.capacity())
                append(buffer=BufferAllocator.current().allocate());
            else
                NIOUtil.compact(buffer);
            while(i<len && buffer.hasRemaining())
                buffer.put((byte)ascii.charAt(i++));
            buffer.flip();
            buffer = null;
        }
    }

    public void writeTo(OutputStream out) throws IOException{
        ByteBuffer temp = null;
        try{
            for(int i=0; i<length; i++){
                ByteBuffer buffer = array[offset+i];
                if(buffer.hasRemaining()){
                    if(buffer.hasArray())
                        out.write(buffer.array(), buffer.arrayOffset()+buffer.position(), buffer.remaining());
                    else{
                        if(temp==null)
                            temp = BufferAllocator.current().allocateHeap();
                        int bufferPos = buffer.position();
                        int bufferLimit = buffer.limit();
                        try{
                            while(buffer.hasRemaining()){
                                temp.clear();
                                int min = Math.min(buffer.remaining(), temp.remaining());
                                buffer.limit(buffer.position()+min);
                                temp.put(buffer);
                                buffer.limit(bufferLimit);
                                temp.flip();
                                out.write(temp.array(), 0, temp.remaining());
                            }
                        }finally{
                            buffer.position(bufferPos);
                            buffer.limit(bufferLimit);
                        }
                    }
                }
            }
        }finally{
            if(temp!=null)
                BufferAllocator.current().free(temp);
        }
    }

    public class Input extends InputStream{
        int i = 0;
        ByteBuffer buffer = length==0 ? null : array[offset].duplicate();

        @Override
        public int read() throws IOException{
            byte b[] = new byte[1];
            int len = read(b, 0, 1);
            return len==1 ? (b[0]&0xff) : -1;
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException{
            if(buffer==null)
                return -1;

            int _length = length;
            while(buffer!=null && length>0){
                int min = Math.min(length, buffer.remaining());
                buffer.get(bytes, offset, min);
                offset += min;
                length -= min;
                if(buffer.hasRemaining())
                    break;
                else{
                    ++i;
                    if(i==Buffers.this.length)
                        buffer = null;
                    else
                        buffer = array[offset+i].duplicate();
                }
            }
            return length==_length && buffer==null ? -1 : _length-length;
        }

        @Override
        public int available() throws IOException{
            if(buffer==null)
                return 0;
            int available = buffer.remaining();
            for(int j=i+1; j<length; j++)
                available += array[offset+j].remaining();
            return available;
        }
    }

    public int read(ByteBuffer dst, BufferAllocator allocator){
        int read = 0;
        while(length>0){
            ByteBuffer src = array[offset];
            read += NIOUtil.copy(src, dst);
            if(!src.hasRemaining())
                allocator.free(remove());
            if(!dst.hasRemaining())
                break;
        }
        return read==0 && length==0 ? -1 : 0;
    }

    public String toString(Charset charset) throws IOException{
        BytesDecoder decoder = new BytesDecoder(new StringBuilder(), charset.newDecoder(), 1024);
        ByteBuffer temp = null;
        for(int i=0; i<length; i++){
            ByteBuffer buffer = array[offset+i].duplicate();
            if(temp!=null && temp.hasRemaining()){
                do{
                    temp.compact();
                    int min = Math.min(temp.remaining(), buffer.remaining());
                    int bufferLimit = buffer.limit();
                    buffer.limit(buffer.position()+min);
                    temp.put(buffer);
                    buffer.limit(bufferLimit);
                    temp.flip();

                    decoder.write(temp, i==length-1 && !buffer.hasRemaining());
                }while(buffer.hasRemaining());
            }else{
                decoder.write(buffer, i==length-1);
                if(buffer.hasRemaining()){
                    if(temp==null)
                        temp = BufferAllocator.current().allocate();
                    temp.put(buffer);
                    temp.flip();
                }
            }
        }
        return decoder.appendable.toString();
    }

    public String toString(){
        try{
            return toString(IOUtil.ISO_8859_1);
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }
    }
}
