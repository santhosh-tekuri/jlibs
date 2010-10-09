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
import org.apache.xerces.impl.XMLEntityManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.Locator2;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
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

    private XMLEntityScanner xmlScanner = new XMLEntityScanner(this, XMLScanner.RULE_DOCUMENT);
    private XMLEntityScanner curScanner = xmlScanner;

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException{
        try{
            super.setFeature(name, value);
        } catch(SAXNotRecognizedException e){
            // ignore
        }
    }

    private void reset() throws SAXException{
        curScanner = xmlScanner;
        xmlScanner.reset();
    }

    public Writer getWriter() throws SAXException{
        reset();
        return xmlScanner.writer;
    }

    @Override
    public void parse(InputSource input) throws IOException, SAXException{
        reset();
        xmlScanner.parse(input);
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException{
        reset();
        xmlScanner.parse(URLUtil.toURL(systemId));
    }

    /*-------------------------------------------------[ Locator ]---------------------------------------------------*/

    @Override
    public String getPublicId(){
        return null;
    }

    @Override
    public String getSystemId(){
        return curScanner.sourceURL.toString();
    }

    @Override
    public int getLineNumber(){
        return curScanner.location.getLineNumber();
    }

    @Override
    public int getColumnNumber(){
        return curScanner.location.getColumnNumber();
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

    void documentStart() throws SAXException{
        encoding = "UTF-8";
        standalone = null;
        xdeclEnd = false;
        clearQName();
        value.setLength(0);
        valueStarted = false;
        entityValue = false;

        namespaces = new NamespaceMap();
        attributes.clear();

        elementsPrefixes.clear();
        elementsLocalNames.clear();
        elementsQNames.clear();

        externalDTDPublicID = null;
        externalDTDSystemID = null;
        piTarget = null;
        dtdRoot = null;
        systemID = null;
        publicID = null;
        notationName = null;

        entityName = null;
        entities.clear();
        externalEntities.clear();
        entityStack.clear();

        paramEntityName = null;
        paramEntities.clear();
        externalParamEntities.clear();
        entitiesWithExternalEntityValue.clear();
        unparsedEntities.clear();

        dtdAttributes.clear();
        dtdElementName = null;
        attributeList = null;
        dtdAttribute = null;
        open = 0;

        handler.setDocumentLocator(this);
        handler.startDocument();
    }

    /*-------------------------------------------------[ XML Decleration ]---------------------------------------------------*/

    void version(Chars data) throws SAXException{
        if(!"1.0".contentEquals(data))
            fatalError("Unsupported XML Version: "+data);
    }

    String encoding;
    void encoding(Chars data) throws SAXException{
        encoding = data.toString();
    }

    private Boolean standalone;
    void standalone(Chars data){
        standalone = "yes".contentEquals(data);
    }

    boolean xdeclEnd;
    void xdeclEnd(){
        xdeclEnd = true;
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
    private boolean valueStarted = true;
    private boolean entityValue = false;

    void valueStart(){
        value.setLength(0);
        valueStarted = true;
        entityValue = false;
    }

    void entityValue(){
        entityValue = true;
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

    private ArrayDeque<String> entityStack = new ArrayDeque<String>();
    @SuppressWarnings({"ConstantConditions"})
    void entityReference(Chars data) throws SAXException{
        if(entityValue){
            value.append('&').append(data).append(';');
            return;
        }
        
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
                fatalError("The entity \""+entity+"\" was referenced, but not declared.");

            if(unparsedEntities.contains(entity))
                fatalError("The unparsed entity reference \"&"+entity+";\" is not permitted");
            
            if(standalone==Boolean.TRUE && externalEntities.contains(entity))
                fatalError("The external entity reference \"&"+entity+";\" is not permitted in standalone document");

            int rule;
            if(valueStarted){
                if(entitiesWithExternalEntityValue.contains(entity))
                    fatalError("The external entity reference \"&"+entityName+";\" is not permitted in an attribute value.");

                char chars[] = new char[entityValue.length];
                for(int i=chars.length-1; i>=0; i--){
                    char ch = entityValue[i];
                    if(ch=='\n' || ch=='\r' || ch=='\t')
                        ch = ' ';
                    chars[i] = ch;
                }
                entityValue = chars;

                rule = XMLScanner.RULE_VALUE_ENTITY;
            }else
                 rule = XMLScanner.RULE_ELEM_ENTITY;

            if(entityStack.contains(entity)){
                StringBuilder message = new StringBuilder("Recursive entity reference ");
                message.append('"').append(entity).append('"').append(". (Reference path: ");
                boolean first = true;
                Iterator<String> iter = entityStack.descendingIterator();
                while(iter.hasNext()){
                    if(first)
                        first = false;
                    else
                        message.append(" -> ");
                    message.append(iter.next());
                }
                message.append(" -> ").append(entity);
                message.append(')');
                fatalError(message.toString());
            }
            entityStack.push(entity);
            try{
                XMLScanner entityValueScanner = new XMLScanner(this, rule);
                entityValueScanner.writer.write(entityValue);
                entityValueScanner.writer.close();
            }catch(IOException ex){
                throw new RuntimeException(ex);
            }finally{
                entityStack.pop();
            }
        }
    }

    private ArrayDeque<String> paramEntityStack = new ArrayDeque<String>();
    void peReference(Chars data) throws Exception{
        if(valueStarted && curScanner==xmlScanner)
            fatalError("The parameter entity reference \"%"+data+";\" cannot occur within markup in the internal subset of the DTD.");
        else{
            String param = data.toString();
            char[] paramValue = paramEntities.get(param);
            if(paramValue==null)
                fatalError("The param entity \""+param+"\" was referenced, but not declared.");
            paramEntityStack.push(param);
            try{
                XMLScanner paramValueScanner = new XMLScanner(this, XMLScanner.RULE_INT_SUBSET);
                paramValueScanner.writer.write(paramValue);
                paramValueScanner.writer.close();
            }catch(IOException ex){
                throw new RuntimeException(ex);
            }finally{
                paramEntityStack.pop();
            }
        }
    }

    void valueEnd(){
        valueStarted = false;
        entityValue = false;
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
        String type = "CDATA";
        Map<String, DTDAttribute> attrList = dtdAttributes.get(elementsQNames.peek());
        if(attrList!=null){
            DTDAttribute dtdAttr = attrList.get(qname);
            if(dtdAttr!=null){
                if(dtdAttr.type==AttributeType.ENUMERATION)
                    type = "NMTOKEN";
                else
                    type = dtdAttr.type.name();
            }
        }

        String value = this.value.toString();
        if(type.equals("NMTOKEN"))
            value = value.trim();
        else if(type.equals("NMTOKENS"))
            value = toNMTOKENS(value);

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
                    if(value.length()==0)
                        fatalError("No Prefix Undeclaring: "+localName);
                    namespaces.put(localName, value);
                    handler.startPrefixMapping(localName, value);
                }
            }
        }else{
            attributes.addAttribute(prefix, localName, qname, type, value);
        }

        clearQName();
    }

    private String toNMTOKENS(String value){
        char[] buffer = value.toCharArray();
        int write = 0;
        int lastWrite = 0;
        boolean wroteOne = false;

        int read = 0;
        while(read<buffer.length && buffer[read]==' '){
            read++;
        }

        int len = buffer.length;
        while(len<read && buffer[len-1]==' ')
            len--;

        while(read<len){
            if (buffer[read]==' '){
                if (wroteOne)
                    buffer[write++] = ' ';

                do{
                    read++;
                }while(read<len && buffer[read]==' ');
            }else{
                buffer[write++] = buffer[read++];
                wroteOne = true;
                lastWrite = write;
            }
        }

        value = new String(buffer, 0, lastWrite);
        return value;
    }

    private Set<String> attributeNames = new HashSet<String>();
    void attributesEnd() throws SAXException{
        attributeNames.clear();
        int attrCount = attributes.getLength();
        for(int i=0; i<attrCount; i++){
            String prefix = attributes.getURI(i);
            String uri = "";
            if(prefix.length()>0){
                uri = namespaces.getNamespaceURI(prefix);
                if(uri==null)
                    fatalError("Unbound prefix: "+prefix);
                attributes.setURI(i, uri);
            }

            String clarkName = ClarkName.valueOf(uri, attributes.getLocalName(i));
            if(!attributeNames.add(clarkName))
                fatalError("Attribute \""+clarkName+"\" was already specified for element \""+elementsQNames.peek()+"\"");
        }

        String elemQName = elementsQNames.peek();
        Map<String, DTDAttribute> attList = dtdAttributes.get(elemQName);
        if(attList!=null){
            for(DTDAttribute dtdAttr: attList.values()){
                if(dtdAttr.valueType==AttributeValueType.DEFAULT || dtdAttr.valueType==AttributeValueType.FIXED){
                    int index = attributes.getIndex(dtdAttr.name);
                    if(index==-1){
                        if(!dtdAttr.name.equals("xmlns") && !dtdAttr.name.startsWith("xmlns:"))
                            attributes.addAttribute("", dtdAttr.name, dtdAttr.name, dtdAttr.type.name(), dtdAttr.value);
                    }
                }
            }
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
        if(entityStack.isEmpty() && paramEntityStack.isEmpty() && curScanner==xmlScanner)
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
        publicID = toNMTOKENS(data.toString());
    }

    void dtdStart() throws SAXException{
        handler.startDTD(dtdRoot, publicID, systemID);
        externalDTDPublicID = publicID;
        externalDTDSystemID = systemID;
        publicID = systemID = null;
    }

    private String notationName;
    void notationName(Chars data){
        notationName = data.toString();
    }

    void notationEnd() throws SAXException, IOException{
        String systemID = this.systemID;
        if(systemID!=null && curScanner.sourceURL!=null)
            systemID = XMLEntityManager.expandSystemId(systemID, curScanner.sourceURL.toString(), false);
        handler.notationDecl(notationName, publicID, systemID);
        notationName = null;
        publicID = this.systemID = null;
    }

    void dtdElement(Chars data){
        System.out.println("dtdElement: "+data);
    }

    public void dtdEnd() throws SAXException, IOException{
        if(externalDTDPublicID!=null || externalDTDSystemID!=null){
            try{
                if(externalDTDSystemID!=null)
                    externalDTDSystemID = curScanner.resolve(externalDTDSystemID).toURI().toString();
            }catch(URISyntaxException ex){
                throw new SAXException(ex);
            }
            resolveExternalDTD();
        }
        handler.endDTD();
        dtdRoot = null;
    }

    /*-------------------------------------------------[ Entity Definition ]---------------------------------------------------*/

    private String entityName;
    void entityName(Chars data){
        entityName = data.toString();
    }

    private Set<String> unparsedEntities = new HashSet<String>();
    void notationReference(Chars data) throws IOException, SAXException{
        String systemID = this.systemID;
        if(systemID!=null && curScanner.sourceURL!=null)
            systemID = XMLEntityManager.expandSystemId(systemID, curScanner.sourceURL.toString(), false);
        handler.unparsedEntityDecl(entityName, publicID, systemID, data.toString());
        unparsedEntities.add(entityName);
    }

    private Map<String, char[]> entities = new HashMap<String, char[]>();
    private Set<String> externalEntities = new HashSet<String>();
    private Set<String> entitiesWithExternalEntityValue = new HashSet<String>();
    void entityEnd() throws SAXException{
        if(curScanner!=xmlScanner)
            externalEntities.add(entityName);

        if(systemID==null && publicID==null){
            // entities may be declared more than once, with the first declaration being the binding one
            if(!entities.containsKey(entityName))
                entities.put(entityName, value.toString().toCharArray());
            value.setLength(0);
        }else{
            if(standalone==Boolean.TRUE)
                fatalError("The reference to entity \""+entityName+"\" declared in an external parsed entity is not permitted in a standalone document");
            if(!entities.containsKey(entityName))
                entities.put(entityName, "external-entity".toCharArray()); //todo
            entitiesWithExternalEntityValue.add(entityName);
            publicID = systemID = null; 
        }
    }

    /*-------------------------------------------------[ Param Entity Definition ]---------------------------------------------------*/

    private String paramEntityName;
    void paramEntityName(Chars data){
        paramEntityName = data.toString();
    }

    private Map<String, char[]> paramEntities = new HashMap<String,char[]>();
    private Set<String> externalParamEntities = new HashSet<String>();
    void paramEntityEnd(){
        if(systemID==null && publicID==null){
            // entities may be declared more than once, with the first declaration being the binding one
            if(!paramEntities.containsKey(paramEntityName))
                paramEntities.put(paramEntityName, value.toString().toCharArray());
            value.setLength(0);
            if(curScanner!=xmlScanner)
                externalEntities.add(paramEntityName);
        }else{
            if(!paramEntities.containsKey(paramEntityName))
                paramEntities.put(paramEntityName, "external-param-entity".toCharArray()); //todo
            externalParamEntities.add(paramEntityName);
        }
    }

    /*-------------------------------------------------[ DTD Attributes ]---------------------------------------------------*/

    private Map<String, Map<String, DTDAttribute>> dtdAttributes = new HashMap<String, Map<String, DTDAttribute>>();
    private String dtdElementName;
    private Map<String, DTDAttribute> attributeList;
    private DTDAttribute dtdAttribute;

    void dtdAttributesStart(Chars data){
        dtdElementName = data.toString();
        attributeList = dtdAttributes.get(dtdElementName);
        if(attributeList==null)
            dtdAttributes.put(dtdElementName, attributeList=new HashMap<String, DTDAttribute>());
    }

    void dtdAttribute(Chars data){
        String attributeName = data.toString();
        if(attributeList.get(attributeName)==null){
            dtdAttribute = new DTDAttribute();
            dtdAttribute.name = attributeName;
            attributeList.put(attributeName, dtdAttribute);
        }else
            dtdAttribute = null;
    }

    void cdataAttribute(){
        if(dtdAttribute!=null)
            dtdAttribute.type = AttributeType.CDATA;
    }

    void idAttribute(){
        if(dtdAttribute!=null)
            dtdAttribute.type = AttributeType.ID;
    }

    void idRefAttribute(){
        if(dtdAttribute!=null)
            dtdAttribute.type = AttributeType.IDREF;
    }

    void idRefsAttribute(){
        if(dtdAttribute!=null)
            dtdAttribute.type = AttributeType.IDREFS;
    }

    void nmtokenAttribute(){
        if(dtdAttribute!=null)
            dtdAttribute.type = AttributeType.NMTOKEN;
    }

    void nmtokensAttribute(){
        if(dtdAttribute!=null)
            dtdAttribute.type = AttributeType.NMTOKENS;
    }

    void entityAttribute(){
        if(dtdAttribute!=null)
            dtdAttribute.type = AttributeType.ENTITY;
    }

    void entitiesAttribute(){
        if(dtdAttribute!=null)
            dtdAttribute.type = AttributeType.ENTITIES;
    }

    void enumerationAttribute(){
        if(dtdAttribute!=null){
            dtdAttribute.type = AttributeType.ENUMERATION;
            dtdAttribute.validValues = new ArrayList<String>();
        }
    }

    void notationAttribute(){
        if(dtdAttribute!=null){
            dtdAttribute.type = AttributeType.NOTATION;
            dtdAttribute.validValues = new ArrayList<String>();
        }
    }

    void attributeEnumValue(Chars data){
        if(dtdAttribute!=null)
            dtdAttribute.validValues.add(data.toString());
    }

    void attributeNotationValue(Chars data){
        if(dtdAttribute!=null)
            dtdAttribute.validValues.add(data.toString());
    }

    void attributeDefaultValue() throws SAXException{
        if(dtdAttribute!=null){
            dtdAttribute.valueType = AttributeValueType.DEFAULT;
            dtdAttribute.value = value.toString();
            if(dtdAttribute.type==AttributeType.NMTOKEN)
                dtdAttribute.value = dtdAttribute.value.trim();
            else if(dtdAttribute.type==AttributeType.NMTOKENS)
                dtdAttribute.value = toNMTOKENS(dtdAttribute.value);
            fireDTDAttributeEvent();
        }
        value.setLength(0);
    }

    void attributeRequired() throws SAXException{
        if(dtdAttribute!=null){
            dtdAttribute.valueType = AttributeValueType.REQUIRED;
            fireDTDAttributeEvent();
        }
    }

    void attributeImplied() throws SAXException{
        if(dtdAttribute!=null){
            dtdAttribute.valueType = AttributeValueType.IMPLIED;
            fireDTDAttributeEvent();
        }
    }

    void attributeFixedValue() throws SAXException{
        if(dtdAttribute!=null){
            dtdAttribute.valueType = AttributeValueType.FIXED;
            dtdAttribute.value = value.toString();
            if(dtdAttribute.type==AttributeType.NMTOKEN)
                dtdAttribute.value = dtdAttribute.value.trim();
            else if(dtdAttribute.type==AttributeType.NMTOKENS)
                dtdAttribute.value = toNMTOKENS(dtdAttribute.value);
            fireDTDAttributeEvent();
        }
        value.setLength(0);
    }

    private void fireDTDAttributeEvent() throws SAXException{
        handler.attributeDecl(dtdElementName, dtdAttribute.name, dtdAttribute.type.toString(dtdAttribute.validValues), dtdAttribute.valueType.mode, dtdAttribute.value);
    }

    void dtdAttributesEnd(){
        System.out.println("dtdAttributesEnd");
    }

    /*-------------------------------------------------[ Entity Resolution ]---------------------------------------------------*/

    private String externalDTDPublicID, externalDTDSystemID;    
    private void resolveExternalDTD() throws IOException, SAXException{
        String publicID = externalDTDPublicID;
        String systemID = this.externalDTDSystemID;
        this.externalDTDPublicID = this.externalDTDSystemID = null;
        InputSource is = handler.resolveEntity(publicID, systemID);
        XMLEntityScanner dtdScanner = new XMLEntityScanner(this, XMLScanner.RULE_EXT_SUBSET);
        dtdScanner.parent = curScanner;
        curScanner = dtdScanner;
        encoding = null;
        xdeclEnd = false;
        if(is!=null)
            dtdScanner.parse(is);
        else
            dtdScanner.parse(new URL(systemID));
        curScanner = curScanner.parent;
    }

    private int open = 0;
    void ignoreSect(){
        open++;
    }

    void includeSect(){
        open++;
    }

    void ignoreStart(Chars data){
        open++;
    }

    void ignoreEnd(Chars data) throws SAXException{
        if(open==0)
            fatalError("']]>' is unexpected here");
        open--;
    }

    void includeEnd() throws SAXException{
        if(open==0)
            fatalError("']]>' is unexpected here");
        open--;
    }

    void ignoreSectContents(Chars data){

    }

    void sectEnd() throws SAXException{
        if(open!=0)
            fatalError("']]>' expected "+open+" times");
    }
    
    /*-------------------------------------------------[ Test ]---------------------------------------------------*/

    public static void main(String[] args) throws Exception{
        AsyncXMLReader parser = new AsyncXMLReader();

        TransformerHandler handler = TransformerUtil.newTransformerHandler(null, true, -1, null);
        handler.setResult(new StreamResult(System.out));
        SAXUtil.setHandler(parser, handler);

//        String xml = "<root attr1='value1'/>";
//        parser.parse(new InputSource(new StringReader(xml)));

//        String file = "/Users/santhosh/projects/SAXTest/xmlconf/xmltest/valid/sa/049.xml"; // with BOM
        String file = "/Users/santhosh/projects/SAXTest/xmlconf/ibm/valid/P32/ibm32v02.xml";
//        String file = "/Users/santhosh/projects/jlibs/examples/resources/xmlFiles/test.xml";
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        try{
            factory.newSAXParser().parse(file, new DefaultHandler(){
                @Override
                public void characters(char[] ch, int start, int length) throws SAXException{
                    super.characters(ch, start, length);    //To change body of overridden methods use File | Settings | File Templates.
                }

                @Override
                public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException{
                    super.unparsedEntityDecl(name, publicId, systemId, notationName);    //To change body of overridden methods use File | Settings | File Templates.
                }
            });
        }catch(Exception ex){
            ex.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.out.println("==========================");
        }

        IOUtil.pump(new InputStreamReader(new FileInputStream(file), "UTF-8"), new StringWriter(), true, true);
//        IOUtil.pump(new UTF8Reader(new FileInputStream(file)), new StringWriter(), true, true);


        parser.parse(new InputSource(file));

        
//        parser.scanner.write("<root attr1='value1'/>");        
//        parser.scanner.close();
    }
}
