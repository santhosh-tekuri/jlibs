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

import jlibs.nio.http.msg.FilePayload;

import java.io.IOException;

import static jlibs.nio.http.WriteFilePayload.State.SETUP;
import static jlibs.nio.http.WriteFilePayload.State.TRANSFER_FILE;

/**
 * @author Santhosh Kumar Tekuri
 */
public class WriteFilePayload extends WritePayload{
    private final FilePayload filePayload;

    public WriteFilePayload(FilePayload filePayload){
        this.filePayload = filePayload;
    }

    enum State{ SETUP, TRANSFER_FILE }
    private State state = SETUP;

    @Override
    protected boolean process(int readyOp) throws IOException{
        while(true){
            switch(state){
                case SETUP:
                    setup();
                    prepareTransferFromFile(filePayload.file);
                    state = TRANSFER_FILE;
                case TRANSFER_FILE:
                    return transferFromFile();
            }
        }
    }
}
