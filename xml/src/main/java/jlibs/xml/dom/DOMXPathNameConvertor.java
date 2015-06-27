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

package jlibs.xml.dom;

import jlibs.core.graph.Convertor;
import jlibs.core.lang.StringUtil;
import jlibs.xml.Namespaces;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import javax.xml.namespace.NamespaceContext;

/**
 * @author Santhosh Kumar T
 */
public class DOMXPathNameConvertor implements Convertor<Node, String>{
    private NamespaceContext nsContext;

    public DOMXPathNameConvertor(NamespaceContext nsContext){
        this.nsContext = nsContext;
    }

    public DOMXPathNameConvertor(){}

    @Override
    public String convert(Node source){
        switch(source.getNodeType()){
            case Node.DOCUMENT_NODE:
                return "";
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                return "text()";
            case Node.COMMENT_NODE:
                return "comment()";
            case Node.PROCESSING_INSTRUCTION_NODE:
                ProcessingInstruction pi = (ProcessingInstruction)source;
                return "processing-instruction('"+pi.getTarget() +"')";
            case Node.ELEMENT_NODE:
                if(nsContext!=null){
                    String prefix = nsContext.getPrefix(source.getNamespaceURI());
                    String name = source.getLocalName();
                    return StringUtil.isEmpty(prefix) ? name : prefix+':'+name;
                }else
                    return source.getNodeName();
            case Node.ATTRIBUTE_NODE:
                if(Namespaces.URI_XMLNS.equals(source.getNamespaceURI()))
                    return "namespace::"+source.getLocalName();
                
                if(nsContext!=null){
                    String prefix = nsContext.getPrefix(source.getNamespaceURI());
                    String name = source.getLocalName();
                    return '@'+ (StringUtil.isEmpty(prefix) ? name : prefix+':'+name);
                }else
                    return '@'+source.getNodeName();
            case 13: /*Node.NAMESPACE_NODE*/
                return "namespace::"+source.getLocalName();
            default:
                return null;
        }
    }
}
