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

package jlibs.nio.http.filters;

import jlibs.nio.http.FilterType;
import jlibs.nio.http.ServerExchange;
import jlibs.nio.http.ServerFilter;
import jlibs.nio.http.msg.EncodablePayload;
import jlibs.nio.http.msg.Method;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Response;
import jlibs.nio.http.util.USAscii;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Santhosh Kumar Tekuri
 *
 * Server Request Filter
 */
public class SendTrace implements ServerFilter{
    @Override
    public boolean filter(ServerExchange exchange, FilterType type) throws Exception{
        assert type==FilterType.REQUEST;
        Request request = exchange.getRequest();
        if(request.method==Method.TRACE){
            Response response = new Response();
            response.setPayload(new TracePayload(request));
            exchange.setResponse(response);
        }
        return true;
    }

    public static class TracePayload extends EncodablePayload{
        private Request request;
        public TracePayload(Request request){
            super("message/http");
            this.request = request;
        }

        @Override
        public void writeTo(OutputStream out) throws IOException{
            out.write(USAscii.toBytes(request.toString()));
        }
    }
}
