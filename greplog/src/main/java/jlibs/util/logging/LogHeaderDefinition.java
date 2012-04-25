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

package jlibs.util.logging;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Santhosh Kumar T
 */
public class LogHeaderDefinition{
    public final Pattern pattern;
    public final String groupNames[];

    public LogHeaderDefinition(Pattern pattern, String[] groupNames){
        this.pattern = pattern;
        this.groupNames = groupNames;
    }

    public static LogHeaderDefinition parse(File file) throws Exception{
        Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getDocumentElement();
        NodeList nodeList = root.getElementsByTagName("pattern");
        if(nodeList.getLength()==0)
            throw new IllegalArgumentException("[LogHeader] pattern element is missing");
        String p = nodeList.item(0).getTextContent();
        Pattern pattern;
        try{
            pattern = Pattern.compile(p);
        }catch(PatternSyntaxException ex){
            throw new RuntimeException("[LogHeader] invalid regex in pattern element", ex);
        }
        String groupNames[] = new String[pattern.matcher("").groupCount()+1];
        groupNames[0] = "header";
        NodeList groupList = root.getElementsByTagName("field");
        for(int i=0; i<groupList.getLength(); i++){
            Element groupElement = (Element)groupList.item(i);
            Attr attr = groupElement.getAttributeNode("group");
            if(attr==null)
                throw new IllegalArgumentException("[LogHeader] group attribute missing in field element");
            int index = Integer.parseInt(attr.getNodeValue());
            attr = groupElement.getAttributeNode("name");
            if(attr==null)
                throw new IllegalArgumentException("[LogHeader] name attribute missing in field element");
            String name = attr.getNodeValue();
            if(name.equals("header"))
                throw new IllegalArgumentException("[LogHeader] field name header is reserved");
            groupNames[index] = name;
        }
        if(groupNames[groupNames.length-1]==null)
            throw new IllegalArgumentException("[LogHeader] expected "+(groupNames.length-1)+" field elements");
        return new LogHeaderDefinition(pattern, groupNames);
    }
}
