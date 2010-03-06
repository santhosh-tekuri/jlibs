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
 * This class simplifies usage of {@link java.io.PipedReader} and {@link java.io.PipedWriter}
 * <p>
 * Using {@link java.io.PipedReader} and {@link java.io.PipedWriter} looks cumbersome.
 * <pre class="prettyprint">
 * PipedReader pipedReader = new PipedReader();
 * final PipedWriter pipedWriter = new PipedWriter(pipedReader);
 * final IOException ioEx[] = { null };
 * new Thread(){
 *     &#064;Override
 *     public void run(){
 *         try{
 *             writeDataTo(pipedWriter);
 *             pipedWriter.close();
 *         }catch(IOException ex){
 *             ioEx[0] = ex;
 *         }
 *     }
 * }.start();
 * readDataFrom(pipedReader);
 * pipedReader.close();
 * if(ioEx[0]!=null)
 *     throw new RuntimeException("something gone wrong", ioEx[0]);
 * </pre>
 * The same can be achieved using {@link PumpedReader} as follows:
 * <pre class="prettyprint">
 * PumpedReader reader = new PumpedReader(){
 *     &#064;Override
 *     protected void {@link #pump(java.io.PipedWriter) pump}(PipedWriter writer) throws Exception{
 *         writeDataTo(writer);
 *     }
 * }.{@link #start()}; // start() will spawn new thread
 * readDataFrom(reader);
 * reader.{@link #close()}; // any exceptions occurred in pump(...) are thrown by close()
 * </pre>
 *
 * {@link PumpedReader} is an abstract class with following abstract method:
 * <pre class="prettyprint">
 * protected abstract void {@link #pump(java.io.PipedWriter) pump}(PipedWriter writer) throws Exception;
 * </pre>
 * This method implementation should write data into {@code writer} which is passed as argument and close it.<br>
 * Any exception thrown by {@link #pump(java.io.PipedWriter) pump(...)} are wrapped in {@link java.io.IOException} and rethrown by PumpedReader.{@link #close()}.
 * <p>
 * {@link PumpedReader} implements {@link Runnable} which is supposed to be run in thread.<br>
 * You can use PumpedReader.{@link #start()} method to start thread or spawn thread implicitly.<br>
 * {@link #start()} method returns self reference.
 * <pre class="prettyprint">
 * public PumpedReader {@link #start()};
 * </pre>
 * The advantage of {@link PumpedReader} over {@link java.io.PipedReader}/{@link java.io.PipedWriter}/{@link Thread} is:
 * <ul>
 * <li>it doesn't clutter the exising flow of code</li>
 * <li>exception handling is better</li>
 * </ul>
 *
 * @see PumpedInputStream
 *
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

    /**
     * Starts a thread with this instance as runnable.
     *
     * @return self reference
     */
    public PumpedReader start(){
        new Thread(this).start();
        return this;
    }

    /**
     * Closes this stream and releases any system resources
     * associated with the stream.
     * <p>
     * Any exception thrown by {@link #pump(java.io.PipedWriter)}
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
     * @param writer writer into which data should be written
     */
    protected abstract void pump(PipedWriter writer) throws Exception;
}