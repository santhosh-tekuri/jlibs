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

import jlibs.nio.*;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static jlibs.nio.Debugger.DEBUG;
import static jlibs.nio.Debugger.println;

/**
 * @author Santhosh Kumar Tekuri
 */
public class IOListener implements Input.Listener, Output.Listener{
    private Task task;

    public void start(Task task, Input in, Output out){
        this.task = task;
        if(in!=null)
            in.setInputListener(this);
        if(out!=null)
            out.setOutputListener(this);
        task.init(this, in, out);
        process(task.firstOp);
    }

    public void start(Task task, Connection con){
        start(task, con.in(), con.out());
    }

    @Override
    public void process(Input in){
        process(OP_READ);
    }

    @Override
    public void process(Output out){
        process(OP_WRITE);
    }

    @SuppressWarnings("unchecked")
    private void process(int readyOp){
        Throwable error = null;
        try{
            while(true){
                Throwable taskError = null;
                if(DEBUG)
                    println(task+".process{");
                try{
                    if(!task.process(readyOp)){
                        if(DEBUG)
                            println("}");
                        return;
                    }
                }catch(Throwable thr){
                    if(DEBUG)
                        println("throw "+thr);
                    taskError = thr;
                }
                if(DEBUG)
                    println("}");
                task = taskFinished(taskError);
                if(task==null)
                    break;
                readyOp = task.firstOp;
            }
        }catch(Throwable thr){
            error = thr;
        }

        try{
            if(callback!=null)
                callback.completed(attachment, error);
            else if(error!=null)
                Reactor.current().handleException(error);
        }catch(Throwable thr){
            Reactor.current().handleException(error);
        }
    }

    private Task taskFinished(Throwable thr) throws Throwable{
        if(thr!=null || task.child==null)
            task.cleanup(thr);
        if(thr==null && task.child!=null){
            Task next = task.child;
            task.child = null;
            return next;
        }
        Task parent = task.parent;
        task.parent = null;
        if(parent==null){
            if(thr!=null)
                throw thr;
        }else{
            if(DEBUG)
                println(parent+".childTaskFinished("+task+", "+thr+"){");
            parent.firstOp = parent.childTaskFinished(task, thr);
            if(DEBUG)
                println("}");
        }
        return parent;
    }

    public interface Callback<A>{
        public void completed(A attachment, Throwable thr);
    }

    private Callback callback;
    private Object attachment;
    public <A> IOListener setCallback(Callback callback, A attachment){
        this.callback = callback;
        this.attachment = attachment;
        return this;
    }
}
