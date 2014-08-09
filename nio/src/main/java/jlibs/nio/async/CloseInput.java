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

import jlibs.nio.channels.InputChannel;
import jlibs.nio.channels.ListenerUtil;

/**
 * @author Santhosh Kumar Tekuri
 */
public class CloseInput extends InputTask{
    public static final CloseInput INSTANCE = new CloseInput();
    private CloseInput(){}

    public void start(InputChannel in, ExecutionContext context){
        try{
            if(in.isOpen())
                in.close();
        }catch(Throwable thr){
            ListenerUtil.resume(context, thr, false);
            return;
        }
        if(in.isClosed())
            ListenerUtil.resume(context, null, false);
        else
            super.start(in, context);
    }
}
