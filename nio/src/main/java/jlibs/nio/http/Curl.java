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

import jlibs.core.io.FileUtil;
import jlibs.nio.Reactors;
import jlibs.nio.TCPEndpoint;
import jlibs.nio.http.filters.AddAuthentication;
import jlibs.nio.http.filters.FollowRedirects;
import jlibs.nio.http.msg.*;
import jlibs.nio.http.util.MediaType;
import jlibs.nio.listeners.IOListener;
import jlibs.nio.listeners.WriteToOutputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Curl implements ResponseListener, ClientCallback{
    private BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
    private HTTPClient client;

    public Curl(HTTPClient client){
        this.client = client;
    }

    public void run(){
        ClientExchange exchange = null;
        String payload = null;
        String payloadMarker = null;
        while(true){
            try{
                if(exchange==null)
                    System.out.print("\ncurl> ");
                String line = console.readLine();
                if(line==null)
                    return;
                if(exchange==null){
                    Method method = Method.GET;
                    int space = line.indexOf(' ');
                    if(space!=-1){
                        method = Method.valueOf(line.substring(0, space));
                        line = line.substring(space+1);
                    }
                    if(!line.contains("://"))
                        line = "http://"+line;
                    exchange = client.newExchange(line);
                    exchange.setCallback(this);
                    exchange.getRequest().method = method;
                }else if(line.isEmpty()){
                    if(payload==null){
                        MediaType mt = exchange.getRequest().getMediaType();
                        if(mt!=null)
                            payload = "";
                    }
                    if(payload==null){
                        exchange.execute(this);
                        return;
                    }
                }else if(payload==null){
                    int colon = line.indexOf(':');
                    AsciiString name = AsciiString.valueOf(line.substring(0, colon));
                    String value = line.substring(colon+1).trim();
                    exchange.getRequest().headers.add(name, value);
                }else if(payloadMarker==null){
                    if(line.startsWith("<<"))
                        payloadMarker = line.substring(2);
                }else{
                    if(line.equals(payloadMarker)){
                        MediaType mt = exchange.getRequest().getMediaType();
                        exchange.getRequest().setPayload(new StringPayload(payload, mt.toString()));
                        exchange.execute(this);
                        return;
                    }
                    payload += FileUtil.LINE_SEPARATOR+line;
                }
            }catch(Throwable thr){
                thr.printStackTrace();
            }
        }
    }

    @Override
    public void process(ClientExchange exchange, Throwable thr) throws Exception{
        if(thr==null){
            Response response = exchange.getResponse();
            System.out.print(response);
            Payload payload = response.getPayload();
            if(payload.getContentLength()!=0){
                SocketPayload socketPayload = (SocketPayload)payload;
                new IOListener().start(new WriteToOutputStream(System.out, null), socketPayload.socket(), null);
            }
        }
    }

    @Override
    public void completed(ClientExchange exchange, Throwable thr){
        System.out.flush();
        if(thr!=null)
            thr.printStackTrace();
        run();
    }

    public static void main(String[] args) throws IOException{
        Reactors.start(1);
        Reactors.get().get(0).invokeLater(() -> {
            HTTPClient client = new HTTPClient();
            client.proxy = new HTTPProxy(new TCPEndpoint("localhost", 8080));

            List<ClientFilter> requestFilters = new ArrayList<>();
//            requestFilters.add(new AddBasicAuthentication(new BasicCredentials("user", "passwd")));
            client.requestFilters = requestFilters;

            List<ClientFilter> responseFilters = new ArrayList<>();
            responseFilters.add(new FollowRedirects());
            responseFilters.add(new AddAuthentication("user", "passwd"));
            client.responseFilters = responseFilters;

            new Curl(client).run();
        });
    }
}
