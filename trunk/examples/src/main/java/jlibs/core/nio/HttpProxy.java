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

package jlibs.core.nio;

import jlibs.core.nio.channels.InputChannel;
import jlibs.core.nio.channels.OutputChannel;
import jlibs.core.nio.channels.OutputHandler;
import jlibs.core.nio.handlers.Operation;
import jlibs.core.nio.handlers.ServerHandler;

import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar T
 */
public class HttpProxy implements ServerHandler{
    @Override
    public void onAccept(ServerChannel channel, ClientChannel client) throws Exception{
        new HttpMessageReader(client);
    }

    @Override
    public void onThrowable(NIOChannel channel, Operation operation, Throwable error) throws Exception {
        error.printStackTrace();
        if(channel instanceof ClientChannel)
            channel.close();
    }
}

class HttpMessageWriter implements OutputHandler{
    InputChannel input;
    ByteBuffer buffer;

    @Override
    public void onWrite(OutputChannel output) throws Exception{
        if(buffer==null)
            output.close();
        else
            output.write(buffer);

        buffer = null;

        if(output.status()==OutputChannel.Status.NEEDS_OUTPUT)
            output.addStatusInterest();
        else
            input.addInterest();
    }

    @Override
    public void onTimeout(OutputChannel output) throws Exception{
        System.out.println("output timeout occurred");
    }

    @Override
    public void onError(OutputChannel output, Throwable error) throws Exception{
        output.client().close();
    }

    @Override
    public void onStatus(OutputChannel output) throws Exception{
        input.addInterest();
    }
}