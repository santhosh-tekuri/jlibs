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
import jlibs.core.nio.NIOChannel;
import jlibs.core.nio.handlers.ClientHandler;
import jlibs.core.nio.handlers.Operation;

/**
 * @author Santhosh Kumar T
 */
public class IOChannelHandler implements ClientHandler{
    @Override
    public void onConnect(ClientChannel client){}

    protected InputChannel input;
    protected OutputChannel output;

    @Override
    public void onIO(ClientChannel client) throws Exception{
        try{
            if(client.isReadable() && input!=null)
                input.handler.onRead(input);
        }catch(Throwable error){
            if(input!=null)
                input.handler.onError(input, error);
        }
        try{
            if(client.isWritable() && output!=null)
                output.onWrite();
        }catch(Throwable error){
            if(output!=null)
                output.handler.onError(output, error);
        }
    }

    @Override
    public void onTimeout(ClientChannel client) throws Exception{
        try{
            if(client.isReadable() && input!=null)
                input.handler.onTimeout(input);
        }catch(Throwable error){
            if(input!=null)
                input.handler.onError(input, error);
        }
        try{
            if(client.isWritable() && output!=null)
                output.handler.onTimeout(output);
        }catch(Throwable error){
            if(output!=null)
                output.handler.onError(output, error);
        }
    }

    @Override
    public void onThrowable(NIOChannel channel, Operation operation, Throwable error) throws Exception{
        channel.close();
    }
}
