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
import java.nio.charset.CharacterCodingException;

/**
 * @author Santhosh Kumar T
 */
public class Feeder{
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    
    public final NBParser parser;
    protected ReadableCharChannel channel;

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

    protected Feeder child;
    private Feeder parent;
    public void setChild(Feeder child){
        this.child = child;
        child.parent = this;
        parser.stop = true;
    }

    public Feeder getParent(){
        return parent;
    }

    protected boolean canClose(){
        return parent==null || this.parser!=parent.parser;
    }

    /*-------------------------------------------------[ CharBuffer ]---------------------------------------------------*/
    
    protected CharBuffer charBuffer = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
    protected void feedCharBuffer() throws IOException{
        charBuffer.position(parser.consume(charBuffer.array(), charBuffer.position(), charBuffer.limit()));
    }

    /*-------------------------------------------------[ Eating ]---------------------------------------------------*/

    protected Feeder parent(){
        if(parent!=null)
            parent.child = null;
        return parent;
    }

    public Feeder feed() throws IOException{
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
            parser.ioError(ex.getClass().getSimpleName()+": "+ex.getMessage());
        }
        return null;
    }

    protected boolean eofSent;
    protected Feeder read() throws IOException{
        if(charBuffer.position()>0){
            charBuffer.flip();
            feedCharBuffer();
            charBuffer.compact();
            if(child!=null)
                return child;
        }

        int read = eofSent ? -1 : 0;
        try{
            if(!eofSent){
                while((read=channel.read(charBuffer))>0){
                    charBuffer.flip();
                    feedCharBuffer();
                    charBuffer.compact();
                    if(child!=null)
                        return child;
                }
                if(read==-1 && canClose()){
                    eofSent = true;
                    parser.eof();
                    if(child!=null)
                        return child;
                }
            }
            return read==-1 ? parent() : this;
        }finally{
            try{
                if(child==null && read==-1){
                    if(canClose())
                        parser.reset();
                    channel.close();
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
