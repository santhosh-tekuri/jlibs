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
public class ClientOutputChannel extends OutputChannel{
    public ClientOutputChannel(ClientChannel client){
        super(client);
    }

    @Override
    protected boolean activateInterest(){
        return true;
    }

    @Override
    public int write(ByteBuffer dst) throws IOException{
        int wrote = client.write(dst);
        notifyCompleted(Status.COMPLETED, Status.COMPLETED);
        return wrote;
    }

    @Override
    protected void writePending() throws IOException{}

    @Override
    public Status status(){
        return Status.COMPLETED;
    }
}
