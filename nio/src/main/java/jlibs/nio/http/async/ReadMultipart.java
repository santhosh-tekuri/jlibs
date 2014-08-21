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

package jlibs.nio.http.async;

import jlibs.nio.Client;
import jlibs.nio.async.CloseInput;
import jlibs.nio.async.ExecutionContext;
import jlibs.nio.async.ReadBytes;
import jlibs.nio.async.ReadLines;
import jlibs.nio.channels.InputChannel;
import jlibs.nio.channels.filters.BoundaryInputFilter;
import jlibs.nio.http.msg.Multipart;
import jlibs.nio.util.BytePattern;
import jlibs.nio.util.Bytes;
import jlibs.nio.util.Line;

import java.io.IOException;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ReadMultipart{
    public final Multipart multipart;
    private BytePattern pattern;
    public ReadMultipart(Multipart multipart, String boundary){
        this.multipart = multipart;
        pattern = BoundaryInputFilter.createPattern(boundary);
    }

    private Client client;
    private ExecutionContext context;
    private Bytes bytes;
    public void start(InputChannel in, ExecutionContext context){
        client = in.getClient();
        this.context = context;
        try{
            client.inPipeline.push(new BoundaryInputFilter(pattern, true));
        }catch(IOException ex){
            resume(ex, false);
            return;
        }
        bytes = new Bytes();
        new ReadBytes(bytes).start(client.in(), this::readPartHeaders);
    }

    private void resume(Throwable thr, boolean timeout){
        if(client.in() instanceof BoundaryInputFilter)
            client.inPipeline.pop();
        CloseInput.INSTANCE.start(client.in(), ExecutionContext.doFinally(context, thr, timeout));
    }

    private Line line = new Line();
    private void readPartHeaders(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            resume(thr, timeout);
            return;
        }
        if(multipart.parts.isEmpty()){
            if(!bytes.isEmpty()){
                multipart.preamble = bytes;
                bytes = null;
            }
        }

        BoundaryInputFilter in = (BoundaryInputFilter)client.in();
        if(in.isLast()){
            client.inPipeline.pop();
            try{
                client.inPipeline.push(new BoundaryInputFilter(pattern, false));
            }catch(IOException ex){
                resume(ex, false);
                return;
            }
            bytes = new Bytes();
            new ReadBytes(bytes).start(client.in(), this::finished);
        }else{
            client.inPipeline.pop();
            try{
                client.inPipeline.push(new BoundaryInputFilter(pattern, false));
            }catch(IOException ex){
                resume(ex, false);
                return;
            }
            Multipart.Part part = new Multipart.Part();
            if(bytes==null)
                bytes = new Bytes();
            part.payload = bytes;
            bytes = null;
            multipart.parts.add(part);
            line.reset();
            new ReadLines(line, part.headers).start(client.in(), this::readPartPayload);
        }
    }

    private void readPartPayload(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            resume(thr, timeout);
            return;
        }
        Multipart.Part part = multipart.parts.get(multipart.parts.size()-1);
        new ReadBytes((Bytes)part.payload).start(client.in(), this::readPartHeaders);
    }

    private void finished(Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            resume(thr, timeout);
            return;
        }
        if(!bytes.isEmpty())
            multipart.epilogue = bytes;
        resume(null, false);
    }
}
