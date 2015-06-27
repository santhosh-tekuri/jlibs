/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.util.logging;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
            try{
                return new MessageCondition(Pattern.compile(element.getTextContent()));
            }catch(PatternSyntaxException ex){
                throw new RuntimeException("[LogFilter] invalid regex "+element.getTextContent()+" in message element", ex);
            }
        else if("field".equals(name)){
            Attr attr = element.getAttributeNode("name");
            if(attr==null)
                throw new IllegalArgumentException("[LogFilter] name attribute missing in field element");
            int index = Arrays.asList(definition.groupNames).indexOf(attr.getNodeValue());
            try{
                Pattern pattern = Pattern.compile(element.getTextContent());
                return new FieldCondition(pattern, index);
            }catch(PatternSyntaxException ex){
                throw new RuntimeException("[LogFilter] invalid regex "+element.getTextContent()+" in field element", ex);
            }
        }else if("and".equals(name))
            return new AndCondition(getChildConditions(element, definition));
        else if("or".equals(name))
            return new OrCondition(getChildConditions(element, definition));
        else if("not".equals(name)){
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
            throw new RuntimeException("[LogFilter] invalid element "+name);
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
