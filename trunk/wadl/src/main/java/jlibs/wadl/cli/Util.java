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

package jlibs.wadl.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class Util{
    public static boolean isContentType(String contentType, String match){
        if(contentType==null)
            return false;
        int semicolon = contentType.indexOf(';');
        if(semicolon!=-1)
            contentType = contentType.substring(0, semicolon);
        if(("text/"+match).equalsIgnoreCase(contentType))
            return true;
        else if(contentType.startsWith("application/"))
            return contentType.endsWith("application/"+match) || contentType.endsWith("+"+match);
        else
            return false;
    }
    
    public static boolean isXML(String contentType){
        return isContentType(contentType, "xml");
    }
    
    public static boolean isJSON(String contentType){
        return isContentType(contentType, "json");
    }

    public static boolean isPlain(String contentType){
        return isContentType(contentType, "plain");
    }

    public static boolean isHTML(String contentType){
        return isContentType(contentType, "html");
    }

    public static Map<String, List<String>> toMap(List<String> list, char separator){
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        for(String item: list){
            int index = item.indexOf(separator);
            if(index!=-1){
                String key = item.substring(0, index);
                String value = item.substring(index+1);
                List<String> values = map.get(key);
                if(values==null)
                    map.put(key, values=new ArrayList<String>());
                values.add(value);
            }
        }
        return map;
    }
}
