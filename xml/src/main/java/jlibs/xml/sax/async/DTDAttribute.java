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

package jlibs.xml.sax.async;

import jlibs.xml.sax.SAXDelegate;
import org.xml.sax.SAXException;

import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class DTDAttribute{
    public String element;
    
    public String name;
    public AttributeType type;
    public AttributeValueType valueType;
    public String value;
    public List<String> validValues;

    boolean isNamespace(){
        return name.startsWith("xmlns") && (name.length()==5 || name.charAt(5)==':');
    }
    
    void fire(SAXDelegate handler) throws SAXException{
        handler.attributeDecl(element, name, type.toString(validValues), valueType.mode, value);
    }
}
