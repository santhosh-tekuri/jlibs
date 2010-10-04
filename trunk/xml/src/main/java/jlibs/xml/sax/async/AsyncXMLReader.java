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

package jlibs.xml.sax.async;

import jlibs.core.io.IOUtil;
import jlibs.core.net.URLUtil;
import jlibs.nbp.Chars;
import jlibs.nbp.NBHandler;
import jlibs.xml.ClarkName;
import jlibs.xml.NamespaceMap;
import jlibs.xml.sax.AbstractXMLReader;
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.xsl.TransformerUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.Locator2;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.XMLConstants;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
@SuppressWarnings({"ThrowableInstanceNeverThrown"})
public class AsyncXMLReader extends AbstractXMLReader implements NBHandler<SAXException>, Locator2{
    private static Map<String, char[]> defaultEntities = new HashMap<String, char[]>();
    static{
        defaultEntities.put("amp",  new char[]{ '&' });
        defaultEntities.put("lt",   new char[]{ '<' });
        defaultEntities.put("gt",   new char[]{ '>' });
        defaultEntities.put("apos", new char[]{ '\'' });
        defaultEntities.put("quot", new char[]{ '"' });
    }

    private XMLScanner scanner = new XMLScanner(this){
        protected void consumed(int ch){
            consumed = true;
            int line = location.getLineNumber();
            boolean addToBuffer = location.consume(ch);
            if(addToBuffer && buffer.isBufferring())
                buffer.append(location.getLineNumber()>line ? '\n' : ch);
        }
    };

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException{
        try{
            super.setFeature(name, value);
        } catch(SAXNotRecognizedException e){
            // ignore
        }
    }

    public Writer getWriter() throws SAXException{
        scanner.setRule(XMLScanner.RULE_DOCUMENT);
        documentStart();
        return scanner;
    }

    @Override
    public void parse(InputSource input) throws IOException, SAXException{
        Reader charStream = input.getCharacterStream();
        if(charStream !=null)
            IOUtil.pump(charStream, getWriter(), true, true);
        else{
            InputStream inputStream = input.getByteStream();
            if(inputStream!=null)
                parse(inputStream);
            else
                parse(input.getSystemId());
        }
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException{
        // special handling for http url's like redirect, get encoding information from http headers
        parse(URLUtil.toURL(systemId).openStream());
    }

    private void parse(InputStream in) throws IOException, SAXException{
        IOUtil.pump(new InputStreamReader(in, IOUtil.UTF_8), getWriter(), true, true);
    }

    /*-------------------------------------------------[ Locator ]---------------------------------------------------*/

    @Override
    public String getPublicId(){
        return null;
    }

    @Override
    public String getSystemId(){
        return null;
    }

    @Override
    public int getLineNumber(){
        return scanner.location.getLineNumber();
    }

    @Override
    public int getColumnNumber(){
        return scanner.location.getColumnNumber();
    }

    @Override
    public String getXMLVersion(){
        return "1.0";
    }

    @Override
    public String getEncoding(){
        return encoding;
    }

    /*-------------------------------------------------[ Document ]---------------------------------------------------*/

    private void documentStart() throws SAXException{
        encoding = "UTF-8";
        clearQName();
        value.setLength(0);
        singleQuote = true;
        valueStarted = false;

        namespaces = new NamespaceMap();
        attributes.clear();

        elementsPrefixes.clear();
        elementsLocalNames.clear();
        elementsQNames.clear();

        piTarget = null;
        dtdRoot = null;
        systemID = null;
        publicID = null;
        notationName = null;

        entityName = null;
        entities.clear();
        entityScannerCount = 0;

        handler.setDocumentLocator(this);
        handler.startDocument();
    }

    /*-------------------------------------------------[ XML Decleration ]---------------------------------------------------*/

    void version(Chars data) throws SAXException{
        if(!"1.0".contentEquals(data))
            fatalError("Unsupported XML Version: "+data);
    }

    private String encoding;
    void encoding(Chars data) throws SAXException{
        encoding = data.toString();
    }

    void standalone(Chars data){
        System.out.println("standalone: "+data);
    }

    void xdeclEnd(){
        System.out.println("xdeclEnd");
    }

    /*-------------------------------------------------[ QName ]---------------------------------------------------*/

    private String prefix = "";
    void prefix(Chars data){
        prefix = data.toString();
    }

    private String localName;
    void localName(Chars data){
        localName = data.toString();
    }

    private String qname;
    void qname(Chars data){
        qname = data.toString();
    }

    private void clearQName(){
        prefix = "";
        localName = null;
        qname = null;
    }

    /*-------------------------------------------------[ Value ]---------------------------------------------------*/

    private StringBuilder value = new StringBuilder();
    private boolean singleQuote = true;
    private boolean valueStarted = true;
    private boolean entityValue = false;

    void valueStart(){
        value.setLength(0);
        singleQuote = true;
        valueStarted = true;
        entityValue = false;
    }

    void entityValue(){
        entityValue = true;
    }

    void doubleQuoteValue(){
        singleQuote = false;
    }

    void rawValue(Chars data){
        char[] chars = data.array();
        int end = data.offset() + data.length();
        for(int i=data.offset(); i<end; i++){
            char ch = chars[i];
            if(ch=='\n' || ch=='\r' || ch=='\t')
                ch = ' ';
            value.append(ch);
        }
    }

    private boolean isValid(int ch){
        return (ch==0x9 || ch==0xa || ch==0xd) || (ch>=0x20 && ch<=0xd7ff) || (ch>=0xe000 && ch<=0xfffd) || (ch>=0x10000 && ch<=0x10ffff);
    }

    void hexCode(Chars data) throws SAXException{
        int codePoint = Integer.parseInt(data.toString(), 16);
        if(!isValid(codePoint))
            fatalError("invalid xml character");
        if(valueStarted)
            value.appendCodePoint(codePoint);
        else{
            char chars[] = Character.toChars(codePoint);
            handler.characters(chars, 0, chars.length);
        }
    }

    void asciiCode(Chars data) throws SAXException{
        int codePoint = Integer.parseInt(data.toString(), 10);
        if(!isValid(codePoint))
            fatalError("invalid xml character");
        if(valueStarted)
            value.appendCodePoint(codePoint);
        else{
            char chars[] = Character.toChars(codePoint);
            handler.characters(chars, 0, chars.length);
        }
    }

    private int entityScannerCount = 0;
    @SuppressWarnings({"ConstantConditions"})
    void entityReference(Chars data) throws SAXException{
        String entity = data.toString();

        char[] entityValue = defaultEntities.get(entity);
        if(entityValue!=null){
            if(valueStarted)
                value.append(entityValue);
            else
                handler.characters(entityValue, 0, entityValue.length);
        }else{
            entityValue = entities.get(entity);
            if(entityValue==null)
                fatalError("Undefined entityReference: "+entity);

            int rule;
            if(valueStarted){
                if(AsyncXMLReader.this.entityValue)
                    rule = singleQuote ? XMLScanner.RULE_Q_ENTITY_VALUE_ENTITY : XMLScanner.RULE_DQ_ENTITY_VALUE_ENTITY;
                else{
                    char chars[] = new char[entityValue.length];
                    for(int i=chars.length-1; i>=0; i--){
                        char ch = entityValue[i];
                        if(ch=='\n' || ch=='\r' || ch=='\t')
                            ch = ' ';
                        chars[i] = ch;
                    }
                    entityValue = chars;

                    rule = singleQuote ? XMLScanner.RULE_Q_VALUE_ENTITY : XMLScanner.RULE_DQ_VALUE_ENTITY;
                }
            }else
                 rule = XMLScanner.RULE_ELEM_ENTITY;

            entityScannerCount++;
            try{
                XMLScanner entityValueScanner = new XMLScanner(this);
                entityValueScanner.setRule(rule);
                entityValueScanner.write(entityValue);
                entityValueScanner.close();
            }catch(IOException ex){
                throw new RuntimeException(ex);
            }finally{
                entityScannerCount--;
            }
        }
    }

    void valueEnd(){
        valueStarted = false;
    }

    /*-------------------------------------------------[ Start Element ]---------------------------------------------------*/

    private NamespaceMap namespaces = new NamespaceMap();
    private AttributesImpl attributes = new AttributesImpl();

    private Deque<String> elementsPrefixes = new ArrayDeque<String>();
    private Deque<String> elementsLocalNames = new ArrayDeque<String>();
    private Deque<String> elementsQNames = new ArrayDeque<String>();

    void attributesStart(){
        elementsPrefixes.push(prefix);
        elementsLocalNames.push(localName);
        elementsQNames.push(qname);
        clearQName();

        namespaces = new NamespaceMap(namespaces);
        attributes.clear();
    }

    void attributeEnd() throws SAXException{
        String value = this.value.toString();
        if(qname.equals("xmlns")){
            namespaces.put("", value);
            handler.startPrefixMapping("", value);
        }else if(prefix.equals("xmlns")){
            if(localName.equals(XMLConstants.XML_NS_PREFIX)){
                if(!value.equals(XMLConstants.XML_NS_URI)){
                    clearQName();
                    fatalError("prefix "+XMLConstants.XML_NS_PREFIX+" must refer to "+XMLConstants.XML_NS_URI);
                }
            }else if(localName.equals(XMLConstants.XMLNS_ATTRIBUTE)){
                clearQName();
                fatalError("prefix "+XMLConstants.XMLNS_ATTRIBUTE+" must not be declared");
            }else{
                if(value.equals(XMLConstants.XML_NS_URI)){
                    clearQName();
                    fatalError(XMLConstants.XML_NS_URI+" must be bound to "+XMLConstants.XML_NS_PREFIX);
                }else if(value.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)){
                    clearQName();
                    fatalError(XMLConstants.XMLNS_ATTRIBUTE_NS_URI+" must be bound to "+XMLConstants.XMLNS_ATTRIBUTE);
                }else{
                    namespaces.put(localName, value);
                    handler.startPrefixMapping(localName, value);
                }
            }
        }else
            attributes.addAttribute(prefix, localName, qname, "CDATA", value);

        clearQName();
    }

    private Set<String> attrClarkNames = new HashSet<String>();
    void attributesEnd() throws SAXException{
        attrClarkNames.clear();
        int attrCount = attributes.getLength();
        for(int i=0; i<attrCount; i++){
            String prefix = attributes.getURI(i);
            String uri = namespaces.getNamespaceURI(prefix);
            if(uri==null)
                fatalError("Unbound prefix: "+prefix);
            attributes.setURI(i, uri);
            String clarkName = ClarkName.valueOf(uri, attributes.getLocalName(i));
            if(!attrClarkNames.add(clarkName))
                fatalError("Attribute "+clarkName+" appears more than once in element");
        }

        String prefix = elementsPrefixes.peek();
        String namespaceURI = namespaces.getNamespaceURI(prefix);
        if(namespaceURI==null)
            fatalError("Unbound prefix: "+prefix);
        handler.startElement(namespaceURI, elementsLocalNames.peek(), elementsQNames.peek(), attributes);
    }

    void emptyElementEnd() throws SAXException{
        elementEnd(elementsQNames.pop());
    }

    void elementEnd() throws SAXException{
        String startQName = elementsQNames.pop();
        if(!startQName.equals(qname))
            fatalError("expected </"+startQName+">");
        elementEnd(qname);
    }

    private void elementEnd(String qname) throws SAXException{
        String prefix = elementsPrefixes.pop();
        String namespaceURI = namespaces.getNamespaceURI(prefix);
        if(namespaceURI==null)
            fatalError("Unbound prefix: "+prefix);
        handler.endElement(namespaceURI, elementsLocalNames.pop(), qname);

        if(namespaces.map()!=null){
            for(String nsPrefix: namespaces.map().keySet())
                handler.endPrefixMapping(nsPrefix);
        }
        namespaces = namespaces.parent();
        clearQName();
    }

    /*-------------------------------------------------[ PI ]---------------------------------------------------*/

    private String piTarget;
    void piTarget(Chars data){
        piTarget = data.toString();
    }

    void piData(Chars piData) throws SAXException{
        handler.processingInstruction(piTarget, piData.length()>0 ? piData.toString() : "");
    }

    void piData() throws SAXException{
        handler.processingInstruction(piTarget, "");
    }

    /*-------------------------------------------------[ Misc ]---------------------------------------------------*/

    void characters(Chars data) throws SAXException{
        handler.characters(data.array(), data.offset(), data.length());
    }

    void cdata(Chars data) throws SAXException{
        handler.startCDATA();
        handler.characters(data.array(), data.offset(), data.length());
        handler.endCDATA();
    }

    void comment(Chars data) throws SAXException{
        handler.comment(data.array(), data.offset(), data.length());
    }

    @Override
    public void fatalError(String message) throws SAXException{
        fatalError(new SAXParseException(message, this));
    }

    public void fatalError(SAXParseException ex) throws SAXException{
        try{
            handler.fatalError(ex);
            throw ex;
        }finally{
            handler.endDocument();
        }
    }

    @Override
    public void onSuccessful() throws SAXException{
        if(entityScannerCount ==0)
            handler.endDocument();
    }

    /*-------------------------------------------------[ DTD ]---------------------------------------------------*/

    private String dtdRoot;
    void dtdRoot(Chars data){
        dtdRoot = data.toString();
    }

    private String systemID;
    void systemID(Chars data){
        systemID = data.toString();
    }

    private String publicID;
    void publicID(Chars data){
        publicID = data.toString();
    }

    void dtdStart() throws SAXException{
        handler.startDTD(dtdRoot, publicID, systemID);
        publicID = systemID = null;
    }

    private String notationName;
    void notationName(Chars data){
        notationName = data.toString();
    }

    void notationEnd() throws SAXException{
        handler.notationDecl(notationName, publicID, systemID);
    }

    void dtdElement(Chars data){
        System.out.println("dtdElement: "+data);
    }

    void dtdAttributesStart(Chars data){
        System.out.println("dtdAttributesOf: "+data);
    }

    void dtdAttribute(Chars data){
        System.out.println("dtdAttribute: "+data);
    }

    void dtdAttributesEnd(){
        System.out.println("dtdAttributesEnd");
    }

    private String entityName;
    void entityName(Chars data){
        entityName = data.toString();
    }

    private Map<String, char[]> entities = new HashMap<String, char[]>();
    void entityEnd(){
        if(value!=null){
            entities.put(entityName, value.toString().toCharArray());
            value.setLength(0);
        }
    }

    public void dtdEnd() throws SAXException{
        handler.endDTD();
        dtdRoot = null;
    }

    /*-------------------------------------------------[ Test ]---------------------------------------------------*/

    public static void main(String[] args) throws Exception{
        AsyncXMLReader parser = new AsyncXMLReader();

        TransformerHandler handler = TransformerUtil.newTransformerHandler(null, true, -1, null);
        handler.setResult(new StreamResult(System.out));
        SAXUtil.setHandler(parser, handler);

//        String xml = "<root attr1='value1'/>";
//        parser.parse(new InputSource(new StringReader(xml)));

//        String file = "/Users/santhosh/projects/SAXTest/xmlconf/xmltest/valid/sa/114.xml";
        String file = "/Users/santhosh/projects/jlibs/examples/resources/xmlFiles/test.xml";
        parser.parse(new InputSource(file));

//        parser.scanner.write("<root attr1='value1'/>");
//        parser.scanner.close();
    }
}
