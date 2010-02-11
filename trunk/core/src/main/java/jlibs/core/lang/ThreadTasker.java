/**
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

package jlibs.core.lang;

/**
 * @author Santhosh Kumar T
 */
public abstract class ThreadTasker{
    public abstract boolean isValid();
    protected abstract void executeAndWait(Runnable runnable);
    public abstract void executeLater(Runnable runnable);

    public void execute(Runnable runnable){
        if(isValid())
            runnable.run();
        else
            executeAndWait(runnable);
    }

    public <R, E extends Exception> R execute(ThrowableTask<R, E> task) throws E{
        execute(task.asRunnable());
        return task.getResult();
    }
    
    public <R> R execute(Task<R> task){
        execute(task.asRunnable());
        return task.getResult();
    }
}
