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

package jlibs.nio.channels.filters;

import jlibs.core.io.IOUtil;
import jlibs.nio.Reactor;
import jlibs.nio.channels.impl.filters.AbstractInputFilterChannel;
import jlibs.nio.util.BytePattern;
import jlibs.nio.util.Bytes;
import jlibs.nio.util.NIOUtil;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class BoundaryInputFilter extends AbstractInputFilterChannel{
    public final BytePattern pattern;
    private ByteBuffer buffer;
    private int readPos, patternPos;
    private int j;
    private boolean preamble;

    public BoundaryInputFilter(BytePattern pattern, boolean preamble){
        this.pattern = pattern;
        buffer = Reactor.current().bufferPool.borrow(Bytes.CHUNK_SIZE);
        if(preamble){
            this.preamble = true;
            buffer.put((byte)'\r');
            buffer.put((byte)'\n');
            patternPos = j = 2;
        }
    }

    @Override
    protected int _read(ByteBuffer dst) throws IOException{
        if(j==-1)
            return -1;

        int dstPos = dst.position();

        if(j==0 || j==pattern.length()){
            copy2Dst(dst);
            if(readPos==patternPos)
                j = 0;
            else
                return dst.position()-dstPos;
        }

        if(patternPos==buffer.position()){
            if(!buffer.hasRemaining()){
                buffer.position(readPos);
                buffer.limit(patternPos);
                NIOUtil.compact(buffer);
                readPos = 0;
                patternPos = buffer.position();
            }
            int read = peerInput.read(buffer);
            if(read==0)
                return 0;
            else if(read==-1)
                throw new EOFException();
        }

        while(true){
            if(j!=pattern.length()){
                while(patternPos<buffer.position()){
                    j = pattern.match(j, buffer.get(patternPos++));
                    if(j==0){
                        copy2Dst(dst);
                        if(readPos!=patternPos){
                            assert !dst.hasRemaining();
                            return dst.position()-dstPos;
                        }
                    }else if(j==pattern.length())
                        break;
                }
            }
            if(j==pattern.length()){
                assert patternPos==readPos+pattern.length();
                if(patternPos+1<buffer.position()){
                    if(buffer.get(patternPos)=='\r' && buffer.get(patternPos+1)=='\n'){
                        readPos = patternPos+2;
                        j = -1;
                        unread();
                        break;
                    }else if(buffer.get(patternPos)=='-' && buffer.get(patternPos+1)=='-'){
                        if(patternPos+3<buffer.position()){
                            if(buffer.get(patternPos+2)=='\r' && buffer.get(patternPos+3)=='\n'){
                                last = true;
                                readPos = patternPos+4;
                                j = -1;
                                unread();
                                break;
                            }
                        }else
                            break;
                    }
                    copy2Dst(dst);
                    if(readPos==patternPos)
                        j = 0;
                    else
                        break;
                }else
                    break;
            }else
                break;
        }

        return dst.position()==dstPos && j==-1 ? -1 : dst.position()-dstPos;
    }

    private void copy2Dst(ByteBuffer dst){
        if(preamble){
            buffer.get(readPos++);
            buffer.get(readPos++);
            preamble = false;
        }
        while(readPos<patternPos && dst.hasRemaining())
            dst.put(buffer.get(readPos++));
    }

    private void unread(){
        if(readPos==buffer.position())
            Reactor.current().bufferPool.returnBack(buffer);
        else{
            buffer.limit(buffer.position());
            buffer.position(readPos);
            peerInput.unread(buffer);
        }
    }

    @Override
    protected boolean isReadReady(){
        return j==-1 || ((j==0 || j==pattern.length()) && readPos<patternPos);
    }

    private boolean last;
    public boolean isLast(){
        return last;
    }

    public static BytePattern createPattern(String boundary){
        byte[] b = boundary.getBytes(IOUtil.UTF_8);
        byte b1[] = new byte[b.length+4];
        b1[0] = '\r';
        b1[1] = '\n';
        b1[2] = '-';
        b1[3] = '-';
        System.arraycopy(b, 0, b1, 4, b.length);
        return new BytePattern(b1);
    }
}
