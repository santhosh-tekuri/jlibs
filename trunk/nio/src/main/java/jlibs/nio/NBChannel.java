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

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class NBChannel<T extends SelectableChannel> implements Channel{
    protected String uniqueID;
    public final Reactor reactor;
    public final T selectable;
    public NBChannel(T selectable) throws IOException{
        this.selectable = selectable;
        reactor = Reactor.current();
        if(selectable!=null)
            selectable.configureBlocking(false);
        workingFor = reactor==null ? this : reactor.getExecutionOwner();
        if(workingFor==null)
            workingFor = this;
        uniqueID = getClass().getSimpleName()+'@'+Integer.toHexString(hashCode());
        makeActive();
    }

    protected abstract void process(boolean timeout);

    int heapIndex = -1;
    long timeoutAt = Long.MAX_VALUE;
    public long getTimeout(){
        return 0;
    }

    @Override
    public boolean isOpen(){
        return selectable.isOpen();
    }

    @Override
    public void close(){
        shutdown();
    }

    public void shutdown(){
        try{
            selectable.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public void makeActive(){
        if(reactor!=null)
            reactor.activeChannel = this;
    }

    NBChannel workingFor;
    int taskID = 1;
    public void taskCompleted(){
        ++taskID;
        executionID = null;
    }

    String executionID;
    public String getExecutionID(){
        if(executionID==null){
            executionID = reactor==null ? "" : reactor.executionID+"/";
            executionID += workingFor.uniqueID+'.'+workingFor.taskID;
            if(workingFor!=this)
                executionID += '/'+uniqueID+'.'+taskID;
        }
        return executionID;
    }
}
