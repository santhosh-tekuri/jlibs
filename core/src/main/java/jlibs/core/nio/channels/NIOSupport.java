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
import jlibs.core.nio.handlers.ClientHandler;

import java.io.IOException;

/**
 * @author Santhosh Kumar T
 */
public interface NIOSupport{
    public IOChannelHandler createHandler();
}

class IOChannelHandler{
    protected InputChannel input;
    protected OutputChannel output;
}

final class DefaultNIOSupport implements NIOSupport{
    static final DefaultNIOSupport INSTANCE = new DefaultNIOSupport();
    private DefaultNIOSupport(){}

    @Override
    public IOChannelHandler createHandler(){
        return new DefaultIOChannelHandler();
    }

    private static class DefaultIOChannelHandler extends IOChannelHandler implements ClientHandler{
        @Override
        public void onConnect(ClientChannel client){}

        @Override
        public void onConnectFailure(ClientChannel client, Exception ex){
            ex.printStackTrace();
            try{
                client.close();
            }catch(IOException ignore){
                ignore.printStackTrace();
            }
        }

        @Override
        public void onIO(ClientChannel client){
            if(client.isReadable() && input!=null)
                input.handler.onRead(input);
            try{
                if(client.isWritable() && output!=null)
                    output.onWrite();
            }catch(IOException ex){
                if(output!=null)
                    output.handler.onIOException(output, ex);
            }
        }

        @Override
        public void onTimeout(ClientChannel client){
            if(client.isReadable() && input!=null)
                input.handler.onTimeout(input);
            if(client.isWritable() && output!=null)
                output.handler.onTimeout(output);
        }
    }
}

