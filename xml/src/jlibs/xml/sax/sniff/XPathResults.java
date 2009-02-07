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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class XPathResults implements Debuggable{
    private Map<String, Object> map;

    public XPathResults(List<XPath> xpaths){
        map = new HashMap<String, Object>(xpaths.size());
        for(XPath xpath: xpaths)
            map.put(xpath.toString(), xpath.result);
    }

    public Object getResult(XPath xpath){
        return map.get(xpath.toString());
    }
}
