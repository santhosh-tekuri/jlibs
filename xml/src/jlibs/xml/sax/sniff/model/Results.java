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

package jlibs.xml.sax.sniff.model;

import jlibs.xml.sax.sniff.Debuggable;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Santhosh Kumar T
 */
public class Results implements Debuggable{
    public TreeMap<Integer, String> results;

    public void addResult(int docOrder, String result){
        if(results==null)
            results = new TreeMap<Integer, String>();
        results.put(docOrder, result);
    }

    public void addAllResults(Results r){
        if(r!=null && r.hasResult()){
            for(Map.Entry<Integer, String> entry: r.results.entrySet())
                addResult(entry.getKey(), entry.getValue());
        }
    }

    public boolean hasResult(){
        return results!=null && results.size()>0;
    }

    public void reset(){
        results = null;
    }
}
