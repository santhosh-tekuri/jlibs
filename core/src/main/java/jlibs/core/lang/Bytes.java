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

package jlibs.core.lang;

import jlibs.core.io.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Santhosh Kumar T
 */
public class Bytes implements Iterable<ByteSequence>, Serializable{
    public static final int CHUNK_SIZE = 1024*4; // 4K

    public Bytes(int chunkSize){
        if(chunkSize<=0)
            throw new IllegalArgumentException("chunkSize: "+chunkSize);
        this.chunkSize = chunkSize;
    }

    public Bytes(Bytes that, boolean deepCopy){
        for (ByteSequence seq: that){
            if(deepCopy)
                seq = seq.copy();
            append(seq);
        }
    }

    public Bytes(){
        this(CHUNK_SIZE);
    }

    private LinkedList<ByteSequence> list = new LinkedList<ByteSequence>();

    public int size(){
        int size = 0;
        for(ByteSequence seq: list)
            size += seq.length();
        return size;
    }

    public boolean isEmpty(){
        if(list.size()==0)
            return true;
        for(ByteSequence seq: list){
            if(seq.length()>0)
                return false;
        }
        return true;
    }

    public void clear(){
        list.clear();
        buff = null;
    }

    public void prepend(ByteSequence seq){
        list.add(0, seq);
    }

    public void append(ByteSequence seq){
        list.add(seq);
    }

    @Override
    public Iterator<ByteSequence> iterator(){
        return list.iterator();
    }

    public void remove(int count){
        Iterator<ByteSequence> iter = iterator();
        while(iter.hasNext()){
            ByteSequence seq = iter.next();
            count -= seq.length();
            iter.remove();
            if(count==0)
                return;
            else if(count<0){
                list.add(0, seq.slice(seq.length()+count));
                return;
            }
        }
    }

    public int getBytes(int offset, byte dest[], int destOffset, int destLength){
        ByteSequence seq = null;
        Iterator<ByteSequence> iter = iterator();
        while(iter.hasNext()){
            seq = iter.next();
            offset -= seq.length();
            if(offset==0){
                seq = null;
                break;
            }else if(offset<0){
                seq = seq.slice(seq.length()+offset);
                break;
            }
        }
        int filled = 0;
        do{
            if(seq!=null){
                int min = Math.min(destLength-filled, seq.length());
                System.arraycopy(seq.buffer(), seq.offset(), dest, destOffset+filled, min);
                filled += min;
            }
            if(iter.hasNext())
                seq = iter.next();
            else
                break;
        }while(filled<destLength);

        return filled;
    }
    private transient ByteBuffer buff;
    private int chunkSize;

    public ByteSequence readChunk(ReadableByteChannel channel) throws IOException{
        if(buff==null)
            buff = ByteBuffer.allocate(chunkSize);
        int read = channel.read(buff);
        if(read<=0){
            if(read<0 && buff.position()==0) // garbage buff
                buff = null;
            return null;
        }
        ByteSequence seq = new ByteSequence(buff.array(), buff.position() - read, read);
        append(seq);
        if(!buff.hasRemaining())
            buff = null;
        return seq;
    }

    public int readFrom(ReadableByteChannel channel) throws IOException{
        int total = 0;
        while(true){
            if(buff==null)
                buff = ByteBuffer.allocate(chunkSize);
            int read = channel.read(buff);
            if(read<=0){
                if(read<0 && buff.position()==0) // garbage buff
                    buff = null;
                break;
            }
            total += read;
            append(new ByteSequence(buff.array(), buff.position() - read, read));
            if(buff.hasRemaining())
                break;
            else
                buff = null;
        }
        return total;
    }

    public int readFully(InputStream in) throws IOException{
        int total = 0;
        while(true){
            if(buff==null)
                buff = ByteBuffer.allocate(chunkSize);
            int read = IOUtil.readFully(in, buff.array(), buff.position(), buff.limit());
            if(read==0){
                if(buff.position()==0) // garbage buff
                    buff = null;
                break;
            }
            buff.position(buff.position()+read);
            total += read;
            append(new ByteSequence(buff.array(), buff.position() - read, read));
            if(buff.hasRemaining()) // eof reached
                break;
            else
                buff = null;
        }
        return total;
    }

    public int writeTo(WritableByteChannel channel) throws IOException{
        int total = 0;
        Iterator<ByteSequence> iter = iterator();
        while(iter.hasNext()){
            ByteSequence seq = iter.next();
            int wrote = channel.write(seq.toByteBuffer());
            if(wrote==0)
                break;
            total += wrote;
            iter.remove();
            if(wrote<seq.length()){
                list.add(0, seq.slice(wrote));
                break;
            }
        }
        return total;
    }

    public InputStream createInputStream(){
        return new InputStream(){
            int contentOffset;

            @Override
            public int read() throws IOException{
                byte b[] = new byte[1];
                int len = read(b, 0, 1);
                return len==1 ? (b[0]&0xff) : -1;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException{
                int read = getBytes(contentOffset, b, off, len);
                if(read==0)
                    return -1;
                contentOffset += read;
                return read;
            }

            @Override
            public int available() throws IOException{
                return size()-contentOffset;
            }
        };
    }

    public OutputStream createOutputStream(){
        return new OutputStream(){
            @Override
            public void write(int b) throws IOException{
                write(new byte[]{ (byte)b }, 0, 1);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException{
                byte array[] = new byte[len];
                System.arraycopy(b, off, array, 0, len);
                append(new ByteSequence(array));
            }

            @Override
            public void close(){}
        };
    }

    @Override
    public String toString(){
        StringBuilder buff = new StringBuilder();
        for(ByteSequence seq: this)
            buff.append(seq);
        return buff.toString();
    }
}
