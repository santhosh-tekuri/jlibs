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

package jlibs.xml.dom;

import jlibs.core.graph.Navigator2;
import jlibs.core.graph.PredicateConvertor;
import jlibs.core.graph.Sequence;
import jlibs.core.graph.sequences.ConcatSequence;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;

/**
 * @author Santhosh Kumar T
 */
public class DOMNavigator extends Navigator2<Node>{
    @Override
    public Node parent(Node node){
        if(node instanceof Attr)
            return ((Attr)node).getOwnerElement();
        else
            return node.getParentNode();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Sequence<? extends Node> children(Node node){
        Sequence<Node> seq = new NodeListSequence(node.getChildNodes());
        if(node instanceof Element){
            Element elem = (Element)node;
            seq = new ConcatSequence<Node>(new NamedNodeMapSequence(elem.getAttributes()), seq);
        }
        return seq;
    }

    private class XPathConvertor extends PredicateConvertor<Node>{
        public XPathConvertor(NamespaceContext nsContext){
            super(DOMNavigator.this, new DOMXPathNameConvertor(nsContext));
        }

        @Override
        public String convert(Node source){
            switch(source.getNodeType()){
                case Node.ATTRIBUTE_NODE:
                case 13: /*Node.NAMESPACE_NODE*/
                    return delegate.convert(source);
            }
            return super.convert(source);
        }
    }

    public String getXPath(Node node, NamespaceContext nsContext){
        if(node instanceof Document)
            return "/";
        else
            return getPath(node, new XPathConvertor(nsContext), "/");
    }
}
