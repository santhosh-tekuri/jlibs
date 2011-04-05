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

import jlibs.core.lang.ByteSequence;
import jlibs.core.lang.Bytes;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.nio.ClientChannel;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar T
 */
public class ClientInputChannel extends InputChannel{
    public ClientInputChannel(ClientChannel client){
        this(new DefaultNIOSupport(client, ClientChannel.OP_READ));
    }

    public ClientInputChannel(NIOSupport nioSupport){
        super(nioSupport);
    }

    @Override
    protected boolean activateInterest(){
        return true;
    }

    @Override
    protected int doRead(ByteBuffer dst) throws IOException{
        return nioSupport.process(dst);
    }

    public boolean isBroken() throws IOException{
        if(isEOF())
            return true;
        ByteBuffer dst = ByteBuffer.allocate(1);
        switch(nioSupport.process(dst)){
            case -1:
                return true;
            case 1:
                if(unread==null)
                    unread = new Bytes();
                unread.append(new ByteSequence(dst.array(), 0, 1));
            case 0:
                return false;
            default:
                throw new ImpossibleException();
        }
    }
}
