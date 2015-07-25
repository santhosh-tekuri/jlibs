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

package jlibs.examples.xml.sax.dog;

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.dom.DOMUtil;
import jlibs.xml.sax.dog.NodeItem;
import net.sf.saxon.dom.DOMNodeWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class XPathEngine{
    public abstract String getName();
    public abstract List<Object> evaluate(TestCase testCase, String file) throws Exception;

    protected final Document toDOM(String file) throws ParserConfigurationException, IOException, SAXException{
        return DOMUtil.newDocumentBuilder(true, false, true, false).parse(new InputSource(file));
    }

    @SuppressWarnings({"unchecked"})
    public List<?> translate(Object result, NamespaceContext nsContext){
        List nodeList = new ArrayList<NodeItem>();

        if(result instanceof NodeList){
            NodeList nodeSet = (NodeList)result;
            for(int i=0; i<nodeSet.getLength(); i++){
                Node node = nodeSet.item(i);
                NodeItem item = new NodeItem(node, nsContext);
                nodeList.add(item);
            }
        }else{
            if(result instanceof List){
                for(Object obj: (Collection)result){
                    Object item;
                    if(obj instanceof Node)
                        item = new NodeItem((Node)obj, nsContext);
                    else if(obj instanceof net.sf.saxon.om.NodeInfo){
                        net.sf.saxon.om.NodeInfo info = (net.sf.saxon.om.NodeInfo)obj;
                        Node node = (Node)((DOMNodeWrapper)info.getParent()).getUnderlyingNode();
                        item = new NodeItem(node, info.getLocalPart(), info.getStringValue(), nsContext);
                    }else if(obj instanceof NodeList)
                        item = translate(obj, nsContext);
                    else
                        throw new ImpossibleException(obj.getClass().getName());
                    nodeList.add(item);
                }
            }
        }

        return nodeList;
    }

    public boolean equals(Object myNode, Node dogNode){
        return DOMUtil.equals((Node)myNode, dogNode);
    }
}
