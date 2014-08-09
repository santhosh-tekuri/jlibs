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

import jlibs.nio.channels.ListenerUtil;
import jlibs.nio.channels.OutputChannel;

import java.io.IOException;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class OutputTask implements OutputChannel.Listener{
    protected void start(OutputChannel out, ExecutionContext context){
        out.attach(context);
        out.setOutputListener(this);
    }

    protected final ExecutionContext detach(OutputChannel out){
        ExecutionContext context = out.attachment();
        out.attach(null);
        out.setOutputListener(null);
        return context;
    }

    @Override
    public void timeout(OutputChannel out) throws IOException{
        ListenerUtil.resume(detach(out), null, true);
    }

    @Override
    public void error(OutputChannel out, Throwable thr){
        ListenerUtil.resume(detach(out), thr, false);
    }

    @Override
    public void closed(OutputChannel out) throws IOException{
        ListenerUtil.resume(detach(out), null, false);
    }

    @Override
    public void ready(OutputChannel out) throws IOException{}
}
