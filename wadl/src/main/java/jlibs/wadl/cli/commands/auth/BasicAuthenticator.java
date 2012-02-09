/**
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

package jlibs.wadl.cli.commands.auth;

import javax.xml.bind.DatatypeConverter;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

/**
 * @author Santhosh Kumar T
 */
public class BasicAuthenticator implements Authenticator{
    public static final String TYPE = "Basic";
    private String user;
    private String passwd;
    private String headerValue;

    public BasicAuthenticator(String user, String passwd){
        this.user = user;
        this.passwd = passwd;
        String base64UserCredentials = DatatypeConverter.printBase64Binary((user + ":" + passwd).getBytes(Charset.forName("US-ASCII")));
        headerValue = TYPE +" "+ base64UserCredentials;
    }

    @Override
    public void authenticate(HttpURLConnection con){
        con.setRequestProperty(HEADER_AUTHORIZATION, headerValue);
    }
}
