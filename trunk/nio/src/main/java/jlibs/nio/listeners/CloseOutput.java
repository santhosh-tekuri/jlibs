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

import jlibs.nio.Reactor;
import jlibs.nio.Output;

/**
 * @author Santhosh Kumar Tekuri
 */
public class CloseOutput implements Output.Listener{
    public static final Output.Listener LISTENER = out -> CloseOutput.start(out, null, OutputExceptionListener.DO_NOTHING);

    private Output.Listener successListener;
    private OutputExceptionListener failureListener;
    private CloseOutput(Output.Listener successListener, OutputExceptionListener failureListener){
        this.successListener = successListener;
        this.failureListener = failureListener;
    }

    public static void start(Output out, Output.Listener successListener, OutputExceptionListener failureListener){
        try{
            out.close();
            if(out.flush()){
                out.setOutputListener(successListener);
                out.wakeupWriter();
            }else{
                out.setOutputListener(new CloseOutput(successListener, failureListener));
                out.addWriteInterest();
            }
        }catch(Throwable thr){
            try{
                failureListener.process(out, thr);
            }catch(Throwable thr1){
                Reactor.current().handleException(thr1);
            }
        }
    }

    @Override
    public void process(Output out){
        try{
            if(out.flush()){
                out.setOutputListener(successListener);
                out.wakeupWriter();
            }else
                out.addWriteInterest();
        }catch(Throwable thr){
            try{
                failureListener.process(out, thr);
            }catch(Throwable thr1){
                Reactor.current().handleException(thr1);
            }
        }
    }
}
