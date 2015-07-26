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

package jlibs.xml.sax.dog;

import jlibs.core.io.IOUtil;
import jlibs.core.lang.ImpossibleException;
import jlibs.xml.Namespaces;
import jlibs.xml.dom.DOMNavigator;
import jlibs.xml.dom.DOMUtil;
import jlibs.xml.sax.dog.sniff.Event;
import org.jaxen.dom.NamespaceNode;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;

/**
 * @author Santhosh Kumar T
 */
public class NodeItem implements NodeType{
    /**
     * This field tells the type of this NodeItem.
     * It is one of the constants in NodeType other than ANY, MAX
     */
    public final int type;
    public final String location; // unique xpath
    public final String value;
    public final String localName;
    public final String namespaceURI;
    public final String qualifiedName;

    public final long order;

    /**
     * This field tells how many expressions have hit or might hit
     * this NodeItem. This is incremented by Event. and decremented by
     * XMLBuilder.
     *
     * This field is used to decide whether xml needs be built for this
     * NodeItem or not. If refCount becomes zero before xml is completely
     * built, then the xml object built so far is discarded.
     */
    public int refCount;

    /**
     * This field holds the in-memory xml representation of this node item.
     * This could be DOM Node or XOM Node etc based on which type of
     * XMLBuilder is being used.
     */
    public Object xml;

    /**
     * This field tells if the value of field `xml` is completely built
     * or not.
     */
    public boolean xmlBuilt;

    public NodeItem(){
        order = 0;
        type = DOCUMENT;
        location = "/";
        value = null;
        localName = null;
        namespaceURI = null;
        qualifiedName = null;
    }

    public NodeItem(Event event){
        order = event.order();
        type = event.type();
        location = event.location();
        value = event.value();
        localName = event.localName();
        namespaceURI = event.namespaceURI();
        qualifiedName = event.qualifiedName();
    }

    // used only for testing purposes
    public NodeItem(Node node, NamespaceContext nsContext){
        order = -100; // not used
        if(node instanceof Attr && Namespaces.URI_XMLNS.equals(node.getNamespaceURI()))
            type = NAMESPACE;
        else if(node.getNodeType()==NamespaceNode.NAMESPACE_NODE)
            type = NAMESPACE;
        else
            type = node.getNodeType();
        location = new DOMNavigator().getXPath(node, nsContext);
        value = node.getNodeValue();

        localName = node.getLocalName();
        namespaceURI = node.getNamespaceURI();
        qualifiedName = node.getNodeName();
        xml = node;
    }

    // used only for testing purposes
    public NodeItem(int type, String location, String value, String localName, String namespaceURI, String qualifiedName){
        order = -100; // not used
        this.type = type;
        this.location = location;
        this.value = value;
        this.localName = localName;
        this.namespaceURI = namespaceURI;
        this.qualifiedName = qualifiedName;
    }

    // used only for testing purposes
    public NodeItem(Node node, String prefix, String uri, NamespaceContext nsContext){
        order = -100; // not used
        type = NAMESPACE;

        location = new DOMNavigator().getXPath(node, nsContext)+"/namespace::"+prefix;
        value = uri;

        localName = prefix;
        namespaceURI = Namespaces.URI_XMLNS;
        qualifiedName = "xmlns:"+prefix;
        xml = node;
    }

    public void printTo(PrintStream out){
        if(xml instanceof Node){
            out.println(location);
            DOMUtil.serialize((Node)xml, out);
        }else
            out.print(localName);
    }

    @Override
    public String toString(){
        if(xml instanceof Node){
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            printTo(new PrintStream(bout, true));
            try{
                return bout.toString(IOUtil.UTF_8.name());
            }catch(UnsupportedEncodingException ex){
                throw new ImpossibleException(ex);
            }
        }else
            return location;
    }

    /**
     * This field is used only in instantResults mode;
     * This field tells which expressions has hit this node.
     * if ith bit is set, it means the doc-expression whose id is i
     * has hit this node.
     *
     * This is used not to notify same node in an expression's result
     * more than once
     */
    public BitSet expressions;
}
