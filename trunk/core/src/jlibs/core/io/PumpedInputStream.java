/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.core.io;

import jlibs.core.lang.ImpossibleException;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.IOException;

/**
 * @author Santhosh Kumar T
 */
public abstract class PumpedInputStream extends PipedInputStream implements Runnable{
    private PipedOutputStream out = new PipedOutputStream();

    public PumpedInputStream(){
        try{
            super.connect(out);
        }catch(IOException ex){
            throw new ImpossibleException();
        }
    }

    private IOException exception;
    private void setException(Exception ex){
        if(ex instanceof IOException)
            exception = (IOException)ex;
        else
            exception = new IOException(ex);
    }

    @Override
    public void run(){
        try{
            pump(out);
        }catch(Exception ex){
            setException(ex);
        }finally{
            try{
                out.close();
            }catch(IOException ex){
                this.exception = ex;
            }
        }
    }

    public PumpedInputStream start(){
        new Thread(this).start();
        return this;
    }

    @Override
    @SuppressWarnings({"ThrowFromFinallyBlock"})
    public void close() throws IOException{
        try{
            super.close();
        }finally{
            if(exception!=null)
                throw exception;
        }
    }

    /**
     * write data into <code>out</code>. <code>out</code> can be closed if required.
     */
    protected abstract void pump(PipedOutputStream out) throws Exception;
}
