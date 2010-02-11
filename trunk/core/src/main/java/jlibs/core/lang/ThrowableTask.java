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
public abstract class ThrowableTask<R, E extends Exception>{
    private Class<E> exceptionClass;
    
    private R r;
    private Exception ex;

    public ThrowableTask(Class<E> exceptionClass){
        this.exceptionClass = exceptionClass;
    }

    public abstract R run() throws E;
    
    @SuppressWarnings({"unchecked"})
    public R getResult() throws E{
        if(exceptionClass.isInstance(ex))
            throw (E)ex;
        else if(ex!=null)
            throw new RuntimeException(ex);
        return r;
    }
    
    public Exception getException(){
        return ex;
    }

    public Runnable asRunnable(){
        return new Runnable(){
            public void run(){
                try{
                    r = ThrowableTask.this.run();
                }catch(Exception ex){
                    ThrowableTask.this.ex = ex;
                }
            }
        };
    }
}
