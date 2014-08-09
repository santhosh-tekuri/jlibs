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

package jlibs.nio.channels.impl.filters;

import jlibs.nio.channels.impl.SelectableChannel;

import java.io.IOException;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

/**
 * @author Santhosh Kumar Tekuri
 */
public interface FilterChannel extends SelectableChannel{
    public int selfInterestOps();
    public default void dispose(){}

    public interface Helper{
        public int peerReadyOps();
        public void doProcess();
        public void addPeerInterests(int ops);
        public default int doProcess(FilterChannel filter, int peerInterestOps) throws IOException{
            int peerReadyOps = peerReadyOps();

            int selfInterestOpsBefore = filter.selfInterestOps();
            if(selfInterestOpsBefore!=0)
                doProcess();
            int selfInterestOps = filter.selfInterestOps();

            int readyOps = 0;
            if(filter.isOpen()){
                if(selfInterestOps==0){
                    int interestOps = filter.interestOps();
                    int selfReadyOps = filter.selfReadyOps();
                    if((interestOps&OP_READ)!=0){
                        if((selfReadyOps&OP_READ)!=0)
                            readyOps |= OP_READ;
                        else if((peerInterestOps&OP_READ)!=0)
                            readyOps |= peerReadyOps&OP_READ;
                    }
                    if((interestOps&OP_WRITE)!=0){
                        if((selfReadyOps&OP_WRITE)!=0)
                            readyOps |= OP_WRITE;
                        else if((peerInterestOps&OP_WRITE)!=0)
                            readyOps |= peerReadyOps&OP_WRITE;
                    }
                    interestOps &= ~readyOps;
                    if(selfInterestOpsBefore!=0 && interestOps!=0)
                        addPeerInterests(interestOps); // push app interests to peer
                }else
                    addPeerInterests(selfInterestOps);
            }

            return readyOps;
        }
    }
}
