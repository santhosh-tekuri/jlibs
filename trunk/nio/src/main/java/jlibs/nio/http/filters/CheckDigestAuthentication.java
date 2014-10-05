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

import jlibs.core.lang.ImpossibleException;
import jlibs.nio.http.FilterType;
import jlibs.nio.http.ServerExchange;
import jlibs.nio.http.util.Credentials;
import jlibs.nio.http.util.DigestChallenge;
import jlibs.nio.http.util.DigestCredentials;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import static javax.xml.bind.DatatypeConverter.*;
import static jlibs.core.io.IOUtil.UTF_8;

/**
 * @author Santhosh Kumar Tekuri
 *
 * Server Request Filter
 */
public class CheckDigestAuthentication extends CheckAuthentication{
    private final String realm;
    public int nonceValiditySeconds = 300;
    private final String key;
    public CheckDigestAuthentication(Authenticator authenticator, String realm, boolean proxy){
        super(authenticator, proxy);
        this.realm = realm;

        try{
            byte bytes[] = new byte[10];
            ThreadLocalRandom.current().nextBytes(bytes);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            key = printHexBinary(md5.digest(bytes)).toLowerCase();
        }catch(NoSuchAlgorithmException ex){
            throw new ImpossibleException(ex);
        }
    }

    @Override
    public boolean filter(ServerExchange exchange, FilterType type) throws Exception{
        assert type==FilterType.REQUEST;
        Credentials credentials = getCredentials(exchange);

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        boolean stale = false;
        if(credentials instanceof DigestCredentials){
            DigestCredentials digestCredentials = (DigestCredentials)credentials;
            long expiryTime = getExpiryTime(md5, digestCredentials.nonce);
            if(expiryTime!=-1){
                long beginTime = -1;
                //beginTime = exchange.getBeginTime();  //todo
                if(beginTime==-1)
                    beginTime = System.currentTimeMillis();
                if(expiryTime<beginTime)
                    stale = true;
                else if("auth".equals(digestCredentials.qop)
                        && realm.equals(digestCredentials.realm)
                        && digestCredentials.nc!=null
                        && digestCredentials.cnonce!=null){
                    String password = authenticator.getPassword(digestCredentials.username);
                    if(password!=null){
                        String a1 = digestCredentials.username+':'+realm+':'+password;
                        String ha1 = printHexBinary(md5.digest(a1.getBytes(UTF_8))).toLowerCase();
                        md5.reset();

                        md5.update((exchange.getRequest().method+":"+digestCredentials.uri).getBytes(UTF_8));
                        String ha2 = printHexBinary(md5.digest()).toLowerCase();
                        md5.reset();

                        String respString = ha1+':'+digestCredentials.nonce;
                        respString += ':'+digestCredentials.nc+':'+digestCredentials.cnonce+':'+digestCredentials.qop;
                        respString += ':'+ha2;
                        String response = printHexBinary(md5.digest(respString.getBytes(UTF_8))).toLowerCase();
                        md5.reset();
                        if(response.equals(digestCredentials.response)){
                            authorized(exchange, digestCredentials.username);
                            return true;
                        }
                    }
                }
            }
        }

        DigestChallenge challenge = new DigestChallenge();
        challenge.realm = realm;
        challenge.qops = Collections.singletonList("auth");
        challenge.stale = stale;
        challenge.nonce = createNonce(md5);
        throw unauthorized(exchange, challenge);
    }

    private String createNonce(MessageDigest md5){
        long expiryTime = System.currentTimeMillis()+nonceValiditySeconds*1000;
        String signature = expiryTime+":"+key;
        signature = printHexBinary(md5.digest(signature.getBytes(UTF_8))).toLowerCase();
        md5.reset();
        String nonce = expiryTime+":"+signature;
        return printBase64Binary(nonce.getBytes(UTF_8));
    }

    private long getExpiryTime(MessageDigest md5, String nonce){
        nonce = new String(parseBase64Binary(nonce), UTF_8);
        int colon = nonce.indexOf(':');
        if(colon!=-1){
            String signature = nonce.substring(0, colon+1)+key;
            signature = printHexBinary(md5.digest(signature.getBytes(UTF_8))).toLowerCase();
            md5.reset();
            if(signature.equals(nonce.substring(colon+1)))
                return Long.parseLong(nonce.substring(0, colon));
        }
        return -1;
    }
}