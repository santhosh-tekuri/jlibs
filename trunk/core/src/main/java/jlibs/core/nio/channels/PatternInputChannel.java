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

package jlibs.core.nio.channels;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar T
 */
public class PatternInputChannel extends FilterInputChannel{
    private BytePattern.Matcher matcher;

    public PatternInputChannel(InputChannel delegate, BytePattern pattern){
        super(delegate);
        matcher = pattern.new Matcher();
    }

    @Override
    protected boolean activateInterest(){
        return super.activateInterest() && matcher!=null;
    }

    @Override
    protected int doRead(ByteBuffer dst) throws IOException{
        if(matcher==null)
            return -1;
        int pos = dst.position();

        if(delegate.read(dst)==-1)
            throw new EOFException();

        int iLast = dst.position();
        for(int i=pos; i<iLast; i++){
            if(matcher.matches(dst.get(i))){
                i++;
                delegate.unread(dst.array(), dst.arrayOffset()+i, dst.position()-i, true);
                dst.position(i);
                matcher = null;
                break;
            }
        }
        int read = dst.position()-pos;
        return read==0 && matcher==null ? -1 : read;
    }
}
