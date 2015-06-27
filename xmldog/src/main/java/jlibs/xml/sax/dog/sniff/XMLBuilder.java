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

package jlibs.xml.sax.dog.sniff;

import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.NodeType;
import jlibs.xml.sax.helpers.MyNamespaceSupport;
import org.xml.sax.Attributes;

import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class XMLBuilder{
    // changed to true by Event in onStartDocument/onStartElement if current event is hit
    // changed to false by doEndElement() when curNode is completely populated
    // keeps changing values true <-> false, so that nodes are created only portions of xml
    boolean active = false;

    protected abstract Object onStartDocument();
    Object doStartDocument(NodeItem nodeItem){
        stack.add(nodeItem);
        return onStartDocument();
    }

    protected abstract Object onStartElement(Event event);
    Object doStartElement(Event event, NodeItem nodeItem){
        stack.add(nodeItem);
        return onStartElement(event);
    }

    protected abstract Object onEvent(Event event);

    protected abstract Object onEndElement();
    Object doEndElement(Event event){
        assert active;
        NodeItem finishedNode = stack.remove(stack.size()-1);
        if(finishedNode!=null)
            event.finishedXMLBuild(finishedNode);
        Object node = onEndElement();
        if(node==null)
            active = false;
        return node;
    }

    protected abstract void onEndDocument();
    void doEndDocument(Event event){
        if(!stack.isEmpty()){
            NodeItem finishedNode = stack.remove(stack.size()-1);
            if(finishedNode!=null)
                event.finishedXMLBuild(finishedNode);
        }
        stack = null;
        onEndDocument();
    }

    public void onAttributes(Event event, Attributes attrs){
        assert active;
        int len = attrs.getLength();
        for(int i=0; i<len; i++){
            event.setData(NodeType.ATTRIBUTE, attrs.getURI(i), attrs.getLocalName(i), attrs.getQName(i), attrs.getValue(i));
            onEvent(event);
        }
    }

    public void onAttributes(Event event, XMLStreamReader reader){
        assert active;
        int len = reader.getAttributeCount();
        for(int i=0; i<len; i++){
            String prefix = reader.getAttributePrefix(i);
            String localName = reader.getAttributeLocalName(i);
            String qname = prefix.length()==0 ? localName : prefix+':'+localName;
            String uri = reader.getAttributeNamespace(i);
            if(uri==null)
                uri = "";
            event.setData(NodeType.ATTRIBUTE, uri, localName, qname, reader.getAttributeValue(i));
            onEvent(event);
        }
    }

    public void onNamespaces(Event event, MyNamespaceSupport nsSupport){
        assert active;
        Enumeration<String> prefixes = hasParent() ? nsSupport.getDeclaredPrefixes() : nsSupport.getPrefixes();
        while(prefixes.hasMoreElements()){
            String prefix = prefixes.nextElement();
            String uri = nsSupport.getURI(prefix);
            event.setData(NodeType.NAMESPACE, "", prefix, prefix, uri);
            onEvent(event);
        }
    }

    private List<NodeItem> stack = new ArrayList<NodeItem>();
    void discard(long order){
        Iterator<NodeItem> iter = stack.iterator();
        boolean first = true;
        while(iter.hasNext()){
            NodeItem nodeItem = iter.next();
            if(nodeItem!=null){
                if(nodeItem.order==order){
                    if(--nodeItem.refCount>0) // no xml to be dicarded
                        return;
                    if(first){
                        /* detach from first decendant nodeitem */
                        nodeItem.xml = null;
                        while(iter.hasNext()){
                            nodeItem = iter.next();
                            if(nodeItem!=null){
                                if(nodeItem.xml!=null)
                                    removeFromParent(nodeItem.xml);
                                return;
                            }
                        }

                        /* comes here only when no descendant nodeitem was there */
                        clearCurNode();
                        active = false;
                    }
                    return;
                }
                first = false;
            }
        }
    }
    protected abstract void clearCurNode();
    protected abstract void removeFromParent(Object node);
    protected abstract boolean hasParent();
}
