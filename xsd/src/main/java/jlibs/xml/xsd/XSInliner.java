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

package jlibs.xml.xsd;

import jlibs.core.io.CharArrayWriter2;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.OS;
import jlibs.core.net.URLUtil;
import jlibs.xml.Namespaces;
import jlibs.xml.sax.SAXDelegate;
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.sax.helpers.MyNamespaceSupport;
import jlibs.xml.xsl.TransformerUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class XSInliner extends SAXDelegate{
    private static final String SCHEMA_LOCATION = "schemaLocation";
    private static final String IMPORT = "import";

    private String outputSystemID;
    private static final List<String> TOP_ELEMS = Arrays.asList(IMPORT, "redefine", "annotation");
    public Document inline(InputSource input, String outputSystemID) throws IOException, SAXException, ParserConfigurationException, TransformerConfigurationException{
        DOMResult result = new DOMResult();
        TransformerHandler handler = TransformerUtil.newTransformerHandler(null, true, 4, null);
        setHandler(handler);
        handler.setResult(result);
        this.outputSystemID = outputSystemID==null ? input.getSystemId() : outputSystemID;
        if(this.outputSystemID!=null)
            this.outputSystemID = URLUtil.toURL(this.outputSystemID).toString();
        parse(input);

        Document doc = (Document)result.getNode();
        Element root = doc.getDocumentElement();

        // get children xs:import, xs:redefine and xs:annotation
        NodeList children = root.getChildNodes();
        List<Node> list = new ArrayList<Node>();
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(Namespaces.URI_XSD.equals(child.getNamespaceURI()) && TOP_ELEMS.contains(child.getLocalName()))
                list.add(child);
        }

        // delete the children found
        for(Node node: list)
            node.getParentNode().removeChild(node);

        // add the children in the beginning of root children
        for(int i=list.size()-1; i>=0; i--){
             Node node = list.get(i);
            if(root.getFirstChild()==null)
                root.appendChild(node);
            else
                root.insertBefore(node, root.getFirstChild());
        }

        return doc;
    }

    public Document inline(InputSource input) throws IOException, SAXException, ParserConfigurationException, TransformerConfigurationException{
        return inline(input, null);
    }

    private void parse(InputSource source) throws ParserConfigurationException, IOException, SAXException{
        SAXParser parser = SAXUtil.newSAXParser(true, false, false);
        SAXUtil.setHandler(parser.getXMLReader(), this);
        parser.getXMLReader().parse(source);
    }

    Context mainContext;
    Context curContext;
    static class Context{
        Context parent;

        MyNamespaceSupport nsSupport = new MyNamespaceSupport();
        String systemID;
        String targetNamespace;
        int depth;

        Context(Context parent, String systemID){
            this.parent = parent;
            this.systemID = systemID;
        }

        String resolve(String location){
            return URLUtil.resolve(systemID, location).toString();
        }

        public void setTargetNamespace(String namespace){
            targetNamespace = namespace==null ? "" : namespace;
        }
    }

    private String tempSystemID;
    @Override
    public void setDocumentLocator(Locator locator){
        tempSystemID = locator.getSystemId();
    }

    @Override
    public void startDocument() throws SAXException{
        curContext = new Context(curContext, tempSystemID);
        if(mainContext==null)
            mainContext = curContext;
        curContext.nsSupport.startDocument();

        if(isMainDocument())
            super.startDocument();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException{
        curContext.nsSupport.startPrefixMapping(prefix, uri);
        if(!isMainDocument()){
            if(mainContext.nsSupport.findPrefix(uri)!=null)
                return;
            prefix = mainContext.nsSupport.startPrefixMapping(uri);
            if(curContext.depth==0)
                return;
        }
        super.startPrefixMapping(prefix, uri);
    }

    private static final List<String> QNAME_ATTS = Arrays.asList("type", "ref", "base");

    private Set<String> included = new HashSet<String>();
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException{
        curContext.depth++;
        curContext.nsSupport.startElement();
        Enumeration<String> declaredPrefixes = mainContext.nsSupport.getDeclaredPrefixes();
        if(!isMainDocument())
            mainContext.nsSupport.startElement();
        if(isInclude(uri, localName)){
            try{
                InputSource importSource = null;
                String location = curContext.resolve(atts.getValue(SCHEMA_LOCATION));
                String importElem = "<"+qName+" schemaLocation=\""+atts.getValue(SCHEMA_LOCATION)+"\">";
                comment(importElem.toCharArray(), 0, importElem.length());
                if(!included.add(location))
                    return;
                EntityResolver resolver = getEntityResolver();
                if(resolver!=null)
                    importSource = resolver.resolveEntity(null, location);
                if(importSource==null)
                    importSource = new InputSource(location);
                parse(importSource);
                return;
            }catch(Exception ex){
                if(ex instanceof SAXException)
                    throw (SAXException)ex;
                throw new SAXException(ex);
            }
        }

        if(isRoot(uri, localName)){
            curContext.setTargetNamespace(atts.getValue("targetNamespace"));
            if(isMainDocument() && !mainContext.targetNamespace.isEmpty()){
                if(mainContext.nsSupport.findPrefix(mainContext.targetNamespace)==null){
                    String prefix = mainContext.nsSupport.declarePrefix(mainContext.targetNamespace);
                    super.startPrefixMapping(prefix, mainContext.targetNamespace);
                }
            }
        }

        if(isMainDocument()){
             if(isImport(uri, localName) && atts.getValue(SCHEMA_LOCATION)!=null){
                 AttributesImpl newAtts = new AttributesImpl(atts);
                 String attValue = atts.getValue(SCHEMA_LOCATION);
                 String location = curContext.resolve(attValue);
                 attValue = URLUtil.relativize(outputSystemID, location).toString();
                 newAtts.setValue(atts.getIndex(SCHEMA_LOCATION), attValue);
                 atts = newAtts;
             }
        }else{
            if(isRoot(uri, localName))
                return;
            qName = mainContext.nsSupport.toQName(uri, localName);
            AttributesImpl newAtts = new AttributesImpl();
            for(int i=0; i<atts.getLength(); i++){
                String attURI = atts.getURI(i);
                String attLocalName = atts.getLocalName(i);
                String attQName = attURI.isEmpty() ? attLocalName : mainContext.nsSupport.toQName(attURI, attLocalName);
                String attValue = atts.getValue(i);
                if(attURI.isEmpty()){
                    if(uri.equals(Namespaces.URI_XSD) && QNAME_ATTS.contains(attLocalName)){
                        QName qnameObj = curContext.nsSupport.toQName(attValue);
                        String qnameURI = qnameObj.getNamespaceURI();
                        if(qnameURI.isEmpty() && curContext.targetNamespace.isEmpty())
                            qnameURI = mainContext.targetNamespace;
                        attValue = mainContext.nsSupport.toQName(qnameURI, qnameObj.getLocalPart());
                    }else if(SCHEMA_LOCATION.equals(attLocalName) && isImport(uri, localName)){
                        String location = curContext.resolve(attValue);
                        attValue = URLUtil.relativize(outputSystemID, location).toString();
                    }
                }
                newAtts.addAttribute(attURI, attLocalName, attQName, atts.getType(i), attValue);
            }
            atts = newAtts;
        }

        if(!isMainDocument() && curContext.depth==2){
            while(declaredPrefixes.hasMoreElements()){
                String prefix = declaredPrefixes.nextElement();
                String u = mainContext.nsSupport.findURI(prefix);
                super.startPrefixMapping(prefix, u);
            }
        }
        super.startElement(uri, localName, qName, atts);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        curContext.depth--;
        curContext.nsSupport.endElement();
        if(!isMainDocument())
            mainContext.nsSupport.endElement();
        if(isInclude(uri, localName)){
            String importElem = "</"+qName+">";
            comment(importElem.toCharArray(), 0, importElem.length());
            return;
        }
        if(!isMainDocument()){
            if(isRoot(uri, localName))
                return;
            qName = mainContext.nsSupport.toQName(uri, localName);
        }
        super.endElement(uri, localName, qName);
        if(!isMainDocument() && curContext.depth==1){
            Enumeration<String> declaredPrefixes = mainContext.nsSupport.getDeclaredPrefixes();
            while(declaredPrefixes.hasMoreElements()){
                String prefix = declaredPrefixes.nextElement();
                super.endPrefixMapping(prefix);
            }
        }
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException{
        if(isMainDocument())
            super.endPrefixMapping(prefix);
    }

    @Override
    public void endDocument() throws SAXException{
        if(isMainDocument())
            super.endDocument();
        curContext = curContext.parent;
    }

    private boolean isMainDocument(){
        return curContext==mainContext;
    }

    private boolean isRoot(String uri, String localName){
        return Namespaces.URI_XSD.equals(uri) && "schema".equals(localName);
    }

    private boolean isInclude(String uri, String localName){
        return Namespaces.URI_XSD.equals(uri) && "include".equals(localName);
    }

    private boolean isImport(String uri, String localName){
        return Namespaces.URI_XSD.equals(uri) && IMPORT.equals(localName);
    }

    public static InputSource include(String targetNamespace, String... systemIDs) throws TransformerConfigurationException{
        CharArrayWriter2 writer = new CharArrayWriter2();
        XSDocument doc = new XSDocument(new StreamResult(writer), true, 4, null);
        try{
            doc.startDocument();
            doc.startSchema(targetNamespace);
            for(String systemID: systemIDs)
                doc.addInclude(URLUtil.toURL(systemID).toString());
            doc.endSchema();
            doc.endDocument();
        }catch(SAXException ex){
            throw new ImpossibleException(ex);
        }
        return new InputSource(writer.toCharSequence().asReader());
    }

    public static void main(String[] args) throws Exception{
        if(args.length==0){
            System.err.println("Usage:");
            System.err.println("\txsd-inline."+(OS.get().isWindows()?"bat":"sh")+" <xsd-file>");
            System.err.println("\txsd-inline."+(OS.get().isWindows()?"bat":"sh")+" <target-namespace> <xsd-file> ...");
            System.exit(1);
        }

        InputSource source;
        if(args.length==1)
            source = new InputSource(args[0]);
        else{
            String targetNamespace = args[0];
            String xsdFiles[] = Arrays.copyOfRange(args, 1, args.length);
            source = XSInliner.include(targetNamespace, xsdFiles);
        }
        Document doc = new XSInliner().inline(source, "temp.xsd");
        TransformerUtil.newTransformer(null, true, 0, null)
                .transform(new DOMSource(doc), new StreamResult(System.out));
        System.out.println();
    }
}
