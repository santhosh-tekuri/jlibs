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

package jlibs.nbp;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.CharacterCodingException;

/**
 * @author Santhosh Kumar T
 */
public class Feeder{
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    
    public final NBParser parser;
    protected ReadableCharChannel channel;
    protected final CharBuffer charBuffer = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);

    public Feeder(NBParser parser, ReadableCharChannel channel){
        this(parser);
        this.channel = channel;
    }

    protected Feeder(NBParser parser){
        this.parser = parser;
    }

    public ReadableCharChannel channel(){
        return channel;
    }

    public final void setChannel(ReadableCharChannel channel){
        readMore = true;
        this.channel = channel;
        child = null;
        charBuffer.clear();
    }

    public final ReadableByteChannel byteChannel(){
        return channel instanceof NBChannel ? ((NBChannel)channel).getChannel() : null;
    }

    protected Feeder child;
    private Feeder parent;
    public final void setChild(Feeder child){
        this.child = child;
        child.parent = this;
        parser.stop = true;
    }

    public final Feeder getParent(){
        return parent;
    }

    /*-------------------------------------------------[ Eating ]---------------------------------------------------*/

    public final Feeder feed() throws IOException{
        try{
            Feeder current = this;
            Feeder next;
            do{
                next = current.read();
                if(next==current)
                    return current;
                current = next;
            }while(current!=null);
        }catch(CharacterCodingException ex){
            throw parser.ioError(ex.getClass().getSimpleName()+": "+ex.getMessage());
        }
        return null;
    }

    private boolean readMore = true;
    protected Feeder read() throws IOException{
        final char[] chars = charBuffer.array();
        if(channel!=null){
            if(!readMore){
                charBuffer.position(parser.consume(chars, charBuffer.position(), charBuffer.limit(), false));
                if(child!=null)
                    return child;
                else{
                    charBuffer.compact();
                    readMore = true;
                }
            }

            int read;
            while((read=channel.read(charBuffer))>0){
                charBuffer.flip();
                charBuffer.position(parser.consume(chars, charBuffer.position(), charBuffer.limit(), false));
                if(child!=null){
                    readMore = false;
                    return child;
                }
                charBuffer.compact();
            }
            if(read==-1){
                charBuffer.flip();
                channel.close();
                channel = null;
            }
        }
        if(channel==null){
            boolean canSendEOF = parent==null || this.parser!=parent.parser;
            charBuffer.position(parser.consume(chars, charBuffer.position(), charBuffer.limit(), canSendEOF));
            if(child!=null)
                return child;
            
            if(!canSendEOF && charBuffer.hasRemaining())
                    throw new IOException("NotImplemented: remaining "+charBuffer.position());

            if(parent!=null)
                parent.child = null;
            return parent;
        }else
            return this;
    }
}
