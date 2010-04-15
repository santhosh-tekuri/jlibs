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

package jlibs.xml.sax.dog;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class XPathInfo{
    public String forEach;
    public String xpath;
    public QName resultType;
    public boolean hasAttributes;
    public boolean hasNamespaces;

    public XPathInfo(){
    }

    public XPathInfo(QName resultType){
        this.resultType = resultType;
    }
    
    public XPathInfo(String forEach, String xpath, QName resultType){
        this.forEach = forEach;
        this.xpath = xpath;
        this.resultType = resultType;
    }

    public void guessResultType(){
        for(Map.Entry<QName, List<String>> entry: types.entrySet()){
            for(String str: entry.getValue()){
                if(xpath.startsWith(str)){
                    resultType = entry.getKey();
                    return;
                }
            }
        }
        resultType = XPathConstants.NODESET;
    }

    @Override
    public String toString(){
        return forEach==null ? xpath : "#for-each "+forEach+" #eval "+xpath;
    }

    private static HashMap<QName, List<String>> types = new HashMap<QName, List<String>>();
    static{
        List<String> list = new ArrayList<String>();
        list.add("name(");
        list.add("local-name(");
        list.add("namespace-uri(");
        list.add("string(");
        list.add("substring(");
        list.add("substring-after(");
        list.add("substring-before(");
        list.add("normalize-space(");
        list.add("concat(");
        list.add("translate(");
        list.add("upper-case(");
        list.add("lower-case(");
        types.put(XPathConstants.STRING, list);

        list = new ArrayList<String>();
        list.add("number(");
        list.add("sum(");
        list.add("count(");
        list.add("string-length(");
        types.put(XPathConstants.NUMBER, list);

        list = new ArrayList<String>();
        list.add("boolean(");
        list.add("true(");
        list.add("false(");
        list.add("not(");
        list.add("contains(");
        list.add("starts-with(");
        list.add("ends-with(");
        types.put(XPathConstants.BOOLEAN, list);
    }
}
