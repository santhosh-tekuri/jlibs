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
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Santhosh Kumar T
 */
public class Bytes implements Iterable<ByteSequence>{
    public static final int CHUNK_SIZE = 1024*4; // 4K

    public Bytes(int chunkSize){
        this.chunkSize = chunkSize;
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

    private ByteBuffer buff;
    private int chunkSize;
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
}
