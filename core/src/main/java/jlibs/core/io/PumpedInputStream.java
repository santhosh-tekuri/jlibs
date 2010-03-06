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

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.IOException;

/**
 * This class simplifies usage of {@link java.io.PipedInputStream} and {@link java.io.PipedOutputStream}
 * <p>
 * Using {@link java.io.PipedInputStream} and {@link java.io.PipedOutputStream} looks cumbersome.
 * <pre class="prettyprint">
 * PipedInputStream pipedIn = new PipedInputStream();
 * final PipedOutputStream pipedOut = new PipedOutputStream(pipedIn);
 * final IOException ioEx[] = { null };
 * new Thread(){
 *     &#064;Override
 *     public void run(){
 *         try{
 *             writeDataTo(pipedOut);
 *             pipedOut.close();
 *         }catch(IOException ex){
 *             ioEx[0] = ex;
 *         }
 *     }
 * }.start();
 * readDataFrom(pipedIn);
 * pipedIn.close();
 * if(ioEx[0]!=null)
 *     throw new RuntimeException("something gone wrong", ioEx[0]);
 * </pre>
 * The same can be achieved using {@link PumpedInputStream} as follows:
 * <pre class="prettyprint">
 * PumpedInputStream in = new PumpedInputStream(){
 *     &#064;Override
 *     protected void {@link #pump(java.io.PipedOutputStream) pump}(PipedOutputStream out) throws Exception{
 *         writeDataTo(out);
 *     }
 * }.{@link #start()}; // start() will spawn new thread
 * readDataFrom(in);
 * in.{@link #close()}; // any exceptions occurred in pump(...) are thrown by close()
 * </pre>
 *
 * {@link PumpedInputStream} is an abstract class with following abstract method:
 * <pre class="prettyprint">
 * protected abstract void {@link #pump(java.io.PipedOutputStream) pump}(PipedOutputStream out) throws Exception;
 * </pre>
 * This method implementation should write data into {@code out} which is passed as argument and close it.<br>
 * Any exception thrown by {@link #pump(java.io.PipedOutputStream) pump(...)} are wrapped in {@link java.io.IOException} and rethrown by PumpedReader.{@link #close()}.
 * <p>
 * {@link PumpedInputStream} implements {@link Runnable} which is supposed to be run in thread.<br>
 * You can use PumpedInputStream.{@link #start()} method to start thread or spawn thread implicitly.<br>
 * {@link #start()} method returns self reference.
 * <pre class="prettyprint">
 * public PumpedInputStream {@link #start()};
 * </pre>
 * The advantage of {@link PumpedInputStream} over {@link java.io.PipedInputStream}/{@link java.io.PipedOutputStream}/{@link Thread} is:
 * <ul>
 * <li>it doesn't clutter the exising flow of code</li>
 * <li>exception handling is better</li>
 * </ul>
 *
 * @see PumpedReader
 *
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

    /**
     * Starts a thread with this instance as runnable.
     *
     * @return self reference
     */
    public PumpedInputStream start(){
        new Thread(this).start();
        return this;
    }

    /**
     * Closes this stream and releases any system resources
     * associated with the stream.
     * <p>
     * Any exception thrown by {@link #pump(java.io.PipedOutputStream)}
     * are cached and rethrown by this method
     *
     * @exception  IOException  if an I/O error occurs.
     */
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
     * Subclasse implementation should write data into <code>writer</code>.
     *
     * @param out outputstream into which data should be written
     */
    protected abstract void pump(PipedOutputStream out) throws Exception;
}
