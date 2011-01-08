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

package jlibs.xml.sax.async;

import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;

/**
 * @author Santhosh Kumar T
 */
public class ChannelInputSource extends InputSource{
    public ChannelInputSource(){}

    public ChannelInputSource(String systemId){
        super(systemId);
    }

    public ChannelInputSource(InputStream byteStream){
        super(byteStream);
    }

    public ChannelInputSource(Reader characterStream){
        super(characterStream);
    }

    public ChannelInputSource(ReadableByteChannel channel){
        setChannel(channel);
    }

    private ReadableByteChannel channel;

    public ReadableByteChannel getChannel(){
        return channel;
    }

    public void setChannel(ReadableByteChannel channel){
        this.channel = channel;
    }
}
