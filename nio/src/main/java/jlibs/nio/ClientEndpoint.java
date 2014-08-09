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

package jlibs.nio;

import jlibs.nio.async.ExecutionContext;
import jlibs.nio.async.Socks4Tunnel;
import jlibs.nio.async.Socks5Tunnel;
import jlibs.nio.channels.filters.SSLFilter;
import jlibs.nio.http.HTTPClient;
import jlibs.nio.http.HTTPException;
import jlibs.nio.http.filters.AddAuthentication;
import jlibs.nio.http.msg.Method;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Response;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.util.function.Consumer;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ClientEndpoint extends Endpoint{
    public ClientEndpoint(String host, int port){
        super(host, port);
    }

    public ClientEndpoint(InetSocketAddress address){
        super(address.getHostString(), address.getPort());
    }

    public ClientEndpoint(String url) throws GeneralSecurityException, SSLException{
        super(url);
    }

    public void borrow(Consumer<Result<Client>> listener, long timeout){
        borrow(listener, null, timeout);
    }

    public void borrow(Consumer<Result<Client>> listener, Proxy proxy, long timeout){
        Reactor reactor = Reactor.current();
        while(true){
            Client client = reactor.clientPool.remove(toString());
            if(client==null)
                break;
            if(client.isBroken()){
                if(Debugger.DEBUG)
                    Debugger.println(client+".isBroken=true");
                client.closeForcefully();
            }else{
                listener.accept(new Result<>(client));
                return;
            }
        }
        borrowNew(listener, proxy, timeout);
    }

    public void borrowNew(Consumer<Result<Client>> listener, long timeout){
        borrowNew(listener, null, timeout);
    }

    public void borrowNew(Consumer<Result<Client>> listener, Proxy proxy, long timeout){
        if(proxy!=null && proxy.type==Proxy.Type.HTTP){
            createHTTPTunneledClient(listener, proxy, timeout);
            return;
        }

        final Client client;
        try{
            client = Reactor.current().newClient();
        }catch(IOException ex){
            listener.accept(new Result<>(ex));
            return;
        }
        client.setTimeout(timeout);
        ExecutionContext context = (thr, timedout) -> notifyUser(client, listener, thr, timedout);
        if(proxy!=null && proxy.type==Proxy.Type.SOCKS){
            client.connect(proxy.endpoint.socketAddress(), (thr, timedout) -> {
                if(thr!=null || timedout)
                    notifyUser(client, listener, thr, timedout);
                else if(proxy instanceof SocksProxy && ((SocksProxy)proxy).version==4)
                    new Socks4Tunnel(this, proxy).start(client, context);
                else
                    new Socks5Tunnel(this, proxy).start(client, context);
            });
        }else
            client.connect(socketAddress(), context);
    }

    private void notifyUser(Client client, Consumer<Result<Client>> listener, Throwable thr, boolean timeout){
        if(thr!=null || timeout){
            if(timeout){
                client.closeForcefully();
                thr = new ConnectException("timeout occurred");
            }
            listener.accept(new Result<>(thr));
        }else{
            client.setTimeout(Client.Defaults.getTimeout());
            try{
                if(sslContext != null){
                    SSLEngine engine = sslContext.createSSLEngine();
                    engine.setUseClientMode(true);
                    client.pipeline.push(new SSLFilter(engine));
                }
            }catch(Throwable thr1){
                client.closeForcefully();
                listener.accept(new Result<>(thr1));
                return;
            }
            listener.accept(new Result<>(client));
        }
    }

    private void createHTTPTunneledClient(Consumer<Result<Client>> listener, Proxy proxy, long timeout){
        HTTPClient httpClient = new HTTPClient(proxy.endpoint);
        if(httpClient.proxy!=null && httpClient.proxy.endpoint.equals(proxy.endpoint))
            httpClient.proxy = null;
        httpClient.connectTimeout = timeout;
        if(proxy.user!=null)
            httpClient.responseFilters.add(new AddAuthentication(true, proxy.user, proxy.password));

        Request request = new Request();
        request.method = Method.CONNECT;
        request.uri = toString();
        HTTPClient.Task task = httpClient.newTask(request);
        task.attach(listener);
        task.finish(this::httpClientFinished);
    }

    private void httpClientFinished(HTTPClient.Task task){
        Throwable thr = null;
        if(task.isSuccess()){
            if(!task.getResponse().isSuccessful()){
                Response response = task.getResponse();
                thr = HTTPException.valueOf(response.statusCode, response.reasonPhrase, null);
            }
        }else{
            thr = task.getError();
            if(thr==null)
                thr = HTTPException.valueOf(task.getErrorCode(), null);
        }

        Consumer<Result<Client>> listener = task.attachment();
        if(thr==null)
            notifyUser(task.stealClient(), listener, null, false);
        else
            listener.accept(new Result<>(thr));
    }

    public void returnBack(Client client){
        client.reactor.clientPool.add(toString(), client);
    }

    public void returnBack(Client client, long timeout){
        client.reactor.clientPool.add(toString(), client, timeout);
    }
}
