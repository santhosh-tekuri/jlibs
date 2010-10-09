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

    public String normalize(String value){
        switch(this){
            case NMTOKEN:
            case ENTITY:
            case ID:
            case IDREF:
            case ENUMERATION:
                return value.trim();
            case NMTOKENS:
            case ENTITIES:
            case IDREFS:
                return toNMTOKENS(value);
            default:
                return value;
        }
    }
    
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

    private String toNMTOKENS(String value){
        char[] buffer = value.toCharArray();
        int write = 0;
        int lastWrite = 0;
        boolean wroteOne = false;

        int read = 0;
        while(read<buffer.length && buffer[read]==' '){
            read++;
        }

        int len = buffer.length;
        while(len<read && buffer[len-1]==' ')
            len--;

        while(read<len){
            if (buffer[read]==' '){
                if (wroteOne)
                    buffer[write++] = ' ';

                do{
                    read++;
                }while(read<len && buffer[read]==' ');
            }else{
                buffer[write++] = buffer[read++];
                wroteOne = true;
                lastWrite = write;
            }
        }

        value = new String(buffer, 0, lastWrite);
        return value;
    }
}
