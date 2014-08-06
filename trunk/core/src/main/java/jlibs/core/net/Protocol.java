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

package jlibs.core.net;

/**
 * @author Santhosh Kumar T
 */
public enum Protocol{
    TCP(-1, false, "Transmission Control Protocol"),
    SSL(-1, true, "Secure Socket Layer"),
    FTP(21, false, "File Transfer Protocol"),
    SSH(22, true, "Secure Shell"),
    TELNET(23, false, "Telnet protocol"),
    SMTP(25, false, "Simple Mail Transfer Protocol"),
    HTTP(80, false, "Hypertext Transfer Protocol"),
    HTTPS(443, true, "Hypertext Transfer Protocol Secure"),
    POP3(110, false, "Post Office Protocol v3"),
    IMAP(143, false, "Internet Message Access Protocol"),
    RMI(1099, false, "Remote Method Invocation"),
    CVS(2401, false, "Concurrent Versions System"),
    SVN(3690, false, "Subversion"),
    ;

    private int port;
    private String displayName;
    private boolean secured;
    Protocol(int port, boolean secured, String displayName){
        this.port = port;
        this.secured = secured;
        this.displayName = displayName;
    }

    public boolean secured(){
        return secured;
    }
    public int port(){
        return port;
    }
    public String displayName(){
        return displayName;
    }
}
