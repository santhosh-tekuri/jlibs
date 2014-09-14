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

package jlibs.nio.listeners;

import jlibs.nio.*;
import jlibs.nio.Readable;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ShutdownChannel implements Output.Listener{
    public static final Input.Listener SHUTDOWN_INPUT_CHANNEL = in -> ShutdownChannel.start(in.channel());
    public static final Output.Listener SHUTDOWN_OUTPUT_CHANNEL = out -> ShutdownChannel.start(out.channel());

    private static final ShutdownChannel INSTANCE = new ShutdownChannel();

    public static void start(NBStream channel){
        if(channel instanceof Writable){
            Output out = ((Writable)channel).out();
            out.setOutputListener(INSTANCE);
            INSTANCE.process(out);
        }else{
            Input in = ((Readable)channel).in();
            try{
                while(true){
                    in.close();
                    Input peer = in.detachInput();
                    if(peer==in)
                        return;
                    in = peer;
                }
            }catch(Throwable thr){
                Reactor.current().handleException(thr);
                channel.shutdown();
            }
        }
    }

    @Override
    public void process(Output out){
        try{
            while(true){
                out.close();
                if(out.flush()){
                    Output peer = out.detachOutput();
                    if(peer==null){
                        Input in = ((Readable)out.channel()).in();
                        while(in!=out){
                            in.close();
                            in = in.detachInput();
                        }
                        peer = out.detachOutput();
                    }
                    if(peer==out)
                        return;
                    out = peer;
                }else
                    out.addWriteInterest();
            }
        }catch(Throwable thr){
            Reactor.current().handleException(thr);
            out.channel().shutdown();
        }
    }
}
