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

import javax.xml.namespace.NamespaceContext;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class XPathResults implements Debuggable{
    private NamespaceContext nsContext;
    private Map<String, Object> map;

    public XPathResults(NamespaceContext nsContext, List<XPath> xpaths){
        this.nsContext = nsContext;
        map = new LinkedHashMap<String, Object>(xpaths.size());
        for(XPath xpath: xpaths)
            map.put(xpath.toString(), xpath.result);
    }

    public NamespaceContext getNamespaceContext(){
        return nsContext;
    }

    public Object getResult(XPath xpath){
        return map.get(xpath.toString());
    }

    /*-------------------------------------------------[ Printing ]---------------------------------------------------*/
    
    private void print(PrintStream out, String xpath, Object result){
        out.printf("XPath: %s%n", xpath);
        if(result instanceof Collection){
            int i = 0;
            for(Object item: (Collection)result)
                out.printf("      %d: %s%n", ++i, item);
        }else
            out.printf("      %s\n", result);
    }

    public void printResult(PrintStream out, XPath xpath){
        print(out, xpath.toString(), map.get(xpath.toString()));
    }

    public void print(PrintStream out){
        for(Map.Entry<String, Object> entry: map.entrySet()){
            print(out, entry.getKey(), entry.getValue());
            out.println();
        }
    }
}
