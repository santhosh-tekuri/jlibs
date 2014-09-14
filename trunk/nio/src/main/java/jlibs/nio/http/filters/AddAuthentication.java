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

import jlibs.nio.http.ClientExchange;
import jlibs.nio.http.ClientFilter;
import jlibs.nio.http.FilterType;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Response;
import jlibs.nio.http.msg.Status;
import jlibs.nio.http.util.*;

import java.security.MessageDigest;
import java.util.concurrent.ThreadLocalRandom;

import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static jlibs.core.io.IOUtil.UTF_8;

/**
 * @author Santhosh Kumar Tekuri
 *
 * Client Response Filter
 */
public class AddAuthentication implements ClientFilter{
    private boolean proxy;
    private Status status;
    private BasicCredentials basicCredentials;

    public AddAuthentication(String username, String password, boolean proxy){
        basicCredentials = new BasicCredentials(username, password);
        this.proxy = proxy;
        status = proxy ? Status.PROXY_AUTHENTICATION_REQUIRED : Status.UNAUTHORIZED;
    }

    public AddAuthentication(String username, String password){
        this(username, password, false);
    }

    @Override
    public boolean filter(ClientExchange exchange, FilterType type) throws Exception{
        assert type==FilterType.RESPONSE;
        Response response = exchange.getResponse();
        if(!status.equals(response.status))
            return true;

        Request request = exchange.getRequest();
        if(request.getCredentials(proxy)!=null) // we already tried to authenticate && failed
            return true;

        Challenge challenge = response.getChallenge(proxy);
        if(challenge instanceof BasicChallenge){
            request.setCredentials(basicCredentials, proxy);
            exchange.retry();
            return true;
        }

        if(challenge instanceof DigestChallenge){
            DigestChallenge digestChallenge = (DigestChallenge)challenge;

            DigestCredentials digestCredentials = new DigestCredentials();
            digestCredentials.username = basicCredentials.user;
            digestCredentials.realm = digestChallenge.realm;
            digestCredentials.nonce = digestChallenge.nonce;

            digestCredentials.uri = request.uri;

            digestCredentials.algorithm = digestChallenge.algorithm;
            digestCredentials.opaque = digestChallenge.opaque;
            if(digestChallenge.qops!=null && !digestChallenge.qops.isEmpty())
                digestCredentials.qop = digestChallenge.qops.contains("auth") ? "auth" : digestChallenge.qops.get(0);

            if("MD5-sess".equals(digestCredentials.algorithm) || digestCredentials.qop!=null){
                byte[] bytes = new byte[4];
                ThreadLocalRandom.current().nextBytes(bytes);
                digestCredentials.cnonce = printHexBinary(bytes).toLowerCase();
            }

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            String a1 = basicCredentials.user+':'+digestChallenge.realm+':'+basicCredentials.password;
            String ha1 = printHexBinary(md5.digest(a1.getBytes(UTF_8))).toLowerCase();
            md5.reset();

            if("MD5-sess".equals(digestCredentials.algorithm)){
                ha1 = printHexBinary(md5.digest((ha1+':'+digestCredentials.nonce+':'+digestCredentials.cnonce).getBytes(UTF_8))).toLowerCase();
                md5.reset();
            }

            md5.update((request.method+":"+request.uri).getBytes(UTF_8));
            if("auth-init".equals(digestCredentials.qop)){
                // ha2 = md5(method:uri:md5(payload)
                if(request.getPayload().getContentLength()==0)
                    md5.update((byte)':');
                else{
                    // not implemented
                    return true;
                }
            }
            String ha2 = printHexBinary(md5.digest()).toLowerCase();
            md5.reset();

            String respString = ha1+':'+digestChallenge.nonce;
            if(digestCredentials.qop!=null){
                digestCredentials.nc = String.format("%08x", 1);
                respString += ':'+digestCredentials.nc+':'+digestCredentials.cnonce+':'+digestCredentials.qop;
            }
            respString += ':'+ha2;
            digestCredentials.response = printHexBinary(md5.digest(respString.getBytes(UTF_8))).toLowerCase();

            request.setCredentials(digestCredentials, proxy);
            exchange.retry();
            return true;
        }

        return true; // we dont understand the challenge
    }
}