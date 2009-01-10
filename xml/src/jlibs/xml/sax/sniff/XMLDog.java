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

package jlibs.xml.sax.sniff;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.jaxen.saxpath.SAXPathException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * @author Santhosh Kumar T
 */
public class XMLDog{
    public static boolean debug = false;
    
    private Root root;

    public XMLDog(NamespaceContext nsContext){
        root = new Root(nsContext);
    }

    public Node add(String xpath) throws SAXPathException{
        return add(xpath, -1);
    }

    public Node add(String xpath, int minHits) throws SAXPathException{
        if(minHits<0)
            minHits = Integer.MAX_VALUE;
        return root.add(xpath, minHits);
    }

    public void sniff(InputSource source) throws ParserConfigurationException, SAXException, IOException{
        new Sniffer(root).sniff(source);
    }
}
