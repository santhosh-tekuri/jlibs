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

package jlibs.nio.http.util;

import jlibs.nio.*;
import jlibs.nio.http.HTTPClient;
import jlibs.nio.http.filters.AddAuthentication;
import jlibs.nio.http.filters.AddBasicAuthentication;
import jlibs.nio.http.filters.FollowRedirects;
import jlibs.nio.http.filters.SAXParsing;
import jlibs.nio.http.msg.Method;
import jlibs.nio.http.msg.Payload;
import jlibs.nio.http.msg.Response;
import jlibs.nio.http.msg.Status;
import jlibs.nio.http.msg.spec.values.BasicChallenge;
import jlibs.nio.http.msg.spec.values.BasicCredentials;
import jlibs.nio.http.msg.spec.values.Challenge;
import jlibs.nio.http.msg.spec.values.MediaType;
import jlibs.nio.util.Bytes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import static jlibs.nio.http.msg.Headers.AUTHORIZATION;
import static jlibs.nio.http.msg.Headers.WWW_AUTHENTICATE;
import static jlibs.nio.http.msg.Method.GET;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Curl{
    private BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

    public final HTTPClient client = new HTTPClient();
    private HTTPClient.Task task;
    private Bytes bytes = null;
    private String payloadMarker;

    public void start(){
        while(true){
            try{
                if(task==null)
                    System.out.print("curl> ");
                String line = console.readLine();
                if(line==null)
                    return;

                if(task==null){
                    Method method = GET;
                    int space = line.indexOf(' ');
                    if(space!=-1){
                        method = Method.valueOf(line.substring(0, space));
                        line = line.substring(space+1);
                    }
                    if(!line.contains("://"))
                        line = "http://"+line;
                    HTTPURL url = new HTTPURL(line);
                    task = client.newTask(url.createClientEndpoint(), url.createRequest());
                    task.getRequest().method = method;
                }else{
                    if(payloadMarker!=null){
                        if(line.equals(payloadMarker)){
                            MediaType mediaType = task.getRequest().getMediaType();
                            if(mediaType==null)
                                mediaType = task.getRequest().method==Method.POST ? MediaType.APPLICATION_FORM_URLENCODED : MediaType.APPLICATION_OCTET_STREAM;
                            Payload payload = new Payload(bytes.size(), mediaType.toString(), null, null, null);
                            payload.bytes = bytes;
                            task.getRequest().setPayload(payload, true);
                            bytes = null;
                            payloadMarker = null;
                        }else{
                            bytes.append(ByteBuffer.wrap(line.getBytes()));
                            continue;
                        }
                    }else{
                        line = line.trim();
                        if(line.length()>0){
                            if(line.startsWith("$payload ")){
                                line = line.substring("$payload ".length());
                                if(line.startsWith("<<")){
                                    payloadMarker = line.substring(2);
                                    bytes = new Bytes();
                                }
                            }else{
                                int colon = line.indexOf(':');
                                task.getRequest().headers.add(line.substring(0, colon).trim(), line.substring(colon+1).trim());
                            }
                            continue;
                        }
                    }
                    executeTask();
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

    private void executeTask() throws Exception{
        synchronized(task){
            Reactors.reactor().invokeLater(() -> task.finish(this::taskFinished));
            task.wait();
        }
        task = null;
    }

    private boolean processUnauthorized(){
        try{
            if(task.getResponse().statusCode==Status.UNAUTHORIZED){
                Challenge challenge = WWW_AUTHENTICATE.get(task.getResponse());
                if(challenge instanceof BasicChallenge){
                    BasicChallenge basicChallenge = (BasicChallenge)challenge;
                    System.out.println("Authenticate for realm: "+basicChallenge.realm);
                    System.out.print("user: ");
                    String user = console.readLine();
                    if(!user.isEmpty()){
                        System.out.print("password: ");
                        String password = console.readLine();
                        AUTHORIZATION.set(task.getRequest(), new BasicCredentials(user, password));
                        task.retry(this::taskFinished);
                        return true;
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    private void resume(){
//        if(processUnauthorized())
//            return;
        synchronized(task){
            task.notify();
        }
    }
    private void taskFinished(HTTPClient.Task task){
        if(task.isSuccess()){
            Response response = task.getResponse();
            System.err.println(response.toString());
            if(processUnauthorized())
                return;
            Payload payload = response.getPayload();
            if(payload.contentLength!=0){
                try{
                    payload.removeEncodings();
                    payload.writePayloadTo(System.out, this::payloadPrinted);
                    return;
                }catch(Exception ex){
                    Reactor.current().handleException(ex);
                    try{
                        payload.close();
                    }catch(IOException e){
                        Reactor.current().handleException(e);
                    }
                }
            }
        }else{
            if(task.getError()!=null)
                Reactor.current().handleException(task.getError());
            else
                System.err.println("Error: "+Status.message(task.getErrorCode()));
        }
        resume();
    }

    private void payloadPrinted(Throwable thr, boolean timeout){
        if(thr!=null)
            Reactor.current().handleException(thr);
        else if(timeout)
            System.err.println("timeout occurred");
        resume();
    }

    public static void main(String[] args) throws Exception{
        Reactors.start(1);
        Curl curl = new Curl();
//        client.requestFilters.add(new AddBasicAuthentication(new BasicCredentials("user", "passwd")));
        curl.client.responseFilters.add(new AddAuthentication("user", "passwd"));
        curl.client.responseFilters.add(new FollowRedirects());
        curl.start();
    }
}
