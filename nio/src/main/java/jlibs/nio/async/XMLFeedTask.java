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

import jlibs.nbp.Feeder;
import jlibs.nio.channels.InputChannel;

import java.io.IOException;

/**
 * @author Santhosh Kumar Tekuri
 */
public class XMLFeedTask extends InputTask{
    private Feeder feeder;
    public XMLFeedTask(Feeder feeder){
        this.feeder = feeder;
    }

    @Override
    public void start(InputChannel in, ExecutionContext context){
        super.start(in, context);
        try{
            ready(in);
        }catch(Throwable thr){
            error(in, thr);
        }
    }

    @Override
    public void ready(InputChannel in) throws IOException{
        feeder = feeder.feed();
        if(feeder==null)
            in.close();
    }
}
