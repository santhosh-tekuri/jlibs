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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static java.util.Objects.requireNonNull;

/**
 * @author Santhosh Kumar Tekuri
 */
public class DigestChallenge extends Challenge{
    public static final String SCHEME = "Digest";

    public static final String REALM = "realm";
    public static final String NONCE = "nonce";
    public static final String DOMAIN = "domain";
    public static final String ALGORITHM = "algorithm";
    public static final String OPAQUE = "opaque";
    public static final String QOP = "qop";
    public static final String STALE = "stale";

    // mandatory
    public String realm;
    public String nonce;

    // optional
    public List<String> domains;
    public String opaque;
    public boolean stale;
    public String algorithm;
    public List<String> qops;


    public DigestChallenge(){
    }

    public DigestChallenge(String headerValue){
        Parser parser = new Parser(true, headerValue);
        while(true){
            String name = parser.lvalue();
            if(name==null)
                break;
            else if(REALM.equalsIgnoreCase(name))
                realm = parser.rvalue();
            else if(NONCE.equalsIgnoreCase(name))
                nonce = parser.rvalue();
            else if(DOMAIN.equalsIgnoreCase(name)){
                StringTokenizer stok = new StringTokenizer(parser.rvalue(), " ");
                domains = new ArrayList<>(stok.countTokens());
                while(stok.hasMoreTokens())
                    domains.add(stok.nextToken());
            }else if(ALGORITHM.equalsIgnoreCase(name))
                algorithm = parser.rvalue();
            else if(OPAQUE.equalsIgnoreCase(name))
                opaque = parser.rvalue();
            else if(QOP.equalsIgnoreCase(name)){
                StringTokenizer stok = new StringTokenizer(parser.rvalue(), ",");
                qops = new ArrayList<>(stok.countTokens());
                while(stok.hasMoreTokens())
                    qops.add(stok.nextToken().trim());
            }else if(STALE.equalsIgnoreCase(name))
                stale = Boolean.valueOf(parser.rvalue());
            parser.skipPairs();
            parser.skip();
        }

        requireNonNull(realm, "realm==null");
        requireNonNull(nonce, "nonce==null");
    }

    @Override
    public String scheme(){
        return SCHEME;
    }

    @Override
    public String toString(){
        StringBuilder buffer = new StringBuilder();
        buffer.append(SCHEME).append(' ');

        Parser.appendQuotedValue(buffer, REALM, realm);

        if(domains!=null && !domains.isEmpty()){
            buffer.append(',');
            Parser.appendQuotedValue(buffer, DOMAIN, String.join(" ", domains));
        }

        buffer.append(',');
        Parser.appendQuotedValue(buffer, NONCE, nonce);

        if(opaque!=null){
            buffer.append(',');
            Parser.appendQuotedValue(buffer, OPAQUE, opaque);
        }
        if(stale){
            buffer.append(',');
            buffer.append(STALE).append("=true");
        }
        if(algorithm!=null){
            buffer.append(',');
            buffer.append(ALGORITHM).append('=').append(algorithm);
        }
        if(qops!=null && !qops.isEmpty()){
            buffer.append(',');
            Parser.appendQuotedValue(buffer, QOP, String.join(",", qops));
        }

        return buffer.toString();
    }
}