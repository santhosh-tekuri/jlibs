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
import java.net.DatagramSocket;
import java.nio.channels.DatagramChannel;

/**
 * @author Santhosh Kumar Tekuri
 */
public class UDPConnection extends Connection<DatagramChannel>{
    public UDPConnection(DatagramChannel selectable) throws IOException{
        super(selectable, null);
    }

    protected void init() throws IOException{
        DatagramSocket socket = selectable.socket();
        if(SO_SNDBUF!=null)
            socket.setSendBufferSize(SO_SNDBUF);
        if(SO_RCVBUF!=null)
            socket.setReceiveBufferSize(SO_RCVBUF);
    }

    /*-------------------------------------------------[ Options ]---------------------------------------------------*/

    public static Integer SO_RCVBUF;
    public static Integer SO_SNDBUF;
}
