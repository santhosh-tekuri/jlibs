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

package jlibs.core.nio;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

/**
 * @author Santhosh Kumar T
 */
public class SSLTransport extends Debuggable implements Transport{
    private Transport transport;
    private final SSLEngine engine;

    private final ByteBuffer appData; // read-mode: to be delivered to app
    private final ByteBuffer inData;  // write-mode: read from socket
    private final ByteBuffer outData; // read-mode: to be written to socket
    private final ByteBuffer dummy = ByteBuffer.allocate(0);

    private SSLEngineResult.HandshakeStatus hsStatus;
    private SSLEngineResult.Status status = null;

    public SSLTransport(Transport transport, SSLEngine engine) throws IOException{
        this.transport = transport;
        this.engine = engine;

//        if(transport instanceof Debuggable)
//            ((Debuggable)transport).DEBUG = DEBUG;

        SSLSession session = engine.getSession();

        inData = ByteBuffer.allocate(session.getPacketBufferSize());
        appData = ByteBuffer.allocate(session.getApplicationBufferSize());
        outData = ByteBuffer.allocate(session.getPacketBufferSize());

        appData.position(appData.limit());
        outData.position(outData.limit());

        initHandshake();
    }

    @Override
    public long id(){
        return -transport.id();
    }

    @Override
    public void id(long newID){
        transport.id(newID);
    }

    @Override
    public ClientChannel client(){
        return transport.client();
    }

    /*-------------------------------------------------[ Handshake ]---------------------------------------------------*/

    private boolean initialHandshake = true;

    private void initHandshake() throws IOException{
        if(DEBUG)
            println("initHandshake@"+id()+"{");
        int ops = transport.interests();
        boolean readWait = (ops&OP_READ)!=0;
        boolean writeWait = (ops&OP_WRITE)!=0;
        if(readWait)
            transport.removeInterest(OP_READ);
        if(writeWait)
            transport.removeInterest(OP_WRITE);

        engine.beginHandshake();
        hsStatus = engine.getHandshakeStatus();
        doHandshake();

        if(readWait)
            addInterest(OP_READ);
        if(writeWait)
            addInterest(OP_WRITE);
        if(DEBUG)
            println("}");
    }

    private void doHandshake() throws IOException{
        while(true){
            SSLEngineResult res;
            switch(hsStatus){
                case FINISHED:
                    if(initialHandshake)
                        finishInitialHandshake();
                    return;

                case NEED_TASK:
                    if(DEBUG)
                        println("running tasks");
                    Runnable task;
                    while((task=engine.getDelegatedTask())!=null)
                        task.run();
                    hsStatus = engine.getHandshakeStatus();
                    break;

                case NEED_UNWRAP:
                    readAndUnwrap();
                    if(engine.isInboundDone()){
                        if(appReadWait)
                            enableAppRead();
                    }else{
                        // During normal operation a call to readAndUnwrap() that results in underflow
                        // does not cause the channel to activate read interest with the selector.
                        // Therefore, if we are at the initial handshake, we must activate the read
                        // interest explicitly.
                        if(initialHandshake && status==SSLEngineResult.Status.BUFFER_UNDERFLOW)
                            waitForChannelRead();
                    }
                    return;

                case NEED_WRAP:
                    // First make sure that the out buffer is completely empty. Since we
                    // cannot call wrap with data left on the buffer
                    if(outData.hasRemaining()){
                        assert channelWriteInterestSet : "Write interest should be active";
                        return;
                    }

                    // Prepare to write
                    outData.clear();
                    res = wrap(dummy);
                    assert res.bytesProduced()!=0 : "No net data produced during handshake wrap.";
                    assert res.bytesConsumed()==0 : "App data consumed during handshake wrap.";
                    hsStatus = res.getHandshakeStatus();
                    outData.flip();

                    // Now send the data and come back here only when
                    // the data is all sent
                    if(!flushData()) // There is data left to be send. Wait for it
                        return;
                    // All data was sent. Break from the switch but don't
                    // exit this method. It will loop again, since there may be more
                    // operations that can be done without blocking.
                    break;

                case NOT_HANDSHAKING:
                    assert false : "doHandshake() should never reach the NOT_HANDSHAKING state";
                    return;
            }
        }
    }

    private void finishInitialHandshake() throws IOException{
        if(DEBUG)
            println("finished InitialHandshake");
        initialHandshake = false;

        // Activate interests
        if(appReadWait){
            if(appData.hasRemaining())
                enableAppRead();
            else
                waitForChannelRead();
        }
        if(appWriteWait){
            assert !outData.hasRemaining() : "There is data left to send after handshake!";
            // We don't need to register with the selector, since we
            // know that the outData buffer is empty after the handshake.
            // Just send the write event to the application.
            enableAppWrite();
        }
    }

    /*-------------------------------------------------[ Helpers ]---------------------------------------------------*/

    private int readAndUnwrap() throws IOException{
        assert !appData.hasRemaining() : "appData not empty";
        // No decrypted data left on the buffers.
        // Try to read from the socket. There may be some data
        // on the inData buffer, but it might not be sufficient.
        int bytesRead = transport.read(inData);
        if(bytesRead==-1){
            try{
                engine.closeInbound(); // signal end of stream
            }catch(SSLException e){
                // ignore
            }
            assert engine.isInboundDone();
            // EOF. But do we still have some useful data available?
            if(inData.position()==0 || status==SSLEngineResult.Status.BUFFER_UNDERFLOW){
                // read nothing (OR) not enough data to reassemble a TLS packet
                return -1;
            }
            // Although we reach EOF, we still have some data left to
            // be decrypted. We must process it
        }

        // During an handshake renegotiation we might need to perform
        // several unwraps to consume the handshake data.
        appData.clear();
        inData.flip();
        SSLEngineResult res;
        do{
            res = unwrap();
        }while(res.getStatus()==SSLEngineResult.Status.OK &&
                res.getHandshakeStatus()==SSLEngineResult.HandshakeStatus.NEED_UNWRAP &&
                res.bytesProduced()==0);
        inData.compact();
        appData.flip();

        // If the initial handshake finish after an unwrap, we must activate
        // the application interests, if any were set during the handshake
        if(res.getHandshakeStatus()==SSLEngineResult.HandshakeStatus.FINISHED)
            finishInitialHandshake();

        // If no data was produced, and the status is still ok, try to read once more
        if(!appData.hasRemaining() &&
                res.getStatus()==SSLEngineResult.Status.OK &&
                inData.hasRemaining()){
            appData.clear();
            inData.flip();
            res = unwrap();
            inData.compact();
            appData.flip();
        }

        /*
         * The status may be:
         * OK - Normal operation
         * OVERFLOW - Should never happen since the application buffer is
         * 	sized to hold the maximum packet size.
         * UNDERFLOW - Need to read more data from the socket. It's normal.
         * CLOSED - The other peer closed the socket. Also normal.
         */
        status = res.getStatus();
        hsStatus = res.getHandshakeStatus();
        // Should never happen, the appData must always have enough space
        // for an unwrap operation
        assert status!=SSLEngineResult.Status.BUFFER_OVERFLOW : "Buffer should not overflow: "+res.toString();

        // The handshake status here can be different than NOT_HANDSHAKING
        // if the other peer closed the connection. So only check for it
        // after testing for closure.
        if(status==SSLEngineResult.Status.CLOSED){
            assert engine.isInboundDone();
            if(DEBUG)
                println("received shutdown signal");
            shutdown = true;
            doShutdown();
            return -1;
        }

//        inData.compact();
//        appData.flip();

        if(hsStatus==SSLEngineResult.HandshakeStatus.NEED_TASK ||
                hsStatus==SSLEngineResult.HandshakeStatus.NEED_WRAP ||
                hsStatus==SSLEngineResult.HandshakeStatus.FINISHED)
        {
            if(DEBUG)
                println("re-handshaking...");
            doHandshake();
        }

        return appData.remaining();
    }

    /**
     * Tries to write the data on the outData buffer to the socket.
     * If not all data is sent, the write interest is activated with
     * the selector thread.
     *
     * @return True if all data was sent. False otherwise.
     */
    private boolean flushData() throws IOException{
        assert outData.hasRemaining() : "Trying to write empty outData";
        try{
            transport.write(outData);
        }catch(IOException ioe){
            if(DEBUG)
                println("channel.write: ["+ioe.getClass().getSimpleName()+"] "+ioe.getMessage());
            // Clear the buffer. If write failed, the socket is dead. Clearing
            // the buffer indicates that no more write should be attempted.
            outData.position(outData.limit());
            throw ioe;
        }
        if(outData.hasRemaining()){
            waitForChannelWrite();
            return false;
        }else{
            if(DEBUG)
                println("no outData left to send");
            return true;
        }
    }

    private void checkChannelStillValid() throws IOException{
        if(closed)
            throw new ClosedChannelException();
        if(asynchException!=null)
            throw new IOException("Asynchronous failure: "+asynchException.getMessage(), asynchException);
    }

    private SSLEngineResult wrap(ByteBuffer src) throws SSLException{
        SSLEngineResult result = engine.wrap(src, outData);
        if(DEBUG){
            println(String.format(
                "%-6s: %-16s %-15s %5d %5d",
                "wrap",
                result.getStatus(), result.getHandshakeStatus(),
                result.bytesConsumed(), result.bytesProduced()
            ));
        }
        return result;
    }

    private SSLEngineResult unwrap() throws SSLException{
        SSLEngineResult result = engine.unwrap(inData, appData);
        if(DEBUG){
            println(String.format(
                "%-6s: %-16s %-15s %5d %5d",
                "unwrap",
                result.getStatus(), result.getHandshakeStatus(),
                result.bytesConsumed(), result.bytesProduced()
            ));
        }
        return result;
    }

    /*-------------------------------------------------[ App Waiting ]---------------------------------------------------*/

    @Override
    public void addInterest(int interest) throws IOException{
        if(DEBUG)
            println("app.register@"+id()+"{");

        if(interest==OP_READ)
            waitForAppRead();

        if(interest==OP_WRITE)
            waitForAppWrite();


        if(appReadReady || appWriteReady)
            client().nioSelector.ready.add(client());
        if(DEBUG)
            println("}");
    }

    @Override
    public void removeInterest(int operation) throws IOException{
        throw new UnsupportedOperationException();
    }

    @Override
    public int interests(){
        int ops = 0;
        if(appReadWait)
            ops |= OP_READ;
        if(appWriteWait)
            ops |= OP_WRITE;
        return ops;
    }

    private boolean appReadWait = false;
    private void waitForAppRead() throws IOException{
        checkChannelStillValid();
        if(!appReadWait){
            appReadWait = true;
            if(DEBUG)
                println("app.readWait");
            if(!initialHandshake){
                if(appData.hasRemaining()){
                    enableAppRead();
                }else{
                    if(inData.position()==0 || status== SSLEngineResult.Status.BUFFER_UNDERFLOW)
                        waitForChannelRead();
                    else{
                        // There is encrypted data available. It may or may not
                        // be enough to reassemble a full packet. We have to check it.
                        if(readAndUnwrap()==0) // Not possible to reassemble a full packet.
                            waitForChannelRead();
                        else // EOF or appData available
                            enableAppRead();
                    }
                }
            }
        }
    }

    private boolean appWriteWait = false;
    private void waitForAppWrite() throws IOException{
        checkChannelStillValid();
        if(!appWriteWait){
            appWriteWait = true;
            if(DEBUG)
                println("app.writeWait");
            if(!initialHandshake){
                // Check if we can write now
                if(outData.hasRemaining()){
                    assert channelWriteInterestSet : "Write interest should be active";
                    // The buffer is full, the application can't write anymore.
                    // The write interest must be set...
                }else{
                    assert !channelWriteInterestSet : "Write interest should not be active";
                    // outData is empty. But don't fire the write event right
                    // now. Instead, register with the SecureChannelManager and
                    // wait for the SelectorThread to call for these events.
                    enableAppWrite();
                }
            }
        }
    }

    /*-------------------------------------------------[ App Ready ]---------------------------------------------------*/

    private boolean appReadReady = false;
    private void enableAppRead(){
        if(DEBUG)
            println("app.readReady");
        appReadReady = true;
    }

    private boolean appWriteReady = false;
    private void enableAppWrite(){
        if(DEBUG)
            println("app.writeReady");
        appWriteReady = true;
    }

    @Override
    public int ready(){
        int ops = 0;
        if(appReadReady)
            ops |= OP_READ;
        if(appWriteReady)
            ops |= OP_WRITE;
        return ops;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException{
        if(appReadReady)
            appReadWait = false;
        appReadReady = false;
        if(DEBUG)
            println("app.read@"+id()+"{");
        int read = appRead(dst);
        if(DEBUG){
            println("return: "+read);
            println("}");
        }
        return read;
    }

    @Override
    public int write(ByteBuffer src) throws IOException{
        if(appWriteReady)
            appWriteWait = false;
        appWriteReady = false;
        if(DEBUG)
            println("app.write@"+id()+"{");
        int wrote = appWrite(src);
        if(DEBUG){
            println("return: "+wrote);
            println("}");
        }
        return wrote;
    }

    private int appRead(ByteBuffer dst) throws IOException{
        checkChannelStillValid();

        // Check if the stream is closed.
        if(engine.isInboundDone()){
//            peerClosed = true;
            return -1; // We reached EOF.
        }

        if(initialHandshake)
            return 0;

        // Perhaps we should always try to read some data from
        // the socket. In some situations, it might not be possible
        // to unwrap some of the data stored on the buffers before
        // reading more.

        // First check if there is decrypted data waiting in the buffers
        if(!appData.hasRemaining()){
            int appBytesProduced = readAndUnwrap();
            if(appBytesProduced==-1 || appBytesProduced==0){
                return appBytesProduced;
            }
        }

        // It's not certain that we will have some data decrypted ready to
        // be sent to the application. Anyway, copy as much data as possible
        int limit = Math.min(appData.remaining(), dst.remaining());
        if(dst.hasArray()){
            System.arraycopy(appData.array(), appData.position(), dst.array(), dst.position(), limit);
            appData.position(appData.position()+limit);
            dst.position(dst.position()+limit);
        }else{
            for(int i=0; i<limit; i++)
                dst.put(appData.get());
        }
        return limit;
    }

    private int appWrite(ByteBuffer src) throws IOException{
        checkChannelStillValid();
        if(!src.hasRemaining())
            return 0;
        if(initialHandshake){
            if(DEBUG)
                println("Writing not possible during handshake");
            // Not ready to write
            return 0;
        }

        // First, check if we still have some data waiting to be sent.
        if(outData.hasRemaining()){
            // There is. Don't try to send it. We should be registered
            // waiting for a write event from the selector thread
            assert channelWriteInterestSet : "Write interest should be active"+ outData;
            return 0;
        }
        assert !channelWriteInterestSet : "Write interest should not be active";

        // There is no data left to be sent. Clear the buffer and get
        // ready to encrypt more data.
        outData.clear();
        SSLEngineResult res = wrap(src);
        // Prepare the buffer for reading
        outData.flip();
        flushData();

        // Return the number of bytes read
        // from the source buffer
        return res.bytesConsumed();
    }

    /*-------------------------------------------------[ Channel Waiting ]---------------------------------------------------*/

    private boolean channelReadWait = false;
    private void waitForChannelRead() throws IOException{
        if(!channelReadWait){
            channelReadWait = true;
            transport.addInterest(OP_READ);
        }
    }

    private boolean channelWriteInterestSet = false;
    private void waitForChannelWrite() throws IOException{
        if(!channelWriteInterestSet){
            channelWriteInterestSet = true;
            transport.addInterest(OP_WRITE);
        }
    }

    /*-------------------------------------------------[ Channel Ready ]---------------------------------------------------*/

    @Override
    public boolean process(){
        if(DEBUG)
            println("process@"+id()+"{");
        try{
            int ops = transport.interests();
            boolean readReady = (ops&OP_READ)!=0;
            boolean writeReady = (ops&OP_WRITE)!=0;
            if(readReady)
                transport.removeInterest(OP_READ);
            if(writeReady)
                transport.removeInterest(OP_WRITE);

            if(readReady)
                channelRead();
            if(transport.isOpen() && writeReady) // to check key validity
                channelWrite();
        }catch(IOException ex){
            handleAsynchException(ex);
        }
        if(DEBUG)
            println("}");

        return appReadReady || appWriteReady;
    }

    private void channelRead(){
        assert initialHandshake || appReadWait : "Trying to read when there is no read interest set";
        assert channelReadWait : "Method called when no read interest was set";
        channelReadWait = false;

        try{
            if(initialHandshake)
                doHandshake();
            else if (shutdown)
                doShutdown();
            else{
                // The read interest is always set when this method is called
                assert appReadWait : "channelRead() called without read interest being set";

                int bytesUnwrapped = readAndUnwrap();
                if(bytesUnwrapped==-1){
                    // End of stream.
                    assert engine.isInboundDone() : "End of stream but engine inbound is not closed";

                    // We must inform the client of the EOF
                    enableAppRead();
                }else if(bytesUnwrapped==0)
                    waitForChannelRead(); // Must read more data
                else
                    enableAppRead(); // There is data to be read by the application. Notify it.
            }
        }catch(IOException ex){
            handleAsynchException(ex);
        }
    }

    private void channelWrite(){
        assert channelWriteInterestSet : "Write event when no write interest set";
        channelWriteInterestSet = false;

        try{
            if(flushData()){ // The buffer was sent completely
                if(initialHandshake)
                    doHandshake();
                else if(shutdown)
                    doShutdown();
                else{
                    // If the listener is interested in writing,
                    // prepare to fire the event.
                    if(appWriteWait)
                        enableAppWrite();
                }
            }else{
                // There is still more data to be sent. Wait for another
                // write event. Calling flush data already resulted in the
                // write interest being reactivated.
            }
        }catch(IOException ex){
            handleAsynchException(ex);
        }
    }

    /*-------------------------------------------------[ Shutdown ]---------------------------------------------------*/

    private void closeChannel(){
        try{
            transport.close();
        }catch(IOException ignore){
            // Ignore
        }
    }
    private void doShutdown() throws IOException{
        assert !outData.hasRemaining() : "Buffer was not empty.";
        // Either shutdown was initiated now or we are on the middle
        // of shutting down and this method was called after emptying
        // the out buffer

        // If the engine has nothing else to do, close the socket. If
        // this socket is dead because of an exception, close it
        // immediately
        if(asynchException!=null || engine.isOutboundDone()){
            // If no data was produced by the call to wrap, shutdown is complete
            closeChannel();
            return;
        }

        // The engine has more things to send
        /*
         * By RFC 2616, we can "fire and forget" our close_notify
         * message, so that's what we'll do here.
         */
        outData.clear();
        try{
            SSLEngineResult res = wrap(dummy);
        }catch(SSLException ex){
            // Problems with the engine. Probably it is dead. So close
            // the socket and forget about it.
            if(DEBUG)
                println("Error during shutdown.\n" + ex.toString());
            closeChannel();
            return;
        }
        outData.flip();
        flushData();

        if(asynchException!=null || engine.isOutboundDone()){
            // If no data was produced by the call to wrap, shutdown is complete
            closeChannel();
        }
    }

    /*-------------------------------------------------[ Close ]---------------------------------------------------*/

    private boolean shutdown = false;
    private boolean closed = false;

    @Override
    public boolean isOpen(){
        return !closed;
    }

    @Override
    public void close() throws IOException{
        if(DEBUG)
            println("close@"+id()+"{");
        if(shutdown){
            if(DEBUG){
                println("Shutdown already in progress");
                println("}");
            }
            return;
        }
        // Initiate the shutdown process
        shutdown = true;
        closed = true;
        // We don't need it anymore
        asynchException = null;
        engine.closeOutbound();
        if(outData.hasRemaining()){
            // If this method is called after an exception, we should
            // close the socket regardless having some data to send.
            assert channelWriteInterestSet : "Data to be sent but no write interest.";
            if(DEBUG){
                println("there is some data left to be sent. Waiting: " + outData);
                println("}");
            }
            // We are waiting to send the data
            return;
        }else
            doShutdown();
        if(DEBUG)
            println("}");
    }

    /*-------------------------------------------------[ Exception Handling ]---------------------------------------------------*/

    /**
     * If an error occurs while processing a callback from the selector
     * thread, the exception is saved in this field to be thrown to the
     * application the next time it calls a public method of this class.
     */
    private IOException asynchException = null;

    private void handleAsynchException(IOException e){
        if(DEBUG)
            println("encountered async exception: "+e.getMessage());
        // Will be sent back to the application next time a public
        // method is called
        asynchException = e;

        // If the application has any interest set, fire an event.
        // Otherwise, the event will be fired next time a public method
        // is called.
        if(appWriteWait)
            enableAppWrite();
        if(appReadWait)
            enableAppRead();

        // We won't be sending any more data.
        engine.closeOutbound();
    }
}
