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

import jlibs.nio.channels.impl.filters.AbstractInputFilterChannel;
import jlibs.nio.util.BytePattern;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class PatternInputFilter extends AbstractInputFilterChannel{
    private BytePattern pattern;
    private int patternJ = 0;
    public PatternInputFilter(BytePattern pattern){
        this.pattern = pattern;
    }

    @Override
    protected int _read(ByteBuffer dst) throws IOException{
        if(pattern==null)
            return -1;
        int pos = dst.position();

        if(peerInput.read(dst)==-1)
            throw new EOFException("Unexpected EOF before pattern '"+pattern);

        int newPos = dst.position();
        if(newPos==0) // nothing read
            return 0;

        dst.position(pos);
        for(int i=pos; i<newPos; i++){
            patternJ = pattern.match(patternJ, dst.get());
            if(patternJ==pattern.length()){
                if(dst.position()!=newPos){
                    byte unread[] = new byte[newPos-dst.position()];
                    dst.get(unread);
                    dst.position(i+1);
                    peerInput.unread(ByteBuffer.wrap(unread));
                }
                pattern = null;
                break;
            }
        }
        int read = dst.position()-pos;
        assert read>0;
        return read;
    }

    @Override
    protected boolean isReadReady(){
        return pattern==null;
    }
}
