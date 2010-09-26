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

package jlibs.nblr.actions;

import jlibs.xml.sax.XMLDocument;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;

/**
 * @author Santhosh Kumar T
 */
public class BufferAction implements Action{
    public static BufferAction INSTANCE = new BufferAction();

    private BufferAction(){}

    @Override
    public String javaCode(){
        return "buffer.push()";
    }

    @Override
    public String toString(){
        return "BUFFER";
    }


    /*-------------------------------------------------[ SAXProducer ]---------------------------------------------------*/

    @Override
    public void serializeTo(QName rootElement, XMLDocument xml) throws SAXException{
        xml.addElement("buffer", "");
    }
}
