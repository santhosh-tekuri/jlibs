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

package jlibs.nio.http;

import jlibs.nio.util.Buffers;

import java.io.IOException;

import static java.nio.channels.SelectionKey.OP_READ;
import static jlibs.nio.http.WriteSocketPayload.State.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public class WriteSocketPayload extends WritePayload{
    private SocketPayload payload;
    public WriteSocketPayload(SocketPayload payload){
        this.payload = payload;
    }

    enum State{ SETUP, WRITE_BUFFERS, PREPARE_PUMP, DO_PUMP }
    private State state = SETUP;

    @Override
    protected boolean process(int readyOp) throws IOException{
        while(true){
            switch(state){
                case SETUP:
                    super.setup();
                    assert payload.in.isOpen();
                    if(ignoreBuffers || payload.buffers==null){
                        state = PREPARE_PUMP;
                        break;
                    }else{
                        prepareFlush(payload.buffers, payload.retain);
                        state = WRITE_BUFFERS;
                    }
                case WRITE_BUFFERS:
                    if(!flushBuffers())
                        return false;
                    state = PREPARE_PUMP;
                case PREPARE_PUMP:
                    switchIO(payload.in, out);
                    Buffers backup = null;
                    if(payload.retain){
                        if(payload.buffers==null)
                            payload.buffers = backup = new Buffers();
                        else
                            backup = payload.buffers;
                    }
                    preparePump(backup);
                    readyOp = OP_READ;
                    state = DO_PUMP;
                case DO_PUMP:
                    return doPump(readyOp, false);
            }
        }
    }

    @Override
    protected void cleanup(Throwable thr){
        revertIO();
    }

    boolean ignoreBuffers;

    @Override
    public String toString(){
        return "WriteSocketPayload";
    }
}
