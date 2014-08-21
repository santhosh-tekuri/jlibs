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

package jlibs.nio.async;

import jlibs.nio.Reactor;
import jlibs.nio.channels.InputChannel;

import java.io.IOException;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class InputTask implements InputChannel.Listener{
    protected void start(InputChannel in, ExecutionContext context){
        in.attach(context);
        in.setInputListener(this);
    }

    protected void resume(InputChannel in, Throwable thr, boolean timeout){
        cleanup();
        ExecutionContext context = in.attachment();
        in.attach(null);
        in.setInputListener(null);
        try{
            context.resume(thr, timeout);
        }catch(Throwable thr1){
            Reactor.current().handleException(thr1);
        }
    }

    protected void cleanup(){}

    @Override
    public void timeout(InputChannel in) throws IOException{
        resume(in, null, true);
    }

    @Override
    public void error(InputChannel in, Throwable thr){
        resume(in, thr, false);
    }

    @Override
    public void closed(InputChannel in) throws IOException{
        resume(in, null, false);
    }

    @Override
    public void ready(InputChannel in) throws IOException{}
}
