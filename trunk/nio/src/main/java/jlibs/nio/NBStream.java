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

package jlibs.nio;

import jlibs.nio.listeners.ShutdownChannel;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class NBStream<T extends SelectableChannel> extends NBChannel<T>{
    protected final SelectionKey selectionKey;
    protected final Socket transport;

    public NBStream(T selectable, SelectionKey selectionKey) throws IOException{
        super(selectable);
        if(selectable==null){
            transport = null;
            this.selectionKey = null;
        }else{
            if(selectionKey==null)
                selectionKey = selectable.register(reactor.selector, 0, this);
            else
                selectionKey.attach(this);
            this.selectionKey = selectionKey;
            transport = new Socket(this, selectionKey);
            selectionKey.attach(this);
        }
        init();
    }

    protected void init() throws IOException{}

    @Override
    protected void process(boolean timeout){
        transport.process(timeout);
    }

    NBStream wakeupNext;

    void wakeup(){
        reactor.wakeup(this);
    }

    void wakeupNow(){
        transport.wakeupNow();
    }

    void closing(){}

    @Override
    public void close(){
        if(isOpen())
            ShutdownChannel.start(this);
    }

    @Override
    public long getTimeout(){
        return SO_TIMEOUT;
    }

    /*-------------------------------------------------[ Options ]---------------------------------------------------*/

    public static long SO_TIMEOUT;
}
