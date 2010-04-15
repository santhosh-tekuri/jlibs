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

package jlibs.xml.sax.dog.engines;

import jlibs.core.graph.Convertor;
import jlibs.core.graph.Navigator2;
import jlibs.core.graph.PredicateConvertor;
import jlibs.core.graph.Sequence;
import jlibs.core.graph.sequences.AbstractSequence;
import jlibs.core.lang.StringUtil;
import jlibs.xml.Namespaces;
import jlibs.xml.sax.dog.*;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.xpath.JAXPXPathStaticContext;
import net.sf.saxon.xpath.XPathEvaluator;
import net.sf.saxon.xpath.XPathFactoryImpl;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPathConstants;
import java.util.ArrayList;
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
    public List<NodeItem> translate(Object result, NamespaceContext nsContext){
        List nodeList = (List)result;
        int i = 0;
        for(Object item: nodeList){
            if(item instanceof List)
                nodeList.set(i, translate(item, nsContext));
            else{
                NodeInfo node = (NodeInfo)item;
                int type = node.getNodeKind();
                String value = "";
                if(type!= NodeType.DOCUMENT && type!=NodeType.ELEMENT)
                    value = node.getStringValue();
                String localName = node.getLocalPart();
                String namespaceURI = node.getURI();
                String qualifiedName = node.getDisplayName();
                String location = SaxonNavigator.INSTANCE.getXPath(node, nsContext);
                nodeList.set(i, new NodeItem(type, location, value, localName, namespaceURI, qualifiedName));
            }
            i++;
        }
        return nodeList;
    }

    static class SaxonNavigator extends Navigator2<NodeInfo>{
        static SaxonNavigator INSTANCE = new SaxonNavigator();
        @Override
        public NodeInfo parent(NodeInfo node){
            return node.getParent();
        }

        @Override
        public Sequence<? extends NodeInfo> children(NodeInfo node){
            return new NodeInfoSequence(node);
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
        AxisIterator iterator;

        NodeInfoSequence(NodeInfo parent){
            iterator = parent.iterateAxis(Axis.CHILD);
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
            return new NodeInfoSequence(iterator);
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
