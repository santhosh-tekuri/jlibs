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

package jlibs.xml.xsd;

import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.xs.LSInputList;
import org.w3c.dom.ls.LSInput;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;

/**
 * @author Santhosh Kumar T
 */
@SuppressWarnings("unchecked")
public class DOMLSInputList extends ArrayList implements LSInputList{
    @Override
    public int getLength(){
        return size();
    }

    @Override
    public LSInput item(int i){
        return (LSInput)get(i);
    }

    public DOMInputImpl addSystemID(String systemID){
        DOMInputImpl input = new DOMInputImpl();
        input.setSystemId(systemID);
        add(input);
        return input;
    }

    public DOMInputImpl addStringData(String stringData, String systemID){
        DOMInputImpl input = new DOMInputImpl();
        input.setStringData(stringData);
        input.setSystemId(systemID);
        add(input);
        return input;
    }

    public DOMInputImpl addReader(Reader reader, String systemID){
        DOMInputImpl input = new DOMInputImpl();
        input.setCharacterStream(reader);
        input.setSystemId(systemID);
        add(input);
        return input;
    }

    public DOMInputImpl addStream(InputStream stream, String systemID, String encoding){
        DOMInputImpl input = new DOMInputImpl();
        input.setByteStream(stream);
        input.setSystemId(systemID);
        input.setEncoding(encoding);
        add(input);
        return input;
    }
}
