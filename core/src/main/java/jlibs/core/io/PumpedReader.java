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

package jlibs.core.io;

import jlibs.core.lang.ImpossibleException;

import java.io.*;

/**
 * @author Santhosh Kumar T
 */
public abstract class PumpedReader extends PipedReader implements Runnable{
    private PipedWriter writer = new PipedWriter();

    public PumpedReader(){
        try{
            super.connect(writer);
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
            pump(writer);
        }catch(Exception ex){
            setException(ex);
        }finally{
            try{
                writer.close();
            }catch(IOException ex){
                this.exception = ex;
            }
        }
    }

    public PumpedReader start(){
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
     * write data into <code>writer</code>. <code>out</code> can be closed if required.
     */
    protected abstract void pump(PipedWriter writer) throws Exception;
}