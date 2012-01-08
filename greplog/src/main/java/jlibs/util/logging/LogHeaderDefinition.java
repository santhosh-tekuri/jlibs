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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.regex.Pattern;

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
        String p = root.getElementsByTagName("pattern").item(0).getTextContent();
        Pattern pattern = Pattern.compile(p);
        String groupNames[] = new String[pattern.matcher("").groupCount()+1];
        groupNames[0] = "header";
        NodeList groupList = root.getElementsByTagName("field");
        for(int i=0; i<groupList.getLength(); i++){
            Element groupElement = (Element)groupList.item(i);
            int index = Integer.parseInt(groupElement.getAttribute("group"));
            String name = groupElement.getAttribute("name");
            groupNames[index] = name;
        }
        return new LogHeaderDefinition(pattern, groupNames);
    }
}
