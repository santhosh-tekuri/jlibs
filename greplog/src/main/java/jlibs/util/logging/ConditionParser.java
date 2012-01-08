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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Santhosh Kumar T
 */
public class ConditionParser{
    public static Condition parse(File file, LogHeaderDefinition definition) throws Exception{
        Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getDocumentElement();
        return parse(root, definition);
    }
    
    private static Condition parse(Element element, LogHeaderDefinition definition){
        String name = element.getTagName();
        if("message".equals(name))
            return new MessageCondition(Pattern.compile(element.getTextContent()));
        else if("field".equals(name)){
            int index = Arrays.asList(definition.groupNames).indexOf(element.getAttribute("name"));
            Pattern pattern = Pattern.compile(element.getTextContent());
            return new FieldCondition(pattern, index);
        }else if("and".equals(name) || "or".equals(name)){
            if("and".equals(name))
                return new AndCondition(getChildConditions(element, definition));
            else
                return new OrCondition(getChildConditions(element, definition));
        }else if("not".equals(name)){
            return new NotCondition(getChildConditions(element, definition)[0]);
        }else if("index".equals(name)){
            return new IndexCondition(Integer.parseInt(element.getTextContent()));
        }else if("following".equals(name)){
            boolean includeSelf = Boolean.parseBoolean(element.getAttribute("includeSelf"));
            return new FollowingCondition(getChildConditions(element, definition)[0], includeSelf);
        }else if("preceding".equals(name)){
            boolean includeSelf = Boolean.parseBoolean(element.getAttribute("includeSelf"));
            return new PrecedingCondition(getChildConditions(element, definition)[0], includeSelf);
        }else
            throw new RuntimeException("Invalid Element: "+name);
    }
    
    private static Condition[] getChildConditions(Element parent, LogHeaderDefinition definition){
        List<Condition> conditions = new ArrayList<Condition>();
        NodeList children = parent.getChildNodes();
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(child.getNodeType()==Node.ELEMENT_NODE)
                conditions.add(parse((Element)child, definition));
        }
        return conditions.toArray(new Condition[conditions.size()]);
    }
}
