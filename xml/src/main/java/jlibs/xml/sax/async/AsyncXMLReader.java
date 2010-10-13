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
import jlibs.xml.sax.AbstractXMLReader;
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.xsl.TransformerUtil;
import org.apache.xerces.util.XMLChar;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.Locator2;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
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
        curQName.reset();
        value.setLength(0);
        valueStarted = false;
        entityValue = false;

        namespaces.reset();
        attributes.reset();
        elements.reset();

        externalDTDPublicID = null;
        externalDTDSystemID = null;
        piTarget = null;
        systemID = null;
        publicID = null;
        notationName = null;

        entityName = null;
        entities.clear();
        entityStack.clear();

        paramEntityName = null;
        paramEntities.clear();
        paramEntityStack.clear();
        peReferenceOutsideMarkup = false;

        dtd.reset();
        dtdElementName = null;
        dtdElement = null;
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

    private QName curQName = new QName();
    void prefix(Chars data){
        curQName.prefix = data.toString();
    }

    void localName(Chars data){
        curQName.localName = data.toString();
    }

    void qname(Chars data){
        curQName.name = data.toString();
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

    void discard(Chars data){}
    
    void rawValue(Chars data){
        if(entityValue)
            value.append(data);
        else{
            char[] chars = data.array();
            int end = data.offset() + data.length();
            for(int i=data.offset(); i<end; i++){
                char ch = chars[i];
                if(ch=='\n' || ch=='\r' || ch=='\t')
                    ch = ' ';
                value.append(ch);
            }
        }
    }

    void hexCode(Chars data) throws SAXException{
        codePoint(Integer.parseInt(data.toString(), 16));
    }

    void asciiCode(Chars data) throws SAXException{
        codePoint(Integer.parseInt(data.toString(), 10));
    }

    private void codePoint(int cp) throws SAXException{
        if(XMLChar.isValid(cp)){
            if(valueStarted)
                value.appendCodePoint(cp);
            else{
                char chars[] = Character.toChars(cp);
                handler.characters(chars, 0, chars.length);
            }
        }else
            fatalError("invalid xml character");
    }

    private ArrayDeque<String> entityStack = new ArrayDeque<String>();
    @SuppressWarnings({"ConstantConditions"})
    void entityReference(Chars data) throws SAXException, IOException{
        if(entityValue){
            value.append('&').append(data).append(';');
            return;
        }
        
        String entity = data.toString();

        char[] entityContent = defaultEntities.get(entity);
        if(entityContent!=null){
            if(valueStarted)
                value.append(entityContent);
            else
                handler.characters(entityContent, 0, entityContent.length);
        }else{
            EntityValue entityValue = entities.get(entity);
            if(entityValue==null)
                fatalError("The entity \""+entity+"\" was referenced, but not declared.");

            if(entityValue.unparsed)
                fatalError("The unparsed entity reference \"&"+entity+";\" is not permitted");
            
            if(standalone==Boolean.TRUE && entityValue.externalDefinition)
                fatalError("The external entity reference \"&"+entity+";\" is not permitted in standalone document");

            if(standalone==Boolean.TRUE && entityValue.externalValue)
                fatalError("The reference to entity \""+entity+"\" declared in an external parsed entity is not permitted in a standalone document");

            checkRecursion(entityStack, entity, "entity");

            int rule;
            if(valueStarted){
                if(entityValue.externalValue)
                    fatalError("The external entity reference \"&"+entityName+";\" is not permitted in an attribute value.");

                entityContent = entityValue.getContent();
                char chars[] = new char[entityContent.length];
                for(int i=chars.length-1; i>=0; i--){
                    char ch = entityContent[i];
                    if(ch=='\n' || ch=='\r' || ch=='\t')
                        ch = ' ';
                    chars[i] = ch;
                }
                entityContent = chars;

                rule = XMLScanner.RULE_INT_VALUE;
            }else{
                entityContent = entityValue.getContent();
                rule = XMLScanner.RULE_ELEM_ENTITY;
            }

            entityStack.push(entity);
            try{
                XMLScanner entityValueScanner = new XMLScanner(this, rule);
                entityValueScanner.writer.write(entityContent);
                entityValueScanner.writer.close();
            }catch(IOException ex){
                throw new RuntimeException(ex);
            }finally{
                entityStack.pop();
            }
        }
    }

    private ArrayDeque<String> paramEntityStack = new ArrayDeque<String>();

    private boolean peReferenceOutsideMarkup = false;
    void peReferenceOutsideMarkup(){
        peReferenceOutsideMarkup = true;
    }

    @SuppressWarnings({"ConstantConditions"})
    void peReference(Chars data) throws Exception{
        String param = data.toString();
        EntityValue entityValue = paramEntities.get(param);

        if(entityValue==null)
            fatalError("The param entity \""+param+"\" was referenced, but not declared.");

        if(standalone==Boolean.TRUE && entityValue.externalValue)
            fatalError("The reference to param entity \""+param+"\" declared in an external parsed entity is not permitted in a standalone document");

        checkRecursion(paramEntityStack, param, "parameter entity");

        if(valueStarted){
            if(curScanner==xmlScanner && curScanner.peStack.size()==0)
                fatalError("The parameter entity reference \"%"+data+";\" cannot occur within markup in the internal subset of the DTD.");

            value.append(entityValue.getContent());
        }else{
            if(peReferenceOutsideMarkup){
                peReferenceOutsideMarkup = false;
                paramEntityStack.push(param);
                try{
                    entityValue.parse(entityValue.externalValue ? XMLScanner.RULE_EXT_SUBSET : XMLScanner.RULE_EXT_SUBSET_DECL);
                }catch(IOException ex){
                    throw new RuntimeException(ex);
                }finally{
                    paramEntityStack.pop();
                }
            }else{
                if(curScanner==xmlScanner && curScanner.peStack.size()==0)
                    fatalError("The parameter entity reference \"%"+data+";\" cannot occur within markup in the internal subset of the DTD.");
                curScanner.peStack.push(new XMLEntityScanner.CharReader(entityValue.getContent()));
            }
        }
    }

    void valueEnd(){
        valueStarted = false;
        entityValue = false;
    }

    /*-------------------------------------------------[ Start Element ]---------------------------------------------------*/

    private final Namespaces namespaces = new Namespaces(handler);
    private final DTD dtd = new DTD(namespaces);
    private final Attributes attributes = new Attributes(namespaces, dtd);
    private final Elements elements = new Elements(handler, namespaces, attributes);

    void attributesStart(){
        elements.push1(curQName);
    }

    void attributeEnd() throws SAXException{
        String error = attributes.addAttribute(elements.currentElementName(), curQName, value);
        if(error!=null)
            fatalError(error);
        curQName.reset();
    }

    void attributesEnd() throws SAXException{
        String error = elements.push2();
        if(error!=null)
            fatalError(error);
    }

    void emptyElementEnd() throws SAXException{
        elements.pop();
    }

    void elementEnd() throws SAXException{
        String error = elements.pop(curQName.name);
        if(error!=null)
            fatalError(error);
        curQName.reset();
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

    private boolean isWhitespace(Chars data){
        char chars[] = data.array();
        int end = data.offset()+data.length();
        for(int i=data.offset(); i<end; i++){
            if(!XMLChar.isSpace(chars[i]))
                return false;
        }
        return true;
    }
    
    void characters(Chars data) throws SAXException{
        if(dtd.nonMixedElements.contains(elements.currentElementName()) && isWhitespace(data))
            handler.ignorableWhitespace(data.array(), data.offset(), data.length());
        else
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

    void dtdRoot(Chars data){
        dtd.root = data.toString();
    }

    private String systemID;
    void systemID(Chars data){
        systemID = data.toString();
    }

    private String publicID;
    void publicID(Chars data){
        publicID = AttributeType.toPublicID(data.toString());
    }

    void dtdStart() throws SAXException{
        handler.startDTD(dtd.root, publicID, systemID);
        externalDTDPublicID = publicID;
        externalDTDSystemID = systemID;
        publicID = systemID = null;
    }

    private String notationName;
    void notationName(Chars data){
        notationName = data.toString();
    }

    void notationEnd() throws SAXException, IOException{
        systemID = curScanner.resolve(systemID);
        handler.notationDecl(notationName, publicID, systemID);
        notationName = null;
        publicID = this.systemID = null;
    }

    private String dtdElement;
    void dtdElement(Chars data){
        dtdElement = data.toString();
    }

    void notMixed(){
        dtd.nonMixedElements.add(dtdElement);
    }

    public void dtdEnd() throws SAXException, IOException{
        if(externalDTDPublicID!=null || externalDTDSystemID!=null){
            externalDTDSystemID = curScanner.resolve(externalDTDSystemID);
            resolveExternalDTD();
        }
        handler.endDTD();
    }

    /*-------------------------------------------------[ Entity Definition ]---------------------------------------------------*/

    private String entityName;
    void entityName(Chars data){
        entityName = data.toString();
    }

    private boolean unparsedEntity;
    void notationReference(Chars data) throws IOException, SAXException{
        handler.unparsedEntityDecl(entityName, publicID, curScanner.resolve(systemID) , data.toString());
        unparsedEntity = true;
    }

    private Map<String, EntityValue> entities = new HashMap<String, EntityValue>();
    void entityEnd() throws SAXException, IOException{
        // entities may be declared more than once, with the first declaration being the binding one        
        if(!entities.containsKey(entityName))
            entities.put(entityName, new EntityValue());
        unparsedEntity = false;
        value.setLength(0);
        publicID = systemID = null;
    }

    private char[] externalEntityValue;
    void externalEntityValue(Chars data){
        externalEntityValue = Arrays.copyOfRange(data.array(), data.offset(), data.offset()+data.length());
    }

    class EntityValue{
        String entityName;
        char[] content;
        boolean externalDefinition;
        boolean unparsed;
        boolean externalValue;
        InputSource inputSource;

        public EntityValue() throws IOException, SAXException{
            entityName = AsyncXMLReader.this.entityName;
            externalDefinition = curScanner!=xmlScanner;
            unparsed = unparsedEntity;

            if(systemID==null && publicID==null)
                content = value.toString().toCharArray();
            else{
                externalValue = true;
                inputSource = curScanner.resolve(publicID, systemID);
            }
        }

        public char[] getContent() throws IOException, SAXException{
            if(content==null){
                InputSource is = handler.resolveEntity(inputSource.getPublicId(), inputSource.getSystemId());
                if(is==null)
                    is = inputSource;
                inputSource = null;

                XMLEntityScanner scanner = new XMLEntityScanner(AsyncXMLReader.this, XMLScanner.RULE_EXTERNAL_ENTITY_VALUE);
                scanner.parent = curScanner;
                curScanner = scanner;
                scanner.parse(is);
                curScanner = curScanner.parent;
                content = externalEntityValue;
            }
            return content;
        }

        public void parse(int rule) throws IOException, SAXException{
            if(content!=null){
                XMLScanner scanner = new XMLScanner(AsyncXMLReader.this, rule);
                scanner.writer.write(content);
                scanner.writer.close();
            }else{
                InputSource is = handler.resolveEntity(inputSource.getPublicId(), inputSource.getSystemId());
                XMLEntityScanner scanner = new XMLEntityScanner(AsyncXMLReader.this, rule);
                scanner.parent = curScanner;
                curScanner = scanner;
                scanner.parse(is==null ? inputSource : is);
                curScanner = curScanner.parent;
            }
        }
    }

    public void checkRecursion(Deque<String> stack, String current, String type) throws SAXException{
        if(stack.contains(current)){
            StringBuilder message = new StringBuilder("Recursive ").append(type).append(" reference ");
            message.append('"').append(current).append('"').append(". (Reference path: ");
            boolean first = true;
            Iterator<String> iter = stack.descendingIterator();
            while(iter.hasNext()){
                if(first)
                    first = false;
                else
                    message.append(" -> ");
                message.append(iter.next());
            }
            message.append(" -> ").append(current);
            message.append(')');
            fatalError(message.toString());
        }
    }

    /*-------------------------------------------------[ Param Entity Definition ]---------------------------------------------------*/

    private String paramEntityName;
    void paramEntityName(Chars data){
        paramEntityName = data.toString();
    }

    private Map<String, EntityValue> paramEntities = new HashMap<String, EntityValue>();
    void paramEntityEnd() throws SAXException, IOException{
        if(!paramEntities.containsKey(paramEntityName))
            paramEntities.put(paramEntityName, new EntityValue());
        unparsedEntity = false;
        value.setLength(0);
        publicID = systemID = null;
    }

    /*-------------------------------------------------[ DTD Attributes ]---------------------------------------------------*/

    private String dtdElementName;
    private Map<String, DTDAttribute> attributeList;
    private DTDAttribute dtdAttribute;

    void dtdAttributesStart(Chars data){
        dtdElementName = data.toString();
        attributeList = dtd.attributes.get(dtdElementName);
        if(attributeList==null)
            dtd.attributes.put(dtdElementName, attributeList=new HashMap<String, DTDAttribute>());
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
            dtdAttribute.value = dtdAttribute.type.normalize(value.toString());
            dtdAttribute.fire(handler);
        }
        value.setLength(0);
    }

    void attributeRequired() throws SAXException{
        if(dtdAttribute!=null){
            dtdAttribute.valueType = AttributeValueType.REQUIRED;
            dtdAttribute.fire(handler);
        }
    }

    void attributeImplied() throws SAXException{
        if(dtdAttribute!=null){
            dtdAttribute.valueType = AttributeValueType.IMPLIED;
            dtdAttribute.fire(handler);
        }
    }

    void attributeFixedValue() throws SAXException{
        if(dtdAttribute!=null){
            dtdAttribute.valueType = AttributeValueType.FIXED;
            dtdAttribute.value = dtdAttribute.type.normalize(value.toString());
            dtdAttribute.fire(handler);
        }
        value.setLength(0);
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

    /*-------------------------------------------------[ Test ]---------------------------------------------------*/

    public static void main(String[] args) throws Exception{
        AsyncXMLReader parser = new AsyncXMLReader();

        TransformerHandler handler = TransformerUtil.newTransformerHandler(null, true, -1, null);
        handler.setResult(new StreamResult(System.out));
        SAXUtil.setHandler(parser, handler);

//        String xml = "<root attr1='value1'/>";
//        parser.parse(new InputSource(new StringReader(xml)));

//        String file = "/Users/santhosh/projects/SAXTest/xmlconf/xmltest/valid/sa/049.xml"; // with BOM
         String file = "/Users/santhosh/projects/SAXTest/xmlconf/xmltest/valid/sa/097.xml";
//        String file = "/Users/santhosh/projects/jlibs/examples/resources/xmlFiles/test.xml";
//        String file = "test.xml";
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
            System.err.println("==================================================================================================================================");
        }

        IOUtil.pump(new InputStreamReader(new FileInputStream(file), "UTF-8"), new StringWriter(), true, true);
//        IOUtil.pump(new UTF8Reader(new FileInputStream(file)), new StringWriter(), true, true);

        parser.parse(new InputSource(file));
//        parser.scanner.write("<root attr1='value1'/>");        
//        parser.scanner.close();
    }
}
