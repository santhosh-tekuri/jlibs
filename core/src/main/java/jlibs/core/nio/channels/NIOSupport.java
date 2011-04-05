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
import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar T
 */
public interface NIOSupport{
    public void addInterest() throws IOException;
    public void removeInterest() throws IOException;

    public void attachHandler();
    public void setInput(InputChannel input);
    public void setOutput(OutputChannel output);
    public InputChannel getInput();
    public OutputChannel getOutput();

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
    public void attachHandler(){
        if(client().attachment() instanceof IOChannelHandler)
            return;
        client.attach(new IOChannelHandler());
    }

    @Override
    public void setInput(InputChannel input){
        ((IOChannelHandler)client.attachment()).input = input;
    }

    @Override
    public void setOutput(OutputChannel output){
        ((IOChannelHandler)client.attachment()).output = output;
    }

    @Override
    public InputChannel getInput(){
        return ((IOChannelHandler)client.attachment()).input;
    }

    @Override
    public OutputChannel getOutput(){
        return ((IOChannelHandler)client.attachment()).output;
    }

    @Override
    public ClientChannel client(){
        return client;
    }
}

class IOChannelHandler implements ClientHandler{
    @Override
    public void onConnect(ClientChannel client){}

    @Override
    public void onConnectFailure(ClientChannel client, IOException ex){
        ex.printStackTrace();
        try{
            client.close();
        }catch(IOException ignore){
            ignore.printStackTrace();
        }
    }

    protected InputChannel input;
    protected OutputChannel output;

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
