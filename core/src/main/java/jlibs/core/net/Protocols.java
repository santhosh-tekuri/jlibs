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
public enum Protocols{
    FTP(21, "File Transfer Protocol"),
    SSH(22, "Secure Shell"),
    TELNET(23, "Telnet protocol"),
    SMTP(25, "Simple Mail Transfer Protocol"),
    HTTP(80, "Hypertext Transfer Protocol"),
    HTTPS(443, "Hypertext Transfer Protocol Secure"),
    POP3(110, "Post Office Protocol v3"),
    IMAP(143, "Internet Message Access Protocol"),
    RMI(1099, "Remote Method Invocation"),
    CVS(2401, "Concurrent Versions System"),
    SVN(3690, "Subversion"),
    ;

    private int port;
    private String displayName;
    Protocols(int port, String displayName){
        this.port = port;
        this.displayName = displayName;
    }

    public int port(){
        return port;
    }

    public String displayName(){
        return displayName;
    }
}
