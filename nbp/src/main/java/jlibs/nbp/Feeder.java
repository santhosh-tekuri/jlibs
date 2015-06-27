/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
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

    private void closeAll(){
        Feeder feeder = this;
        while(feeder.parent!=null)
            feeder = feeder.parent;
        while(feeder!=null){
            try{
                if(feeder.channel!=null)
                    feeder.channel.close();
            }catch(IOException ignore){
                // ignore
            }
            feeder = feeder.child;
        }
    }

    public final Feeder feed() throws IOException{
        try{
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
        }catch(Throwable thr){
            closeAll();
            if(thr instanceof RuntimeException)
                throw (RuntimeException)thr;
            else if(thr instanceof Error)
                throw (Error)thr;
            else
                throw (IOException)thr;
        }
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
