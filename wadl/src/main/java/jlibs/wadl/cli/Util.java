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

package jlibs.wadl.cli;

/**
 * @author Santhosh Kumar T
 */
public class Util{
    public static boolean isXML(String contentType) {
        if(contentType==null)
            return false;
        int semicolon = contentType.indexOf(';');
        if(semicolon!=-1)
            contentType = contentType.substring(0, semicolon);
        if("text/xml".equalsIgnoreCase(contentType))
            return true;
        else if(contentType.startsWith("application/"))
            return contentType.endsWith("application/xml") || contentType.endsWith("+xml");
        else
            return false;
    }
}
