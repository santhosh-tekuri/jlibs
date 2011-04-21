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

package jlibs.core.nio.channels;

import jlibs.core.io.IOUtil;
import jlibs.core.lang.ByteSequence;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Santhosh Kumar T
 */
public class ChunkedInputChannel extends FilterInputChannel{
    public ChunkedInputChannel(InputChannel delegate){
        super(delegate);
    }

    @Override
    protected boolean activateInterest(){
        return super.activateInterest() && state!=FINISHED;
    }

    private static final int CHUNK_START = 0;
    private static final int CHUNK_CONTENT = 1;
    private static final int CHUNK_END = 2;
    private static final int FINISHED = 3;

    private int state = CHUNK_START;
    private int chunkLength = -1;
    private InputChannel contentInputChannel;

    private ByteBuffer crlfBuffer = ByteBuffer.allocate(2);
    private ByteBuffer lenBuffer, trailerBuffer;

    @Override
    protected int doRead(ByteBuffer dst) throws IOException{
        int pos = dst.position();

        loop:
        while(state!=FINISHED){
            switch(state){
                case CHUNK_START:
                    if(lenBuffer==null)
                        lenBuffer = ByteBuffer.allocate(100);
                    if(delegate.read(lenBuffer)<=0)
                        break loop;
                    String chunkStr = getChunkLength();
                    if(chunkStr==null){
                        if(!lenBuffer.hasRemaining()){
                            lenBuffer = ByteBuffer.wrap(Arrays.copyOf(lenBuffer.array(), lenBuffer.capacity()+100), lenBuffer.position(), 100);
                            break;
                        }
                        break loop;
                    }
                    int semicolon = chunkStr.indexOf(';');
                    String lenStr = semicolon==-1 ? chunkStr : chunkStr.substring(0, semicolon);
                    chunkLength = Integer.parseInt(lenStr, 16);
                    if(listener!=null)
                        listener.onChunk(chunkLength, semicolon==-1?null:chunkStr.substring(semicolon+1));
                    if(chunkLength>0){
                        InputChannel oldInput = clientHandler().input;
                        contentInputChannel = new FixedLengthInputChannel(delegate, chunkLength);
                        clientHandler().input = oldInput;
                    }
                    if(lenBuffer.hasRemaining()){
                        delegate.unread(lenBuffer.array(), lenBuffer.position(), lenBuffer.remaining(), false);
                        lenBuffer = null;
                    }else
                        lenBuffer.clear();
                    if(contentInputChannel==null){
                        state = CHUNK_END;
                        break;
                    }else
                        state = CHUNK_CONTENT;
                case CHUNK_CONTENT:
                    if(chunkLength==0){
                        if(trailerBuffer==null)
                            trailerBuffer = ByteBuffer.allocate(250);
                        while(contentInputChannel.read(trailerBuffer)>0)
                            trailerBuffer = ByteBuffer.wrap(Arrays.copyOf(trailerBuffer.array(), trailerBuffer.capacity()+100), trailerBuffer.position(), 100);
                        if(contentInputChannel.isEOF()){
                            contentInputChannel = null;
                            if(listener!=null)
                                listener.onTrailer(new ByteSequence(trailerBuffer.array(), 0, trailerBuffer.position()));
                            trailerBuffer = null;
                            state = FINISHED;
                        }
                        break loop;
                    }else{
                        while(contentInputChannel.read(dst)>0);
                        if(contentInputChannel.isEOF()){
                            contentInputChannel = null;
                            state = CHUNK_END;
                        }else
                            break loop;
                    }
                case CHUNK_END:
                    if(delegate.read(crlfBuffer)==-1)
                        break loop;
                    if(crlfBuffer.hasRemaining())
                        break loop;
                    if(crlfBuffer.array()[0]!='\r' || crlfBuffer.array()[1]!='\n'){
                        if(chunkLength>0)
                            throw new IOException("chunk should end with '\\r\\n'");
                        else{
                            delegate.unread(crlfBuffer.array(), 0, 2, false);
                            contentInputChannel = new PatternInputChannel(delegate, new BytePattern(new byte[]{ '\r', '\n', '\r', '\n' }));
                            state = CHUNK_CONTENT;
                            break;
                        }
                    }else{
                        if(chunkLength==0){
                            state = FINISHED;
                            break loop;
                        }else{
                            crlfBuffer.clear();
                            state = CHUNK_START;
                        }
                    }
            }
        }

        int read = dst.position()-pos;
        if(read==0){
            if(state==FINISHED)
                return -1;
            else if(delegate.isEOF()){
                if(state==CHUNK_START || state==CHUNK_END)
                    return -1;
            }else
                return 0;
            return state==FINISHED ? -1 : 0;
        }else
            return read;
    }

    private String getChunkLength(){
        assert lenBuffer.arrayOffset()==0;
        byte array[] = lenBuffer.array();
        for(int i=0; i<lenBuffer.position()-1; i++){
            if(array[i]=='\r' && array[i+1]=='\n'){
                String str = new String(array, 0, i, IOUtil.US_ASCII);
                lenBuffer.flip();
                lenBuffer.position(i+2);
                return str;
            }
        }
        return null;
    }

    @Override
    public long pending(){
        return super.pending() + (contentInputChannel !=null ? contentInputChannel.pending() : 0);
    }

    private Listener listener;

    public Listener getListener(){
        return listener;
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public interface Listener{
        public void onChunk(int len, String extension);
        public void onTrailer(ByteSequence seq);
    }
}
