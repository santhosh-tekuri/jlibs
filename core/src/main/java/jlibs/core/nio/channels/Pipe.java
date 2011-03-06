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

import java.nio.ByteBuffer;
import java.nio.channels.Channel;

/**
 * @author Santhosh Kumar T
 */
public class Pipe{
    private InputChannel input;
    private OutputChannel output;

    public Pipe(InputChannel input, OutputChannel output){
        this.input = input;
        this.output = output;
    }

    public InputChannel input(){
        return input;
    }

    public OutputChannel output(){
        return output;
    }

    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    public void start(){
        IOHandler ioHandler = new IOHandler();
        input.setHandler(ioHandler);
        output.setHandler(ioHandler);
        try{
            input.addInterest();
        }catch(Throwable thr){
            try{
                handler.onError(this, input, thr);
            }catch(Throwable error){
                error.printStackTrace();
            }
        }
    }

    private class IOHandler implements InputHandler, OutputHandler{
        @Override
        public void onRead(InputChannel input) throws Exception{
            buffer.clear();
            int read = input.read(buffer);
            if(read==-1)
                handler.finished(Pipe.this);
            else if(read==0)
                input.addInterest();
            else if(read>0){
                buffer.flip();
                output.addWriteInterest();
            }
        }

        @Override
        public void onTimeout(InputChannel input) throws Exception{
            handler.onTimeout(Pipe.this, input);
        }

        @Override
        public void onError(InputChannel input, Throwable error) throws Exception{
            handler.onError(Pipe.this, input, error);
        }

        @Override
        public void onWrite(OutputChannel output) throws Exception{
            output.write(buffer);
            if(output.status()==OutputChannel.Status.NEEDS_OUTPUT)
                output.addStatusInterest();
            else
                input.addInterest();
        }

        @Override
        public void onTimeout(OutputChannel output) throws Exception{
            handler.onTimeout(Pipe.this, output);
        }

        @Override
        public void onError(OutputChannel output, Throwable error) throws Exception{
            handler.onError(Pipe.this, output, error);
        }

        @Override
        public void onStatus(OutputChannel output) throws Exception{
            input.addInterest();
        }
    }

    private Handler handler;
    public void setHandler(Handler handler){
        this.handler = handler;
    }

    private interface Handler{
        public void onTimeout(Pipe pipe, Channel channel) throws Exception;
        public void onError(Pipe pipe, Channel channel, Throwable error) throws Exception;
        public void finished(Pipe pipe) throws Exception;
    }
}
