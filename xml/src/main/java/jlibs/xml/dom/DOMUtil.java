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

import jlibs.core.lang.Util;
import jlibs.xml.Namespaces;
import jlibs.xml.sax.SAXDelegate;
import jlibs.xml.xsl.TransformerUtil;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.BitSet;

import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;

/**
 * @author Santhosh Kumar T
 */
public class DOMUtil{
    /*-------------------------------------------------[ Builder ]---------------------------------------------------*/

    public static DocumentBuilder newDocumentBuilder(boolean nsAware, boolean validating) throws ParserConfigurationException{
        return newDocumentBuilder(nsAware, validating, false, false);
    }

    public static DocumentBuilder newDocumentBuilder(boolean nsAware, boolean validating, boolean coalescing, boolean ignoreComments) throws ParserConfigurationException{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(nsAware);
        factory.setValidating(validating);
        factory.setCoalescing(coalescing);
        factory.setIgnoringComments(ignoreComments);
        return factory.newDocumentBuilder();
    }

    /*-------------------------------------------------[ Namespace ]---------------------------------------------------*/

    public static boolean isNamespaceDeclaration(Attr attr){
        return Namespaces.URI_XMLNS.equals(attr.getNamespaceURI());
    }

    public static Attr findNamespaceDeclarationForPrefix(Node node, String prefix){
        while(node!=null){
            NamedNodeMap attrs = node.getAttributes();
            if(attrs!=null){
                for(int i=attrs.getLength()-1; i>=0; i--){
                    Attr attr = (Attr)attrs.item(i);
                    if(isNamespaceDeclaration(attr) && prefix.equals(attr.getLocalName()))
                        return attr;
                }
            }
            node = node.getParentNode();
        }
        return null;
    }

    public static Attr findNamespaceDeclarationForURI(Node node, String uri){
        while(node!=null){
            NamedNodeMap attrs = node.getAttributes();
            if(attrs!=null){
                for(int i=attrs.getLength()-1; i>=0; i--){
                    Attr attr = (Attr)attrs.item(i);
                    if(isNamespaceDeclaration(attr) && uri.equals(attr.getNodeValue()))
                        return attr;
                }
            }
            node = node.getParentNode();
        }
        return null;
    }

    /*-------------------------------------------------[ Equality ]---------------------------------------------------*/

    public static boolean equals(Node node1, Node node2){
        if(node1==node2)
            return true;
        Node n1 = node1;
        Node n2 = node2;

        while(true){
            if(!shallowEquals(n1, n2))
                return false;
            if(n1==null){
                assert n2==null;
                return true;
            }
            Node n1Child = null;
            if(n1.getNodeType()==Node.DOCUMENT_NODE || n1.getNodeType()==Node.ELEMENT_NODE)
                n1Child = n1.getFirstChild(); // the jdk's dom impl returns non-null child for attribute etc
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
                    Node n1Sibling = n1.getNextSibling();
                    Node n2Sibling = n2.getNextSibling();
                    if(!shallowEquals(n1Sibling, n2Sibling))
                        return false;
                    if(n1Sibling==null){
                        assert n2Sibling==null;
                        Node n1Parent = n1.getParentNode();
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

    private static boolean shallowEquals(Node n1, Node n2){
        if(n1==n2)
            return true;
        if(n1==null || n2==null)
            return false;

        int type1 = n1.getNodeType();
        if(type1==Node.CDATA_SECTION_NODE)
            type1 = Node.TEXT_NODE;

        int type2 = n2.getNodeType();
        if(type2==Node.CDATA_SECTION_NODE)
            type2 = Node.TEXT_NODE;

        if(type1!=type2)
            return false;

        switch(type1){
            case Node.PROCESSING_INSTRUCTION_NODE:
                ProcessingInstruction pi1 = (ProcessingInstruction)n1;
                ProcessingInstruction pi2 = (ProcessingInstruction)n2;
                if(!pi1.getTarget().equals(pi1.getTarget())
                        || !pi1.getData().equals(pi2.getData()))
                    return false;
                break;
            case Node.COMMENT_NODE:
                Comment comment1 = (Comment)n1;
                Comment comment2 = (Comment)n2;
                if(!comment1.getData().equals(comment2.getData()))
                    return false;
                break;
            case Node.ELEMENT_NODE:
                Element element1 = (Element)n1;
                Element element2 = (Element)n2;
                String namespaceURI1 = element1.getNamespaceURI();
                if(namespaceURI1==null)
                    namespaceURI1 = "";
                String namespaceURI2 = element2.getNamespaceURI();
                if(namespaceURI2==null)
                    namespaceURI2 = "";
                if(!namespaceURI1.equals(namespaceURI2)
                        || !element1.getLocalName().equals(element2.getLocalName()))
                    return false;


                NamedNodeMap attrs1 = element1.getAttributes();
                NamedNodeMap attrs2 = element2.getAttributes();
                BitSet bitSet = new BitSet();
                for(int i=0; i<attrs1.getLength(); i++){
                    Attr attr1 = (Attr)attrs1.item(i);
                    if(isNamespaceDeclaration(attr1))
                        continue;
                    namespaceURI1 = attr1.getNamespaceURI();
                    if(namespaceURI1==null)
                        namespaceURI1 = "";
                    String localName1 = attr1.getLocalName();
                    String value1 = attr1.getNodeValue();

                    int found = -1;
                    for(int j=0; j<attrs2.getLength(); j++){
                        Attr attr2 = (Attr)attrs2.item(j);
                        namespaceURI2 = attr2.getNamespaceURI();
                        if(namespaceURI2==null)
                            namespaceURI2 = "";
                        String localName2 = attr2.getLocalName();
                        if(namespaceURI1.equals(namespaceURI2) && localName1.equals(localName2)){
                            String value2 = attr2.getNodeValue();
                            if(!value1.equals(value2))
                                return false;
                            found = j;
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
                Attr attr1 = (Attr)n1;
                Attr attr2 = (Attr)n2;
                namespaceURI1 = attr1.getNamespaceURI();
                if(namespaceURI1==null)
                    namespaceURI1 = "";
                namespaceURI2 = attr2.getNamespaceURI();
                if(namespaceURI2==null)
                    namespaceURI2 = "";
                if(!namespaceURI1.equals(namespaceURI2)
                        || !attr1.getLocalName().equals(attr2.getLocalName())
                        || !attr1.getNodeValue().equals(attr2.getNodeValue()))
                    return false;
                break;
            case Node.TEXT_NODE:
                if(!n1.getNodeValue().equals(n2.getNodeValue()))
                    return false;
        }

        return true;
    }

    /*-------------------------------------------------[ Serialize ]---------------------------------------------------*/

    public static String toString(Node node){
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        serialize(node, new PrintStream(bout, true));
        return bout.toString();
    }

    public static void serialize(Node node, PrintStream out){
        switch(node.getNodeType()){
            case Node.ATTRIBUTE_NODE:
                out.print(node.getNodeName());
                out.print("=\"");
                out.print(node.getNodeValue().replace("\"", "&quot;"));
                out.print("\"");
                break;
            case 13: // NodeType.NAMESPACE
                out.print("xmlns:");
                out.print(node.getLocalName());
                out.print("=\"");
                out.print(node.getNodeValue().replace("\"", "&quot;"));
                out.print("\"");
                break;
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                out.print(node.getTextContent());
                break;
            case Node.COMMENT_NODE:
                out.print("<!--");
                out.print(node.getNodeValue());
                out.print("-->");
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                out.print("<?");
                out.print(node.getNodeName());
                out.print(' ');
                out.print(node.getNodeValue());
                out.print("?>");
                break;
            default:
                try{
                    Transformer transformer = TransformerUtil.newTransformer(null, true, 0, null);
                    transformer.transform(new DOMSource(node), new StreamResult(out));
                }catch(TransformerException ex){
                    throw new RuntimeException(ex);
                }
        }
    }

    /*-------------------------------------------------[ Misc ]---------------------------------------------------*/

    public static void toSAX(Node root, SAXDelegate handler) throws SAXException{
        DOMLocator locator = new DOMLocator();

        switch(root.getNodeType()){
            case Node.DOCUMENT_NODE:
                break;
            case Node.ELEMENT_NODE:
                handler.setDocumentLocator(locator);
                handler.startDocument();
                if(root.getParentNode()!=null){
                    DOMNamespaceContext.Iterator iter = new DOMNamespaceContext.Iterator(root.getParentNode());
                    while(iter.hasNext())
                        handler.startPrefixMapping(iter.next(), iter.getNamespaceURI());
                }
                break;
            default:
                throw new IllegalArgumentException("Node must be Document or Element");
        }

        Node node = root;
        AttributesImpl attrs = new AttributesImpl();
        while(node!=null){
            locator.node = node;
            switch(node.getNodeType()){
                case Node.DOCUMENT_NODE:
                    handler.setDocumentLocator(locator);
                    handler.startDocument();
                    break;
                case Node.DOCUMENT_TYPE_NODE:
                    DocumentType docType = (DocumentType)node;
                    handler.startDTD(docType.getName(), docType.getPublicId(), docType.getSystemId());
                    break;
                case Node.ELEMENT_NODE:
                    attrs.clear();
                    NamedNodeMap nodeAttrs = node.getAttributes();
                    if(nodeAttrs!=null){
                        for(int i=nodeAttrs.getLength()-1; i>=0; i--){
                            Node attr = nodeAttrs.item(i);
                            if(XMLNS_ATTRIBUTE.equals(attr.getNodeName()))
                                handler.startPrefixMapping("", attr.getNodeValue());
                            else if(XMLNS_ATTRIBUTE.equals(attr.getPrefix()))
                                handler.startPrefixMapping(attr.getLocalName(), attr.getNodeValue());
                            else
                                attrs.addAttribute(attr.getNamespaceURI(), attr.getLocalName(), attr.getNodeName(), "CDATA", attr.getNodeValue());
                        }
                    }
                    handler.startElement(node.getNamespaceURI(), node.getLocalName(), node.getNodeName(), attrs);
                    break;
                case Node.TEXT_NODE:
                    char chars[] = node.getNodeValue().toCharArray();
                    handler.characters(chars, 0, chars.length);
                    break;
                case Node.CDATA_SECTION_NODE:
                    chars = node.getNodeValue().toCharArray();
                    handler.startCDATA();
                    handler.characters(chars, 0, chars.length);
                    handler.endCDATA();
                    break;
                case Node.PROCESSING_INSTRUCTION_NODE:
                    ProcessingInstruction pi = (ProcessingInstruction)node;
                    handler.processingInstruction(pi.getTarget(), pi.getData());
                    break;
                case Node.COMMENT_NODE:
                    chars = node.getNodeValue().toCharArray();
                    handler.comment(chars, 0, chars.length);
            }
            NodeList children = node.getChildNodes();
            if(children!=null && children.getLength()>0)
                node = children.item(0);
            else{
                while(true){
                    locator.node = node;
                    switch(node.getNodeType()){
                        case Node.ELEMENT_NODE:
                            handler.endElement(node.getNamespaceURI(), node.getLocalName(), node.getNodeName());
                            NamedNodeMap nodeAttrs = node.getAttributes();
                            if(nodeAttrs!=null){
                                for(int i=nodeAttrs.getLength()-1; i>=0; i--){
                                    Node attr = nodeAttrs.item(i);
                                    if(XMLNS_ATTRIBUTE.equals(attr.getNodeName()))
                                        handler.endPrefixMapping("");
                                    else if(XMLNS_ATTRIBUTE.equals(attr.getPrefix()))
                                        handler.endPrefixMapping(attr.getLocalName());
                                }
                            }
                            break;
                        case Node.DOCUMENT_TYPE_NODE:
                            handler.endDTD();
                            break;
                    }
                    if(node==root){
                        if(root.getNodeType()==Node.ELEMENT_NODE && root.getParentNode()!=null){
                            DOMNamespaceContext.Iterator iter = new DOMNamespaceContext.Iterator(root.getParentNode());
                            while(iter.hasNext())
                                handler.endPrefixMapping(iter.next());
                        }
                        handler.endDocument();
                        return;
                    }
                    if(node.getNextSibling()!=null){
                        node = node.getNextSibling();
                        break;
                    }else
                        node = node.getParentNode();
                }
            }
        }
    }

    public static int getPosition(Element elem){
        int pos = 1;
        NodeList list = elem.getParentNode().getChildNodes();
        for(int i=0; i<list.getLength(); i++){
            Node node = list.item(i);
            if(node==elem)
                break;
            if(node instanceof Element
                    && Util.equals(node.getNamespaceURI(), elem.getNamespaceURI())
                    && node.getLocalName().equals(elem.getLocalName()))
                pos++;
        }
        return pos;
    }

    public static void main(String[] args) throws Exception{
        String file = "/Volumes/Backup/projects/office/trunk/fountainhead/gateway/testsuites/newregression/messagevalidation/resources/anothergsearch.wsdl";
        Document doc = newDocumentBuilder(true, false, false, false).parse(file);
        SAXDelegate delegate = new SAXDelegate();
        TransformerHandler handler = TransformerUtil.newTransformerHandler(null, true, 0, null);
        delegate.setHandler(handler);
        DOMResult result = new DOMResult();
        handler.setResult(result);
        Node node = doc;
        node = doc.getDocumentElement().getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "binding").item(0);
        toSAX(node, delegate);
        node = result.getNode();
        TransformerUtil.newTransformer(null, true, 0, null).transform(new DOMSource(node), new StreamResult(System.out));
    }
}
