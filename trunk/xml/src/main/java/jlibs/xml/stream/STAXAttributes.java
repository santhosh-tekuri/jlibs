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

package jlibs.xml.stream;

import org.xml.sax.Attributes;

import javax.xml.stream.XMLStreamReader;

/**
 * SAX {@link Attributes} implementation for {@link XMLStreamReader}.
 * 
 * @author Santhosh Kumar T
 */
public class STAXAttributes implements Attributes{
    private XMLStreamReader reader;

    public STAXAttributes(XMLStreamReader reader){
        this.reader = reader;
    }

    @Override
    public int getLength(){
        return reader.getAttributeCount();
    }

    @Override
    public String getURI(int index){
        String uri = reader.getAttributeNamespace(index);
        return uri==null ? "" : uri;
    }

    @Override
    public String getLocalName(int index){
        return reader.getAttributeLocalName(index);
    }

    @Override
    public String getQName(int index){
        String localName = reader.getAttributeLocalName(index);
        String prefix = reader.getAttributePrefix(index);
        return prefix==null || prefix.length()==0 ? localName : prefix+':'+localName;
    }

    @Override
    public String getType(int index){
        return reader.getAttributeType(index);
    }

    @Override
    public String getValue(int index){
        return reader.getAttributeValue(index);
    }

    @Override
    public int getIndex(String uri, String localName){
        int count = reader.getAttributeCount();
        for(int i=0; i<count; i++){
            if(getURI(i).equals(uri) && getLocalName(i).equals(localName))
                return i;
        }
        return -1;
    }

    @Override
    public int getIndex(String qname){
        int count = reader.getAttributeCount();
        for(int i=0; i<count; i++){
            if(getQName(i).equals(qname))
                return i;
        }
        return -1;
    }

    @Override
    public String getType(String uri, String localName){
        int index = getIndex(uri, localName);
        return index==-1 ? null : getType(index);
    }

    @Override
    public String getType(String qname){
        int index = getIndex(qname);
        return index==-1 ? null : getType(index);
    }

    @Override
    public String getValue(String uri, String localName){
        return reader.getAttributeValue(uri==null?"":uri, localName);
    }

    @Override
    public String getValue(String qname){
        int index = getIndex(qname);
        return index==-1 ? null : getValue(index);
    }
}
