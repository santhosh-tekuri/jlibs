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

import jlibs.core.nio.ClientChannel;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar T
 */
public interface NIOSupport{
    public void addInterest() throws IOException;
    public void removeInterest() throws IOException;
    public void attach(Object obj);
    public Object attachment();
    public int process(ByteBuffer buffer) throws IOException;
    public ClientChannel client();
}

class DefaultNIOSupport implements NIOSupport{
    private ClientChannel client;
    private int operation;

    DefaultNIOSupport(ClientChannel client, int operation){
        this.client = client;
        this.operation = operation;
    }

    @Override
    public void addInterest() throws IOException{
        client.addInterest(operation);
    }

    @Override
    public void removeInterest() throws IOException{
        client.removeInterest(operation);
    }

    @Override
    public int process(ByteBuffer buffer) throws IOException{
        return operation==ClientChannel.OP_READ ? client.read(buffer) : client.write(buffer);
    }

    @Override
    public void attach(Object obj){
        client.attach(obj);
    }

    @Override
    public Object attachment(){
        return client.attachment();
    }

    @Override
    public ClientChannel client(){
        return client;
    }
}
