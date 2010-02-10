/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.stream;

import org.xml.sax.ext.Locator2;

import javax.xml.stream.XMLStreamReader;

/**
 * SAX Locator2 implementation for XMLStreamReader
 * 
 * @author Santhosh Kumar T
 */
public class STAXLocator implements Locator2{
    private XMLStreamReader reader;

    public STAXLocator(XMLStreamReader reader){
        this.reader = reader;
    }

    @Override
    public String getPublicId(){
        return reader.getLocation().getPublicId();
    }

    @Override
    public String getSystemId(){
        return reader.getLocation().getSystemId();
    }

    @Override
    public int getLineNumber(){
        return reader.getLocation().getLineNumber();
    }

    @Override
    public int getColumnNumber(){
        return reader.getLocation().getColumnNumber();
    }

    @Override
    public String getXMLVersion() {
        return reader.getVersion();
    }

    @Override
    public String getEncoding() {
        return reader.getEncoding();
    }
}
