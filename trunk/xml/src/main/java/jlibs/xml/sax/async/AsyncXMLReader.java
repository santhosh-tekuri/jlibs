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

import jlibs.core.io.BOM;
import jlibs.core.io.IOUtil;
import jlibs.core.io.UnicodeInputStream;
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
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
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

    private int iProlog = -1;    
    private XMLScanner scanner = new XMLScanner(this, XMLScanner.RULE_DOCUMENT){
        protected void consumed(int ch){
            consumed = true;
            int line = location.getLineNumber();
            boolean addToBuffer = location.consume(ch);
            if(addToBuffer && buffer.isBufferring())
                buffer.append(location.getLineNumber()>line ? '\n' : ch);
        }

        @Override
        public void write(ByteBuffer in, boolean eof) throws IOException{
            if(iProlog==-1){
                if(in.remaining()>=4){
                    byte marker[] = new byte[]{ in.get(0), in.get(1), in.get(2), in.get(3) };
                    BOM bom = BOM.get(marker, true);
                    String encoding;
                    if(bom!=null){
                        in.position(bom.with().length);
                        encoding = bom.encoding();
                    }else{
                        bom = BOM.get(marker, false);
                        encoding = bom!=null ? bom.encoding() : "UTF-8";
                    }
                    decoder = Charset.forName(encoding).newDecoder();
                    if(!encoding.equals("UTF-8")){
                        decoder.onMalformedInput(CodingErrorAction.REPLACE)
                               .onMalformedInput(CodingErrorAction.REPLACE);
                    }
                    iProlog = 0;
                }else{
                    if(eof)
                        super.write(in, true);
                    return;
                }
            }
            while(iProlog<6){
                if(eof){
                    String str = "<?xml ";
                    for(int i=0; i<iProlog; i++)
                        consume(str.charAt(i));
                    super.write(in, true);
                    return;
                }
                CharBuffer charBuffer = CharBuffer.allocate(1);
                CoderResult coderResult = decoder.decode(in, charBuffer, eof);
                if(coderResult.isUnderflow() || coderResult.isOverflow()){
                    char ch = charBuffer.array()[0];
                    if(isPrologStart(ch)){
                        iProlog++;
                        if(iProlog==6){
                            consume('<');
                            consume('?');
                            consume('x');
                            consume('m');
                            consume('l');
                            consume(' ');
                        }
                        if(coderResult.isOverflow())
                            continue;
                        else
                            return;
                    }else{
                        String str = "<?xml ";
                        for(int i=0; i<iProlog; i++)
                            consume(str.charAt(i));
                        consume(ch);
                        iProlog = 7;
                    }
                }else
                    encodingError(coderResult);
            }
            if(iProlog==6 && !eof){
                while(!xdeclEnd){
                    CharBuffer charBuffer = CharBuffer.allocate(1);
                    CoderResult coderResult = decoder.decode(in, charBuffer, eof);
                    if(coderResult.isUnderflow() || coderResult.isOverflow()){
                        char ch = charBuffer.array()[0];
                        consume(ch);
                        if(coderResult.isUnderflow())
                            return;
                    }else
                        encodingError(coderResult);
                }

                String detectedEncoding = decoder.charset().name().toUpperCase(Locale.ENGLISH);
                String declaredEncoding = encoding.toUpperCase(Locale.ENGLISH);
                if(!detectedEncoding.equals(declaredEncoding)){
                    if(detectedEncoding.startsWith("UTF-16") && declaredEncoding.equals("UTF-16"))
                        ; //donothing
                    else if(!detectedEncoding.equals(encoding)){
                        decoder = Charset.forName(encoding).newDecoder();
                        if(!encoding.equals("UTF-8")){
                            decoder.onMalformedInput(CodingErrorAction.REPLACE)
                                   .onMalformedInput(CodingErrorAction.REPLACE);
                        }
                    }
                }
                iProlog = 7;
            }
            super.write(in, eof);
        }

        private boolean isPrologStart(char ch){
            switch(iProlog){
                case 0:
                    return ch=='<';
                case 1:
                    return ch=='?';
                case 2:
                    return ch=='x';
                case 3:
                    return ch=='m';
                case 4:
                    return ch=='l';
                case 5:
                    return ch==0x20 || ch==0x9 || ch==0xa || ch==0xd;
                default:
                    throw new Error("impossible");
            }
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
        scanner.reset();
        documentStart();
        return scanner.writer;
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
        URL url = URLUtil.toURL(systemId);
        if(url.getProtocol().equals("file")){
            scanner.reset();
            documentStart();
            try{
                FileChannel channel = new FileInputStream(new File(url.toURI())).getChannel();
                ByteBuffer buffer = ByteBuffer.allocate(100);
                while(channel.read(buffer)!=-1){
                    buffer.flip();
                    scanner.write(buffer, false);
                    buffer.compact();
                }
                buffer.flip();
                scanner.write(buffer, true);
            }catch(URISyntaxException ex){
                throw new IOException(ex);
            }
        }else
            parse(url.openStream());
    }

    private void parse(InputStream in) throws IOException, SAXException{
        UnicodeInputStream input = new UnicodeInputStream(in);
        String encoding = input.bom!=null ? input.bom.encoding() : IOUtil.UTF_8.name();
        IOUtil.pump(new InputStreamReader(input, encoding), getWriter(), true, true);
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
        iProlog = -1;
        encoding = "UTF-8";
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

        piTarget = null;
        dtdRoot = null;
        systemID = null;
        publicID = null;
        notationName = null;

        entityName = null;
        entities.clear();
        entityStack.clear();

        dtdAttributes.clear();
        dtdElementName = null;
        attributeList = null;
        dtdAttribute = null;

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

    private boolean xdeclEnd;
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
                fatalError("Undefined entityReference: "+entity);

            int rule;
            if(valueStarted){
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

            if(entityStack.contains(entity))
                fatalError("found recursion in entity expansion :"+entity);
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
        else if(type.equals("NMTOKENS")){
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
        }

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
                fatalError("Attribute "+clarkName+" appears more than once in element");
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
        if(entityStack.isEmpty())
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

    private String entityName;
    void entityName(Chars data){
        entityName = data.toString();
    }

    private Map<String, char[]> entities = new HashMap<String, char[]>();
    void entityEnd(){
        if(value!=null){
            // entities may be declared more than once, with the first declaration being the binding one
            if(!entities.containsKey(entityName))
                entities.put(entityName, value.toString().toCharArray());
            value.setLength(0);
        }
    }

    public void dtdEnd() throws SAXException{
        handler.endDTD();
        dtdRoot = null;
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

    /*-------------------------------------------------[ Test ]---------------------------------------------------*/

    public static void main(String[] args) throws Exception{
        AsyncXMLReader parser = new AsyncXMLReader();

        TransformerHandler handler = TransformerUtil.newTransformerHandler(null, true, -1, null);
        handler.setResult(new StreamResult(System.out));
        SAXUtil.setHandler(parser, handler);

//        String xml = "<root attr1='value1'/>";
//        parser.parse(new InputSource(new StringReader(xml)));

//        String file = "/Users/santhosh/projects/SAXTest/xmlconf/xmltest/valid/sa/049.xml"; // with BOM
//        String file = "/Users/santhosh/projects/SAXTest/xmlconf/ibm/valid/P59/ibm59v01.xml";
        String file = "/Users/santhosh/projects/jlibs/examples/resources/xmlFiles/test.xml";
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        try{
            factory.newSAXParser().parse(file, new DefaultHandler(){
                @Override
                public void characters(char[] ch, int start, int length) throws SAXException{
                    super.characters(ch, start, length);    //To change body of overridden methods use File | Settings | File Templates.
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
