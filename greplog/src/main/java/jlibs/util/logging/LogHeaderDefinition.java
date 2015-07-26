/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
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
