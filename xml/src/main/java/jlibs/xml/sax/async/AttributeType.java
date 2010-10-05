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

import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public enum AttributeType{
    CDATA,
    ENUMERATION,
    ID,
    IDREF,
    IDREFS,
    NMTOKEN,
    NMTOKENS,
    ENTITY,
    ENTITIES,
    NOTATION;

    public String toString(List<String> validValues){
        switch(this){
            case NOTATION:
            case ENUMERATION:
                StringBuilder buff = new StringBuilder();
                if(this==NOTATION)
                    buff.append(name()).append(' ');
                buff.append('(');
                for(int i=0; i<validValues.size(); i++){
                    if(i>0)
                        buff.append('|');
                    buff.append(validValues.get(i));
                }
                buff.append(')');
                return buff.toString();
            default:
                return name();
        }
    }
}
