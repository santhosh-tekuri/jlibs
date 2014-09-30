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

import jlibs.nio.Connection;
import jlibs.nio.Reactor;

import java.io.IOException;

import static java.nio.channels.SelectionKey.OP_READ;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Pump extends Task{
    public Pump(){
        super(OP_READ);
        preparePump(null);
    }

    boolean pumping = true;

    @Override
    protected boolean process(int readyOp) throws IOException{
        if(pumping){
            try{
                if(doPump(readyOp))
                    pumping = false;
                else
                    return false;
            }catch(Throwable thr){
                Reactor.current().handleException(thr);
                pumping = false;
            }
        }
        return shutdown();
    }

    public static void start(Connection con){
        new IOListener().start(new Pump(), con);
    }

    public static void startTunnel(Connection con1, Connection con2){
        new IOListener().start(new Pump(), con1.in(), con2.out());
        new IOListener().start(new Pump(), con2.in(), con1.out());
    }
}
