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

package jlibs.examples.xml.sax.dog.engines;

import jlibs.core.graph.Convertor;
import jlibs.core.graph.Navigator2;
import jlibs.core.graph.PredicateConvertor;
import jlibs.core.graph.Sequence;
import jlibs.core.graph.sequences.AbstractSequence;
import jlibs.core.lang.StringUtil;
import jlibs.examples.xml.sax.dog.TestCase;
import jlibs.examples.xml.sax.dog.XPathEngine;
import jlibs.examples.xml.sax.dog.XPathInfo;
import jlibs.xml.Namespaces;
import jlibs.xml.dom.DOMUtil;
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.NodeType;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.NamespaceIterator;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.xpath.JAXPXPathStaticContext;
import net.sf.saxon.xpath.XPathEvaluator;
import net.sf.saxon.xpath.XPathFactoryImpl;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPathConstants;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class SaxonEngine extends XPathEngine{
    @Override
    public String getName(){
        return "SAXON";
    }

    @Override
    public List<Object> evaluate(TestCase testCase, String file) throws Exception{
        XPathFactoryImpl xpf = new XPathFactoryImpl();
        XPathEvaluator xpe = (XPathEvaluator)xpf.newXPath();
        xpe.setStaticContext(new JAXPXPathStaticContext(xpe.getConfiguration()){
            @Override public void issueWarning(String s, SourceLocator locator){}
        });
        xpe.setBackwardsCompatible(true);
        xpe.setXPathVariableResolver(testCase.variableResolver);
        xpe.setXPathFunctionResolver(testCase.functionResolver);
        xpe.setNamespaceContext(testCase.nsContext);
        NodeInfo doc = xpe.setSource(new SAXSource(new InputSource(file)));

        List<Object> results = new ArrayList<Object>(testCase.xpaths.size());
        for(XPathInfo xpathInfo: testCase.xpaths){
            if(xpathInfo.forEach==null)
                results.add(xpe.evaluate(xpathInfo.xpath, doc, xpathInfo.resultType));
            else{
                List<Object> list = new ArrayList<Object>();
                List nodeList = (List)xpe.evaluate(xpathInfo.forEach, doc, XPathConstants.NODESET);
                for(Object context: nodeList)
                    list.add(xpe.evaluate(xpathInfo.xpath, context, xpathInfo.resultType));
                results.add(list);
            }
        }
        return results;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public List<?> translate(Object result, NamespaceContext nsContext){
        List nodeList = (List)result;
        int i = 0;
        for(Object item: nodeList){
            if(item instanceof List)
                nodeList.set(i, translate(item, nsContext));
            else{
                NodeInfo node = (NodeInfo)item;
                int type = node.getNodeKind();
                String value = "";
                if(type!=NodeType.DOCUMENT && type!=NodeType.ELEMENT)
                    value = node.getStringValue();
                String localName = node.getLocalPart();
                String namespaceURI = node.getURI();
                String qualifiedName = node.getDisplayName();
                String location = SaxonNavigator.INSTANCE.getXPath(node, nsContext);
                NodeItem nodeItem = new NodeItem(type, location, value, localName, namespaceURI, qualifiedName);
                nodeItem.xml = node;
                nodeList.set(i, nodeItem);
            }
            i++;
        }
        return nodeList;
    }

    @Override
    public boolean equals(Object node1, Node node2){
        if(node1==node2)
            return true;
        NodeInfo n1 = (NodeInfo)node1;
        Node n2 = node2;

        while(true){
            if(!shallowEquals(n1, n2))
                return false;
            if(n1==null){
                assert n2==null;
                return true;
            }
            NodeInfo n1Child = new NodeInfoSequence(n1, Axis.CHILD).findNext();
            Node n2Child = null;
            if(n2.getNodeType()==Node.DOCUMENT_NODE || n2.getNodeType()==Node.ELEMENT_NODE)
                n2Child = n2.getFirstChild(); // the jdk's dom impl returns non-null child for attribute etc
            if(!shallowEquals(n1Child, n2Child))
                return false;
            if(n1Child==null){
                assert n2Child==null;

                if(n1==node1 && n2==node2)
                    return true;

                while(true){
                    NodeInfo n1Sibling = new NodeInfoSequence(n1, Axis.FOLLOWING_SIBLING).findNext();
                    Node n2Sibling = n2.getNextSibling();
                    if(!shallowEquals(n1Sibling, n2Sibling))
                        return false;
                    if(n1Sibling==null){
                        assert n2Sibling==null;
                        NodeInfo n1Parent = new NodeInfoSequence(n1, Axis.ANCESTOR).findNext();
                        Node n2Parent = n2.getParentNode();

                        if(n1Parent==null && n2Parent==null)
                            return true;
                        if(n1Parent==node1 && n2Parent==node2)
                            return true;
                        assert n1Parent!=null && n2Parent!=null;
                        n1 = n1Parent;
                        n2 = n2Parent;
                    }else{
                        assert n2Sibling!=null;
                        n1 = n1Sibling;
                        n2 = n2Sibling;
                        break;
                    }
                }
            }else{
                n1 = n1Child;
                n2 = n2Child;
            }
        }
    }

    public static boolean isNamespaceDeclaration(NodeInfo attr){
        return Namespaces.URI_XMLNS.equals(attr.getURI()) || attr instanceof NamespaceIterator.NamespaceNodeImpl;
    }

    private boolean shallowEquals(NodeInfo n1, Node n2){
        if(n1==n2)
            return true;
        if(n1==null || n2==null)
            return false;

        int type1 = n1.getNodeKind();
        if(type1==Node.CDATA_SECTION_NODE)
            type1 = Node.TEXT_NODE;
        else if(type1==NodeType.NAMESPACE)
            type1 = Node.ATTRIBUTE_NODE;

        int type2 = n2.getNodeType();
        if(type2==Node.CDATA_SECTION_NODE)
            type2 = Node.TEXT_NODE;

        if(type1!=type2)
            return false;

        switch(type1){
            case Node.PROCESSING_INSTRUCTION_NODE:
                ProcessingInstruction pi2 = (ProcessingInstruction)n2;
                String target1 = n1.getDisplayName();
                String target2 = pi2.getTarget();
                if(!target1.equals(target2))
                    return false;
                String data1 = n1.getStringValue();
                String data2 = pi2.getData();
                if(!data1.equals(data2))
                    return  false;
                break;
            case Node.COMMENT_NODE:
                Comment comment2 = (Comment)n2;
                data1 = n1.getStringValue();
                data2 = comment2.getData();
                if(!data1.equals(data2))
                    return false;
                break;
            case Node.ELEMENT_NODE:
                Element element2 = (Element)n2;
                String namespaceURI1 = n1.getURI();
                if(namespaceURI1==null)
                    namespaceURI1 = "";
                String namespaceURI2 = element2.getNamespaceURI();
                if(namespaceURI2==null)
                    namespaceURI2 = "";
                if(!namespaceURI1.equals(namespaceURI2))
                    return false;
                String localName1 = n1.getLocalPart();
                String localName2 = element2.getLocalName();
                if(!localName1.equals(localName2))
                    return false;

                NodeInfoSequence attrs1 = new NodeInfoSequence(n1, Axis.ATTRIBUTE);
                NamedNodeMap attrs2 = element2.getAttributes();
                BitSet bitSet = new BitSet();
                NodeInfo attr1;
                while((attr1=attrs1.findNext())!=null){
                    if(isNamespaceDeclaration(attr1))
                        continue;
                    namespaceURI1 = attr1.getURI();
                    if(namespaceURI1==null)
                        namespaceURI1 = "";
                    localName1 = attr1.getLocalPart();
                    String value1 = attr1.getStringValue();

                    int found = -1;
                    for(int i=0; i<attrs2.getLength(); i++){
                        Attr attr2 = (Attr)attrs2.item(i);
                        namespaceURI2 = attr2.getNamespaceURI();
                        if(namespaceURI2==null)
                            namespaceURI2 = "";
                        localName2 = attr2.getLocalName();
                        if(namespaceURI1.equals(namespaceURI2) && localName1.equals(localName2)){
                            String value2 = attr2.getNodeValue();
                            if(!value1.equals(value2))
                                return false;
                            found = i;
                            break;
                        }
                    }
                    if(found==-1)
                        return false;
                    else
                        bitSet.set(found);
                }
                for(int i=0; i<attrs2.getLength(); i++){
                    if(!bitSet.get(i)){
                        Attr attr2 = (Attr)attrs2.item(i);
                        if(!DOMUtil.isNamespaceDeclaration(attr2))
                            return false;
                    }
                }

                break;
            case Node.ATTRIBUTE_NODE:
                Attr attr2 = (Attr)n2;
                namespaceURI1 = isNamespaceDeclaration(n1) ? Namespaces.URI_XMLNS : n1.getURI();
                if(namespaceURI1==null)
                    namespaceURI1 = "";
                namespaceURI2 = attr2.getNamespaceURI();
                if(namespaceURI2==null)
                    namespaceURI2 = "";
                if(!namespaceURI1.equals(namespaceURI2))
                    return false;
                localName1 = n1.getLocalPart();
                localName2 = attr2.getLocalName();
                if(!localName1.equals(localName2))
                    return false;
                String value1 = n1.getStringValue();
                String value2 = attr2.getNodeValue();
                if(!value1.equals(value2))
                    return false;
                break;
            case Node.TEXT_NODE:
                value1 = n1.getStringValue();
                value2 = n2.getNodeValue();
                if(!value1.equals(value2))
                    return false;
        }

        return true;
    }

    static class SaxonNavigator extends Navigator2<NodeInfo>{
        static SaxonNavigator INSTANCE = new SaxonNavigator();
        @Override
        public NodeInfo parent(NodeInfo node){
            return node.getParent();
        }

        @Override
        public Sequence<? extends NodeInfo> children(NodeInfo node){
            return new NodeInfoSequence(node, Axis.CHILD);
        }

        private class XPathConvertor extends PredicateConvertor<NodeInfo>{
            public XPathConvertor(NamespaceContext nsContext){
                super(INSTANCE, new SaxonXPathNameConvertor(nsContext));
            }

            @Override
            public String convert(NodeInfo source){
                switch(source.getNodeKind()){
                    case NodeType.ATTRIBUTE:
                    case NodeType.NAMESPACE:
                        return delegate.convert(source);
                }
                return super.convert(source);
            }
        }

        public String getXPath(NodeInfo node, NamespaceContext nsContext){
            if(node.getNodeKind()==NodeType.DOCUMENT)
                return "/";
            else
                return getPath(node, new XPathConvertor(nsContext), "/");
        }
    }

    static class NodeInfoSequence extends AbstractSequence<NodeInfo>{
        private NodeInfo nodeInfo;
        private byte axis;

        private AxisIterator iterator;
        NodeInfoSequence(NodeInfo nodeInfo, byte axis){
            this.nodeInfo = nodeInfo;
            this.axis = axis;
            iterator = nodeInfo.iterateAxis(axis);
        }

        NodeInfoSequence(AxisIterator iterator){
            this.iterator = iterator;
        }

        @Override
        protected NodeInfo findNext(){
            return (NodeInfo)iterator.next();
        }

        @Override
        public Sequence<NodeInfo> copy(){
            return new NodeInfoSequence(nodeInfo, axis);
        }
    }

    static class SaxonXPathNameConvertor implements Convertor<NodeInfo, String>{
        private NamespaceContext nsContext;

        public SaxonXPathNameConvertor(NamespaceContext nsContext){
            this.nsContext = nsContext;
        }

        @Override
        public String convert(NodeInfo source){
            switch(source.getNodeKind()){
                case Node.DOCUMENT_NODE:
                    return "";
                case Node.TEXT_NODE:
                case Node.CDATA_SECTION_NODE:
                    return "text()";
                case Node.COMMENT_NODE:
                    return "comment()";
                case Node.PROCESSING_INSTRUCTION_NODE:
                    return "processing-instruction('"+source.getDisplayName() +"')";
                case Node.ELEMENT_NODE:
                    String prefix = nsContext.getPrefix(source.getURI());
                    String name = source.getLocalPart();
                    return StringUtil.isEmpty(prefix) ? name : prefix+':'+name;
                case Node.ATTRIBUTE_NODE:
                    if(Namespaces.URI_XMLNS.equals(source.getURI()))
                        return "namespace::"+source.getLocalPart();
                    prefix = nsContext.getPrefix(source.getURI());
                    name = source.getLocalPart();
                    return '@'+ (StringUtil.isEmpty(prefix) ? name : prefix+':'+name);
                case NodeType.NAMESPACE:
                    return "namespace::"+source.getLocalPart();
                default:
                    return null;
            }
        }
    }
}
