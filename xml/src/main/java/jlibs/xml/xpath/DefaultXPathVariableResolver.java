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

package jlibs.xml.xpath;

import javax.xml.xpath.XPathVariableResolver;
import javax.xml.namespace.QName;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Santhosh Kumar T
 */
public class DefaultXPathVariableResolver implements XPathVariableResolver{
    private final Map<QName, Object> map;

    public DefaultXPathVariableResolver(){
        this(null);
    }

    public DefaultXPathVariableResolver(Map<QName, Object> map){
        if(map==null)
            map = new HashMap<QName, Object>();
        this.map = map;
    }

    public void defineVariable(QName variableName, Object value){
        map.put(variableName, value);
    }
    
    @Override
    public Object resolveVariable(QName variableName){
        return map.get(variableName);
    }
}
