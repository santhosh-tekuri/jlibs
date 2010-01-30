/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.sax;

import jlibs.core.lang.StringUtil;
import jlibs.xml.sax.helpers.MyNamespaceSupport;
import jlibs.xml.xsl.TransformerUtil;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Stack;

/**
 * This class is used to write xml documents
 * 
 * @author Santhosh Kumar T
 */
public class XMLDocument{
    private SAXDelegate xml;

    public XMLDocument(SAXDelegate xml){
        this.xml = xml;
    }

    public XMLDocument(Result result, boolean omitXMLDeclaration, int indentAmount, String encoding) throws TransformerConfigurationException{
        TransformerHandler handler = TransformerUtil.newTransformerHandler(null, omitXMLDeclaration, indentAmount, encoding);
        handler.setResult(result);
        xml = new SAXDelegate(handler);
    }

    /*-------------------------------------------------[ Document ]---------------------------------------------------*/

    public XMLDocument startDocument() throws SAXException{
        nsSupport.reset();
        attrs.clear();
        elemStack.clear();
        elem = null;
        nsSupport.pushContext();

        xml.startDocument();
        mark();
        return this;
    }

    public XMLDocument endDocument() throws SAXException{
        release(0);
        xml.endDocument();
        return this;
    }

    /*-------------------------------------------------[ Namespaces ]---------------------------------------------------*/

    private MyNamespaceSupport nsSupport = new MyNamespaceSupport();

    public void suggestPrefix(String prefix, String uri){
        nsSupport.suggestPrefix(prefix, uri);
    }

    public String declarePrefix(String uri){
        String prefix = nsSupport.findPrefix(uri);
        if(prefix==null)
            prefix = nsSupport.declarePrefix(uri);
        return prefix;
    }

    public boolean declarePrefix(String prefix, String uri){
        return nsSupport.declarePrefix(prefix, uri);
    }

    private QName declareQName(String uri, String localPart){
        return new QName(uri, localPart, declarePrefix(uri));
    }

    public String toQName(String uri, String localPart){
        String prefix = declarePrefix(uri);
        return prefix.length()==0 ? localPart : prefix+':'+localPart;
    }

    private void startPrefixMapping(NamespaceSupport nsSupport) throws SAXException{
        Enumeration enumer = nsSupport.getDeclaredPrefixes();
        while(enumer.hasMoreElements()){
            String prefix = (String) enumer.nextElement();
            xml.startPrefixMapping(prefix, nsSupport.getURI(prefix));
        }
    }

    private void endPrefixMapping(NamespaceSupport nsSupport) throws SAXException{
        Enumeration enumer = nsSupport.getDeclaredPrefixes();
        while(enumer.hasMoreElements())
            xml.endPrefixMapping((String)enumer.nextElement());
    }

    /*-------------------------------------------------[ start-element ]---------------------------------------------------*/

    private Stack<QName> elemStack = new Stack<QName>();
    private QName elem;

    private int marks = -1;
    public int mark() throws SAXException{
        finishStartElement();
        elemStack.push(null);
        return ++marks;
    }

    public int release() throws SAXException{
        if(marks==-1 || elemStack.empty())
            throw new SAXException("no mark found to be released");
        endElements();
        if(elemStack.peek()!=null)
            throw new SAXException("expected </"+toString(elemStack.peek())+'>');
        elemStack.pop();
        return --marks;
    }

    public void release(int mark) throws SAXException{
        while(marks>=mark)
            release();
    }

    private String toString(QName qname){
        return qname.getPrefix().length()==0 ? qname.getLocalPart() : qname.getPrefix()+':'+qname.getLocalPart();
    }

    private void finishStartElement() throws SAXException{
        if(elem!=null){
            startPrefixMapping(nsSupport);
            nsSupport.pushContext();

            elemStack.push(elem);
            xml.startElement(elem.getNamespaceURI(), elem.getLocalPart(), toString(elem), attrs);
            elem = null;
            attrs.clear();
        }
    }

    public XMLDocument startElement(String name) throws SAXException{
        return startElement("", name);
    }

    public XMLDocument startElement(String uri, String name) throws SAXException{
        finishStartElement();
        elem = declareQName(uri, name);
        return this;
    }

    /*-------------------------------------------------[ Add-Element ]---------------------------------------------------*/

    public XMLDocument addElement(String name, String text, boolean cdata) throws SAXException{
        return addElement("", name, text, cdata);
    }

    public XMLDocument addElement(String uri, String name, String text, boolean cdata) throws SAXException{
        if(text!=null){
            startElement(uri, name);
            if(cdata)
                addCDATA(text);
            else
                addText(text);
            endElement();
        }
        return this;
    }

    public XMLDocument addElement(String name, String text) throws SAXException{
        return addElement("", name, text, false);
    }

    public XMLDocument addElement(String uri, String name, String text) throws SAXException{
        return addElement(uri, name, text, false);
    }

    public XMLDocument addCDATAElement(String name, String text) throws SAXException{
        return addElement("", name, text, true);
    }

    public XMLDocument addCDATAElement(String uri, String name, String text) throws SAXException{
        return addElement(uri, name, text, true);
    }

    /*-------------------------------------------------[ Attributes ]---------------------------------------------------*/

    private AttributesImpl attrs = new AttributesImpl();

    public XMLDocument addAttribute(String name, String value) throws SAXException{
        return addAttribute("", name, value);
    }

    public XMLDocument addAttribute(String uri, String name, String value) throws SAXException{
        if(elem==null)
            throw new SAXException("no start element found to associate this attribute");
        if(value!=null)
            attrs.addAttribute(uri, name, toQName(uri, name), "CDATA", value);
        return this;
    }

    /*-------------------------------------------------[ Text ]---------------------------------------------------*/

    public XMLDocument addText(String text) throws SAXException{
        if(!StringUtil.isEmpty(text)){
            finishStartElement();
            xml.characters(text.toCharArray(), 0, text.length());
        }
        return this;
    }

    public XMLDocument addCDATA(String text) throws SAXException{
        if(!StringUtil.isEmpty(text)){
            finishStartElement();
            xml.startCDATA();
            xml.characters(text.toCharArray(), 0, text.length());
            xml.endCDATA();
        }
        return this;
    }

    /*-------------------------------------------------[ end-element ]---------------------------------------------------*/

    private QName findEndElement() throws SAXException{
        finishStartElement();
        if(elemStack.empty() || elemStack.peek()==null)
            throw new SAXException("can't find matching start element");
        return elemStack.pop();
    }

    private XMLDocument endElement(QName qname) throws SAXException{
        xml.endElement(qname.getNamespaceURI(), qname.getLocalPart(), toString(qname));

        endPrefixMapping(nsSupport);
        nsSupport.popContext();
        return this;
    }

    public XMLDocument endElement(String uri, String name) throws SAXException{
        QName qname = findEndElement();
        if(!qname.getNamespaceURI().equals(uri) || !qname.getLocalPart().equals(name))
            throw new SAXException("expected </"+toString(qname)+'>');
        return endElement(qname);
    }

    public XMLDocument endElement(String name) throws SAXException{
        return endElement("", name);
    }

    public XMLDocument endElement() throws SAXException{
        return endElement(findEndElement());
    }

    /*-------------------------------------------------[ end-elements ]---------------------------------------------------*/

    public XMLDocument endElements(String uri, String name) throws SAXException{
        QName qname = findEndElement();
        while(true){
            endElement(qname);
            if(qname.getNamespaceURI().equals(uri) && qname.getLocalPart().equals(name))
                break;
        }
        return this;
    }

    public XMLDocument endElements(String name) throws SAXException{
        return endElements("", name);
    }

    public XMLDocument endElements() throws SAXException{
        finishStartElement();
        while(!elemStack.empty() && elemStack.peek()!=null)
            endElement();
        return this;
    }

    /*-------------------------------------------------[ Errors ]---------------------------------------------------*/

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void warning(String msg, Exception ex) throws SAXException{
        xml.warning(new SAXParseException(msg, null, ex));
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void error(String msg, Exception ex) throws SAXException{
        xml.error(new SAXParseException(msg, null, ex));
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void fatalError(String msg, Exception ex) throws SAXException{
        SAXParseException saxException = new SAXParseException(msg, null, ex);
        xml.fatalError(saxException);
        throw saxException;
    }

    /*-------------------------------------------------[ XML ]---------------------------------------------------*/

    public XMLDocument addXML(String xmlString, boolean excludeRoot) throws SAXException{
        if(!StringUtil.isWhitespace(xmlString))
            addXML(new InputSource(new StringReader(xmlString)), excludeRoot);
        return this;
    }

    public XMLDocument addXML(InputSource is, boolean excludeRoot) throws SAXException{
        finishStartElement();
        try{
            SAXDelegate delegate;
            if(excludeRoot){
                delegate = new SAXDelegate(xml){
                    private int depth = 0;
                    private NamespaceSupport nsSupport = new NamespaceSupport();

                    @Override public void startDocument(){}
                    @Override public void endDocument(){}
                    @Override public void setDocumentLocator(Locator locator){}

                    @Override
                    public void characters(char[] ch, int start, int length) throws SAXException{
                        if(depth!=1)
                            super.characters(ch, start, length);
                    }

                    @Override
                    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException{
                        if(depth!=1)
                            super.ignorableWhitespace(ch, start, length);
                    }

                    @Override
                    public void startPrefixMapping(String prefix, String uri) throws SAXException{
                        if(depth==0)
                            nsSupport.declarePrefix(prefix, uri);
                        else
                            super.startPrefixMapping(prefix, uri);
                    }

                    @Override
                    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException{
                        depth++;
                        if(depth>1){
                            if(depth==2)
                                XMLDocument.this.startPrefixMapping(nsSupport);
                            super.startElement(uri, localName, qName, atts);
                        }
                    }

                    @Override
                    public void endElement(String uri, String localName, String qName) throws SAXException{
                        if(depth>1){
                            super.endElement(uri, localName, qName);
                            if(depth==2)
                                XMLDocument.this.endPrefixMapping(nsSupport);
                        }
                        depth--;
                    }

                    @Override
                    public void endPrefixMapping(String prefix) throws SAXException{
                        if(depth>0)
                            super.endPrefixMapping(prefix);
                    }
                };
            }else{
                delegate = new SAXDelegate(xml){
                    @Override public void startDocument(){}
                    @Override public void endDocument(){}
                    @Override public void setDocumentLocator(Locator locator){}
                };
            }
            XMLReader reader = SAXUtil.newSAXParser(true, false, false).getXMLReader();
            SAXUtil.setHandler(reader, delegate);
            reader.parse(is);
        }catch(ParserConfigurationException ex){
            throw new SAXException(ex);
        }catch(IOException ex){
            throw new SAXException(ex);
        }
        return this;
    }

    /*-------------------------------------------------[ Others ]---------------------------------------------------*/

    public XMLDocument addProcessingInstruction(String target, String data) throws SAXException{
        xml.processingInstruction(target, data);
        return this;
    }

    public XMLDocument addComment(String text) throws SAXException{
        if(!StringUtil.isEmpty(text)){
            finishStartElement();
            xml.comment(text.toCharArray(), 0, text.length());
        }
        return this;
    }

    /*-------------------------------------------------[ SAXProducer ]---------------------------------------------------*/

    public XMLDocument add(SAXProducer saxProducer) throws SAXException{
        return add(saxProducer, (QName)null);
    }

    public XMLDocument add(SAXProducer saxProducer, String name) throws SAXException{
        return add(saxProducer, "", name);
    }

    public XMLDocument add(SAXProducer saxProducer, String uri, String name) throws SAXException{
        return add(saxProducer, new QName(uri, name));
    }

    public XMLDocument add(SAXProducer saxProducer, QName qname) throws SAXException{
        if(saxProducer!=null){
            mark();
            saxProducer.serializeTo(qname, this);
            endElements();
            release();
        }
        return this;
    }

    /*-------------------------------------------------[ DTD ]---------------------------------------------------*/

    public XMLDocument addPublicDTD(String name, String publicId, String systemID) throws SAXException{
        xml.startDTD(name, publicId, systemID);
        xml.endDTD();
        return this;
    }

    public XMLDocument addSystemDTD(String name, String systemId) throws SAXException{
        xml.startDTD(name, null, systemId);
        xml.endDTD();
        return this;
    }

    /*-------------------------------------------------[ Testing ]---------------------------------------------------*/

    public static void main(String[] args) throws Exception{
        String google = "http://google.com";
        String yahoo = "http://yahoo.com";

        XMLDocument xml = new XMLDocument(new StreamResult(System.out), false, 4, null);

        xml.startDocument();{
            xml.addProcessingInstruction("san", "test='1.2'");
            xml.declarePrefix("google", google);
            xml.declarePrefix("yahoo", yahoo);
            xml.declarePrefix("http://msn.com");

            xml.startElement(google, "hello");{
                xml.addAttribute("name", "value");
                xml.addElement("xyz", "helloworld");
                xml.addElement(google, "hai", "test");
                xml.addXML(new InputSource("xml/xsds/note.xsd"), true);
                xml.addComment("this is comment");
                xml.addCDATA("this is sample cdata");
            }
        }
        xml.endDocument();
    }
}
