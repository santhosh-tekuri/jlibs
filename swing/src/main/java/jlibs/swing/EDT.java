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

package jlibs.swing;

import jlibs.core.lang.ThreadTasker;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Santhosh Kumar T
 */
public class EDT extends ThreadTasker{
    public static final EDT INSTANCE = new EDT();
    private EDT(){}
    
    @Override
    public boolean isValid(){
        return EventQueue.isDispatchThread();
    }

    @Override
    protected void executeAndWait(Runnable runnable){
        try{
            EventQueue.invokeAndWait(runnable);
        }catch(InterruptedException ex){
            throw new RuntimeException(ex);
        } catch(InvocationTargetException ex){
            if(ex.getCause()==null)
                throw new RuntimeException(ex);
            else if(ex.getCause() instanceof RuntimeException)
                throw (RuntimeException)ex.getCause();
            else
                throw new RuntimeException(ex.getCause());
        }
    }

    @Override
    public void executeLater(Runnable runnable){
        EventQueue.invokeLater(runnable);
    }
}
