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

import java.io.IOException;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class ClosingInputTask extends InputTask{
    protected abstract void cleanup();


    private Throwable thr;
    private boolean timeout;

    @Override
    public void timeout(InputChannel in) throws IOException{
        timeout = true;
        if(in.isOpen())
            in.close();
        else{
            cleanup();
            ListenerUtil.resume(detach(in), thr, timeout);
        }
    }

    private void setException(Throwable thr){
        if(this.thr==null)
            this.thr = thr;
        else
            this.thr.addSuppressed(thr);
    }

    @Override
    public void error(InputChannel in, Throwable thr){
        setException(thr);
        if(in.isOpen()){
            try{
                in.close();
            }catch(Throwable thr1){
                assert !in.isOpen();
                setException(thr1);
                if(!in.isClosed()){
                    cleanup();
                    ListenerUtil.resume(detach(in), this.thr, timeout);
                }
            }
        }else{
            cleanup();
            ListenerUtil.resume(detach(in), this.thr, timeout);
        }
    }

    @Override
    public void closed(InputChannel in) throws IOException{
        cleanup();
        ListenerUtil.resume(detach(in), this.thr, timeout);
    }
}
