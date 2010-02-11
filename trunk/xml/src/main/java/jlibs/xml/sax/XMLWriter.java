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

package jlibs.xml.sax;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * XMLReader implementaion for ObjectInputSource
 *
 * @author Santhosh Kumar T
 */
public class XMLWriter extends AbstractXMLReader{
    @Override
    public void parse(InputSource input) throws SAXException, IOException{
        if(input instanceof ObjectInputSource)
            ((ObjectInputSource)input).writeTo(handler);
        else
            throw new IOException("can't parse "+input);
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException{
        throw new UnsupportedOperationException();
    }
}
