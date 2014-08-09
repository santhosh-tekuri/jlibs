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

package jlibs.nio.channels;

import jlibs.nio.Debugger;
import jlibs.nio.Reactor;
import jlibs.nio.async.ExecutionContext;
import jlibs.nio.channels.impl.SelectableChannel;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ListenerUtil{
    public static <T> void resume(ExecutionContext context, Throwable thr, boolean timeout){
        try{
            context.resume(thr, timeout);
        }catch(Throwable thr1){
            Reactor.current().handleException(thr1);
        }
    }

    /*-------------------------------------------------[ in ]---------------------------------------------------*/

    public static void ready(InputChannel in){
        if(Debugger.IO)
            Debugger.println(in.getClient()+".in.ready("+in.getClass().getSimpleName()+"){");
        try{
            if(in.getInputListener()!=null)
                in.getInputListener().ready(in);
            else if(Debugger.IO)
                Debugger.println("NoListener");
        }catch(Throwable thr){
            error(in, thr);
        }
        if(Debugger.IO)
            Debugger.println("}");
    }

    public static void timeout(InputChannel in){
        if(Debugger.IO)
            Debugger.println(in.getClient()+".in.timeout("+in.getClass().getSimpleName()+"){");
        try{
            if(in.getInputListener()!=null)
                in.getInputListener().timeout(in);
            else if(Debugger.IO)
                Debugger.println("NoListener");
        }catch(Throwable thr){
            error(in, thr);
        }
        if(Debugger.IO)
            Debugger.println("}");
    }

    public static void closed(InputChannel in){
        if(Debugger.IO)
            Debugger.println(in.getClient()+".in.closed("+in.getClass().getSimpleName()+"){");
        assert in.isClosed();
        try{
            if(in.getInputListener()!=null)
                in.getInputListener().closed(in);
            else if(Debugger.IO)
                Debugger.println("NoListener");
        }catch(Throwable thr){
            error(in, thr);
        }
        if(Debugger.IO)
            Debugger.println("}");
    }

    public static void error(InputChannel in, Throwable thr){
        if(Debugger.IO)
            Debugger.println(in.getClient()+".in.error("+in.getClass().getSimpleName()+"){");
        if(in.getInputListener()==null){
            if(Debugger.IO)
                Debugger.println("NoListener");
            Reactor.current().handleException(thr);
            return;
        }
        try{
            in.getInputListener().error(in, thr);
        }catch(Throwable thr1){
            Reactor.current().handleException(thr1);
        }
        if(Debugger.IO)
            Debugger.println("}");
    }

    /*-------------------------------------------------[ out ]---------------------------------------------------*/

    public static void ready(OutputChannel out){
        if(Debugger.IO)
            Debugger.println(out.getClient()+".out.ready("+out.getClass().getSimpleName()+"){");
        try{
            if(out.getOutputListener()!=null)
                out.getOutputListener().ready(out);
            else if(Debugger.IO)
                Debugger.println("NoListener");
        }catch(Throwable thr){
            error(out, thr);
        }
        if(Debugger.IO)
            Debugger.println("}");
    }

    public static void timeout(OutputChannel out){
        if(Debugger.IO)
            Debugger.println(out.getClient()+".out.timeout("+out.getClass().getSimpleName()+"){");
        try{
            if(out.getOutputListener()!=null)
                out.getOutputListener().timeout(out);
            else if(Debugger.IO)
                Debugger.println("NoListener");
        }catch(Throwable thr){
            error(out, thr);
        }
        if(Debugger.IO)
            Debugger.println("}");
    }

    public static void closed(OutputChannel out){
        if(Debugger.IO)
            Debugger.println(out.getClient()+".out.closed("+out.getClass().getSimpleName()+"){");
        try{
            if(out.getOutputListener()!=null)
                out.getOutputListener().closed(out);
            else if(Debugger.IO)
                Debugger.println("NoListener");
        }catch(Throwable thr){
            error(out, thr);
        }
        if(Debugger.IO)
            Debugger.println("}");
    }

    public static void error(OutputChannel out, Throwable thr){
        if(Debugger.IO)
            Debugger.println(out.getClient()+".out.error("+out.getClass().getSimpleName()+"){");
        if(out.getOutputListener()==null){
            if(Debugger.IO)
                Debugger.println("NoListener");
            Reactor.current().handleException(thr);
            return;
        }
        try{
            out.getOutputListener().error(out, thr);
        }catch(Throwable thr1){
            Reactor.current().handleException(thr1);
        }
        if(Debugger.IO)
            Debugger.println("}");
    }
}
