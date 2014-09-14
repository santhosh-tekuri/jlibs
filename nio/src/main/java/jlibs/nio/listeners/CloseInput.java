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
import jlibs.nio.Input;
import jlibs.nio.Output;

/**
 * @author Santhosh Kumar Tekuri
 */
public class CloseInput implements Output.Listener{
    public static final Input.Listener LISTENER = in -> CloseInput.start(in, null, InputExceptionListener.DO_NOTHING);

    private Output.Listener outListener;
    private Input.Listener successListener;
    private InputExceptionListener failureListener;
    public CloseInput(Output.Listener outListener, Input.Listener successListener, InputExceptionListener failureListener){
        this.outListener = outListener;
        this.successListener = successListener;
        this.failureListener = failureListener;
    }

    public static void start(Input in, Input.Listener successListener, InputExceptionListener failureListener){
        try{
            in.close();
            if(in instanceof Output){
                Output out = (Output)in;
                if(out.flush()){
                    in.setInputListener(successListener);
                    in.wakeupReader();
                }else{
                    out.setOutputListener(new CloseInput(out.getOutputListener(), successListener, failureListener));
                    out.addWriteInterest();
                }
            }
        }catch(Throwable thr){
            try{
                failureListener.process(in, thr);
            }catch(Throwable thr1){
                Reactor.current().handleException(thr1);
            }
        }
    }

    @Override
    public void process(Output out){
        try{
            if(out.flush()){
                out.setOutputListener(outListener);
                Input in = (Input)out;
                in.setInputListener(successListener);
                in.wakeupReader();
            }else
                out.addWriteInterest();
        }catch(Throwable thr){
            try{
                Input in = (Input)out;
                failureListener.process(in, thr);
            }catch(Throwable thr1){
                Reactor.current().handleException(thr1);
            }
        }
    }
}
