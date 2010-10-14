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
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * @author Santhosh Kumar T
 */
public class Feeder{
    public final NBParser parser;
    private Object source;

    public Feeder(NBParser parser, Object source){
        this(parser);
        setSource(source);
    }

    protected Feeder(NBParser parser){
        this.parser = parser;
    }

    protected void setSource(Object source){
        this.source = source;
        if(!(source instanceof Reader))
            byteBuffer = ByteBuffer.allocate(500);
    }

    public Object getSource(){
        return source;
    }

    private Feeder child;
    private Feeder parent;
    public void setChild(Feeder child){
        this.child = child;
        child.parent = this;
    }

    public Feeder getParent(){
        return parent;
    }

    private boolean canClose(){
        return parent==null || this.parser!=parent.parser;
    }

    /*-------------------------------------------------[ CharBuffer ]---------------------------------------------------*/
    
    private CharBuffer charBuffer = CharBuffer.allocate(100);
    private void feedCharBuffer() throws IOException{
        char chars[] = charBuffer.array();
        int position = charBuffer.position();
        int limit = charBuffer.limit();
        while(position<limit){
            parser.consume(chars[position]);
            position++;
            if(child!=null)
                break;                
        }
        charBuffer.position(position);
    }

    /*-------------------------------------------------[ ByteBuffer ]---------------------------------------------------*/

    protected CharsetDecoder decoder = Charset.defaultCharset().newDecoder();

    public Charset getCharset(){
        return decoder.charset();
    }

    public void setCharset(Charset charset){
        decoder = charset.newDecoder();
    }

    protected ByteBuffer byteBuffer;
    protected void feedByteBuffer(boolean eof) throws IOException{
        while(true){
            CoderResult cr = byteBuffer.hasRemaining() ? decoder.decode(byteBuffer, charBuffer, eof) : CoderResult.UNDERFLOW;
            charBuffer.flip();
            feedCharBuffer();
            charBuffer.compact();
            if(child!=null)
                break;
            if(cr.isUnderflow())
                break;
            else if(!cr.isOverflow())
                parser.encodingError(cr);
        }
    }

    /*-------------------------------------------------[ Eating ]---------------------------------------------------*/

    private Feeder parent(){
        if(parent!=null)
            parent.child = null;
        return parent;
    }

    public Feeder feed() throws IOException{
        Feeder current = this;
        Feeder next;
        do{
            next = feed(current);
            if(next==current)
                return current;
            current = next;
        }while(current!=null);

        return null;
    }

    protected Feeder feed(Feeder current) throws IOException{
        if(current.source instanceof Reader)
            return current.feed((Reader)current.source);
        else if(current.source instanceof InputStream)
            return current.feed((InputStream)current.source);
        else if(current.source instanceof ReadableByteChannel)
            return current.feed((ReadableByteChannel)current.source);
        else
            throw new IOException("Invalid Source: "+current.source.getClass());
    }
    
    private boolean eofSent;
    private Feeder feed(Reader reader) throws IOException{
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
                while((read=reader.read(charBuffer.array(), charBuffer.position(), charBuffer.remaining()))>0){
                    charBuffer.position(read);
                    charBuffer.flip();
                    feedCharBuffer();
                    charBuffer.compact();
                    if(child!=null)
                        return child;
                }
                if(read==-1 && canClose()){
                    eofSent = true;
                    parser.consume(-1);
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
                    reader.close();
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private Feeder feed(InputStream stream) throws IOException{
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
                while((read=stream.read(byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining()))>0){
                    byteBuffer.position(read);
                    byteBuffer.flip();
                    feedByteBuffer(false);
                    byteBuffer.compact();
                    if(child!=null)
                        return child;
                }
                if(read==-1){
                    byteBuffer.flip();
                    feedByteBuffer(true);
                    byteBuffer.compact();
                }
                if(child!=null)
                    return child;
                if(read==-1 && canClose()){
                    eofSent = true;
                    parser.consume(-1);
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
                    stream.close();
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private Feeder feed(ReadableByteChannel channel) throws IOException{
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
                while((read=channel.read(byteBuffer))>0){
                    byteBuffer.flip();
                    feedByteBuffer(false);
                    byteBuffer.compact();
                    if(child!=null)
                        return child;
                }
                if(read==-1){
                    byteBuffer.flip();
                    feedByteBuffer(true);
                    byteBuffer.compact();
                }
                if(child!=null)
                    return child;

                if(read==-1 && canClose()){
                    eofSent = true;
                    parser.consume(-1);
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
