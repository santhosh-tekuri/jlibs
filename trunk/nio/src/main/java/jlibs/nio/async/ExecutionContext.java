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

import jlibs.nio.Client;
import jlibs.nio.Reactor;
import jlibs.nio.channels.ListenerUtil;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Santhosh Kumar Tekuri
 */
public interface ExecutionContext{
    public void resume(Throwable thr, boolean timeout);

    public static final ExecutionContext DO_NOTHING = (thr,timeout) -> {
        if(thr!=null)
            Reactor.current().handleException(thr);
    };

    public static ExecutionContext close(Closeable closeable){
        return (thr,timeout) -> {
            if(thr!=null || timeout){
                if(thr!=null)
                    Reactor.current().handleException(thr);
                if(closeable instanceof Client){
                    ((Client)closeable).closeForcefully();
                    return;
                }
            }
            try{
                closeable.close();
            }catch(IOException ex){
                Reactor.current().handleException(ex);
            }
        };
    }

    public static ExecutionContext closeClients(Client client, Client buddy){
        return (thr,timeout) -> {
            if(thr!=null || timeout){
                if(thr!=null)
                    client.reactor.handleException(thr);
                client.closeForcefully();
                buddy.closeForcefully();
                return;
            }
            client.close();
            buddy.close();
        };
    }

    public static ExecutionContext doFinally(ExecutionContext delegate, Throwable thr, boolean timeout){
        return (thr1 ,timeout1) -> {
            if(thr!=null){
                if(thr1!=null)
                    thr.addSuppressed(thr1);
                thr1 = thr;
            }
            timeout1 |= timeout;
            ListenerUtil.resume(delegate, thr1, timeout1);
        };
    }
}
