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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Santhosh Kumar T
 */
public class ChunkedInputChannel extends FilterInputChannel{
    private int chunkLength = -1;
    private FixedLengthInputChannel fixedLengthInputChannel;
    private boolean crlfRead = true;

    public ChunkedInputChannel(InputChannel delegate){
        super(delegate);
    }

    @Override
    protected boolean activateInterest(){
        return super.activateInterest() && !(fixedLengthInputChannel==null && crlfRead && chunkLength==0);
    }

    private ByteBuffer crlfBuffer = ByteBuffer.allocate(2);
    private ByteBuffer lenBuffer;
    @Override
    protected int doRead(ByteBuffer dst) throws IOException{
        int pos = dst.position();
        do{
            if(fixedLengthInputChannel==null){
                if(!crlfRead){
                    if(delegate.read(crlfBuffer)==-1)
                        return -1;

                    if(crlfBuffer.hasRemaining())
                        return 0;
                    crlfRead = true;
                    crlfBuffer.clear();
                }
                if(chunkLength==0)
                    return -1;
                if(lenBuffer==null)
                    lenBuffer = ByteBuffer.allocate(100);
                if(delegate.read(lenBuffer)==-1)
                    return -1;
                String chunkStr = getChunkLength();
                if(chunkStr==null){
                    if(!lenBuffer.hasRemaining())
                        lenBuffer = ByteBuffer.wrap(Arrays.copyOf(lenBuffer.array(), lenBuffer.capacity()+100), lenBuffer.position(), 100);
                    return 0;
                }
                int semicolon = chunkStr.indexOf(';');
                String lenStr = semicolon==-1 ? chunkStr : chunkStr.substring(0, semicolon);
                chunkLength = Integer.parseInt(lenStr, 16);
                if(listener!=null){
                    listener.onChunk(chunkLength, semicolon==-1?null:chunkStr.substring(semicolon+1));
                }
                crlfRead = false;
                if(chunkLength>0){
                    IOChannelHandler handler = (IOChannelHandler)client.attachment();
                    InputChannel oldInput = handler.input;
                    fixedLengthInputChannel = new FixedLengthInputChannel(delegate, chunkLength);
                    handler.input = oldInput;
                }
                if(lenBuffer.hasRemaining()){
                    delegate.unread(lenBuffer.array(), lenBuffer.position(), lenBuffer.remaining(), false);
                    lenBuffer = null;
                }else
                    lenBuffer.clear();
            }
            int read = fixedLengthInputChannel==null ? -1 : fixedLengthInputChannel.read(dst);
            if(read==-1)
                fixedLengthInputChannel = null;
        }while(fixedLengthInputChannel==null);

        return dst.position()-pos;
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
        return super.pending() + (fixedLengthInputChannel!=null ? fixedLengthInputChannel.pending() : 0);
    }

    private Listener listener;

    public Listener getListener(){
        return listener;
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    interface Listener{
        public void onChunk(int len, String extension);
    }
}
