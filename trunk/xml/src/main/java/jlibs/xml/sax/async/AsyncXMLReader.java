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

import jlibs.nbp.Chars;
import jlibs.nbp.NBChannel;
import jlibs.nbp.NBHandler;
import jlibs.nbp.ReadableCharChannel;
import jlibs.xml.ClarkName;
import org.apache.xerces.util.XMLChar;
import org.xml.sax.*;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.ext.Locator2;
import org.xml.sax.helpers.AttributesImpl;

import java.io.CharArrayReader;
import java.io.IOException;
import java.util.*;

import static javax.xml.XMLConstants.*;
import static jlibs.xml.sax.SAXFeatures.*;
import static jlibs.xml.sax.SAXProperties.*;

/**
 * @author Santhosh Kumar T
 */
@SuppressWarnings({"ThrowableInstanceNeverThrown"})
public final class AsyncXMLReader implements XMLReader, NBHandler<SAXException>, Locator2{
    private static Map<String, char[]> defaultEntities = new HashMap<String, char[]>();
    static{
        defaultEntities.put("amp",  new char[]{ '&' });
        defaultEntities.put("lt",   new char[]{ '<' });
        defaultEntities.put("gt",   new char[]{ '>' });
        defaultEntities.put("apos", new char[]{ '\'' });
        defaultEntities.put("quot", new char[]{ '"' });
    }

    public AsyncXMLReader(){
        xmlScanner.coalesceNewLines = true;
        encoding = null;
        elements[0] = elem;
        namespaces[0] = XMLNS_ATTRIBUTE;
        namespaces[1] = XMLNS_ATTRIBUTE_NS_URI;
        namespaces[2] = XML_NS_PREFIX;
        namespaces[3] = XML_NS_URI;
        elem.defaultNamespace = "";
    }

    private boolean strict = false;

    public boolean isStrict(){
        return strict;
    }

    public void setStrict(boolean strict){
        this.strict = strict;
    }

    private ContentHandler contentHandler;
    @Override
    public void setContentHandler(ContentHandler contentHandler){
        this.contentHandler = contentHandler;
    }

    @Override
    public ContentHandler getContentHandler(){
        return contentHandler;
    }

    private ErrorHandler errorHandler;
    @Override
    public void setErrorHandler(ErrorHandler errorHandler){
        this.errorHandler = errorHandler;
    }

    @Override
    public ErrorHandler getErrorHandler(){
        return errorHandler;
    }

    private EntityResolver entityResolver;
    @Override
    public void setEntityResolver(EntityResolver entityResolver){
        this.entityResolver = entityResolver;
    }

    @Override
    public EntityResolver getEntityResolver(){
        return entityResolver;
    }

    private DTDHandler dtdHandler;
    @Override
    public void setDTDHandler(DTDHandler dtdHandler){
        this.dtdHandler = dtdHandler;
    }

    @Override
    public DTDHandler getDTDHandler(){
        return dtdHandler;
    }

    private LexicalHandler lexicalHandler;
    private DeclHandler declHandler;

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException{
        if(NAMESPACES.equals(name) || EXTERNAL_GENERAL_ENTITIES.equals(name) || EXTERNAL_PARAMETER_ENTITIES.equals(name))
            return true;
        throw new SAXNotSupportedException();
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException{
        if((NAMESPACES.equals(name) || EXTERNAL_GENERAL_ENTITIES.equals(name) || EXTERNAL_PARAMETER_ENTITIES.equals(name)) && value)
            return;
        throw new SAXNotRecognizedException();
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException{
        if(LEXICAL_HANDLER.equals(name) || LEXICAL_HANDLER_ALT.equals(name))
            return lexicalHandler;
        if(DECL_HANDLER.equals(name) || DECL_HANDLER_ALT.equals(name))
            return declHandler;
        throw new SAXNotRecognizedException();
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException{
        if(LEXICAL_HANDLER.equals(name) || LEXICAL_HANDLER_ALT.equals(name)){
            if(value==null || value instanceof LexicalHandler)
                lexicalHandler = (LexicalHandler)value;
            else
                throw new SAXNotSupportedException("value must implement "+LexicalHandler.class);
        }else if(DECL_HANDLER.equals(name) || DECL_HANDLER_ALT.equals(name)){
            if(value==null || value instanceof DeclHandler)
                declHandler = (DeclHandler)value;
            else
                throw new SAXNotSupportedException("value must implement "+DeclHandler.class);
        }else
            throw new SAXNotRecognizedException();
    }

    private XMLScanner xmlScanner = new XMLScanner(this, XMLScanner.RULE_DOCUMENT);
    private XMLScanner declScanner = new XMLScanner(this, XMLScanner.RULE_XDECL);

    private XMLFeeder xmlFeeder, feeder;
    void setFeeder(XMLFeeder feeder) throws IOException{
        if(this.feeder.getParent()==feeder){
            if(this.feeder.postAction!=null){
                try{
                    this.feeder.postAction.run();
                }catch(Exception ex){
                    throw this.feeder.parser.ioError(ex.getMessage());
                }
            }
        }
        this.feeder = feeder;
        elemLock = feeder.elemDepth;
    }
    
    public XMLFeeder createFeeder(InputSource inputSource) throws IOException, SAXException{
        xmlScanner.reset();
        declScanner.reset(XMLScanner.RULE_XDECL);
        if(xmlFeeder==null)
            xmlFeeder = new XMLFeeder(this, xmlScanner, inputSource, declScanner);
        else
            xmlFeeder.init(inputSource, declScanner);
        feeder = xmlFeeder;
        documentStart();
        return feeder;
    }

    @Override
    public void parse(InputSource input) throws IOException, SAXException{
        if(createFeeder(input).feed()!=null)
            throw new IOException("parse(...) shouldn't be used on non-blocking IO");
    }

    @Override
    public void parse(String systemID) throws IOException, SAXException{
        parse(new InputSource(systemID));
    }

    /*-------------------------------------------------[ Locator ]---------------------------------------------------*/

    @Override
    public String getPublicId(){
        return feeder.publicID;
    }

    @Override
    public String getSystemId(){
        return feeder.systemID;
    }

    @Override
    public int getLineNumber(){
        return feeder.parser.getLineNumber();
    }

    @Override
    public int getColumnNumber(){
        return feeder.parser.getColumnNumber();
    }

    public int getCharacterOffset(){
        return feeder.parser.getCharacterOffset();
    }

    @Override
    public String getXMLVersion(){
        return "1.0";
    }

    @Override
    public String getEncoding(){
        ReadableCharChannel channel = feeder.channel();
        if(channel instanceof NBChannel){
            NBChannel nbChannel = (NBChannel)channel;
            if(nbChannel.decoder()!=null)
                return nbChannel.decoder().charset().name();
        }
        return "UTF-8";
    }

    /*-------------------------------------------------[ Document ]---------------------------------------------------*/

    void documentStart() throws SAXException{
        encoding = null;
        standalone = null;
        prefixLength = 0;
        value.setLength(0);
        valueStarted = false;
        entityValue = false;

        elem = elements[0];
        elemLock = elemDepth = 0;
        nsFree = 4;

        systemID = null;
        publicID = null;

        entities.clear();
        entityStack.clear();

        paramEntities.clear();
        paramEntityStack.clear();
        peReferenceOutsideMarkup = false;

        dtd = null;
        _dtd.reset();
        dtdElement = null;
        attributeList = null;
        dtdAttribute = null;

        if(contentHandler!=null){
            contentHandler.setDocumentLocator(this);
            contentHandler.startDocument();
        }
    }

    /*-------------------------------------------------[ XML Decleration ]---------------------------------------------------*/

    void version(Chars data) throws SAXException{
        if(!"1.0".contentEquals(data))
            throw fatalError("Unsupported XML Version: "+data);
    }

    String encoding;
    void encoding(Chars data) throws SAXException{
        encoding = data.toString();
    }

    private Boolean standalone;
    void standalone(Chars data){
        standalone = "yes".contentEquals(data);
    }

    void xdeclEnd(){
        feeder.setDeclaredEncoding(encoding);
        encoding = null;
    }

    /*-------------------------------------------------[ QName ]---------------------------------------------------*/

    private final QNamePool qnamePool = new QNamePool();
    private QName curQName;
    private int prefixLength = 0;
    
    void prefix(Chars data){
        prefixLength = data.length();
    }

    void qname(Chars data) throws SAXException{
        curQName = qnamePool.add(prefixLength, data.array(), data.offset(), data.length());
        prefixLength = 0;
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
        int offset = data.offset();
        int length = data.length();
        if(!entityValue){
            for(int i=offset; i<length; i++){
                char ch = chars[i];
                if(ch=='\n' || ch=='\r' || ch=='\t')
                    chars[i] = ' ';
            }
        }
        value.append(chars, offset, length);
    }

    private int radix;
    void hexCode() throws SAXException{
        radix = 16;
    }

    void asciiCode() throws SAXException{
        radix = 10;
    }

    void charReference(Chars data) throws SAXException{
        int cp = Integer.parseInt(data.toString(), radix);
        if(XMLChar.isValid(cp)){
            if(valueStarted)
                value.appendCodePoint(cp);
            else if(contentHandler!=null){
                char chars[] = Character.toChars(cp);
                contentHandler.characters(chars, 0, chars.length);
            }
        }else
            throw fatalError("invalid xml character");
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
            else if(contentHandler!=null)
                contentHandler.characters(entityContent, 0, entityContent.length);
        }else{
            EntityValue entityValue = entities.get(entity);
            if(entityValue==null)
                throw fatalError("The entity \""+entity+"\" was referenced, but not declared.");

            if(entityValue.unparsed)
                throw fatalError("The unparsed entity reference \"&"+entity+";\" is not permitted");
            
            if(standalone==Boolean.TRUE && entityValue.externalDefinition)
                throw fatalError("The external entity reference \"&"+entity+";\" is not permitted in standalone document");

            if(standalone==Boolean.TRUE && entityValue.externalValue)
                throw fatalError("The reference to entity \""+entity+"\" declared in an external parsed entity is not permitted in a standalone document");

            checkRecursion(entityStack, entity, "entity");

            int rule;
            if(valueStarted){
                if(entityValue.externalValue)
                    throw fatalError("The external entity reference \"&"+entity+";\" is not permitted in an attribute value.");
                rule = XMLScanner.RULE_INT_VALUE;
            }else{
                rule = XMLScanner.RULE_ELEM_CONTENT;
            }

            entityStack.push(entity);
            try{
                XMLFeeder childFeeder = entityValue.parse(rule);
                childFeeder.elemDepth = elemDepth;
                childFeeder.postAction = new Runnable(){
                    @Override
                    public void run(){
                        entityStack.pop();
                        if(elemDepth>AsyncXMLReader.this.feeder.elemDepth)
                            throw new RuntimeException("expected </"+elem.qname.name+">");
                    }
                };
            }catch(IOException ex){
                throw new RuntimeException(ex);
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
        final EntityValue entityValue = paramEntities.get(param);

        if(entityValue==null){
            if(strict)
                throw fatalError("The param entity \""+param+"\" was referenced, but not declared.");
            else
                return;
        }

        if(standalone==Boolean.TRUE && entityValue.externalValue)
            throw fatalError("The reference to param entity \""+param+"\" declared in an external parsed entity is not permitted in a standalone document");

        checkRecursion(paramEntityStack, param, "parameter entity");

        if(valueStarted){
            if(feeder.parser==xmlScanner && feeder.getParent()==null)
                throw fatalError("The parameter entity reference \"%"+data+";\" cannot occur within markup in the internal subset of the DTD.");

            if(entityValue.content!=null)
                value.append(entityValue.content);
            else{
                entityValue.parse(XMLScanner.RULE_EXTERNAL_ENTITY_VALUE).postAction =  new Runnable(){
                    @Override
                    public void run(){
                        value.append(externalEntityValue);
                    }
                };
            }
        }else{
            if(peReferenceOutsideMarkup){
                peReferenceOutsideMarkup = false;
                paramEntityStack.push(param);
                try{
                    entityValue.parse(XMLScanner.RULE_EXT_SUBSET_DECL).postAction =  new Runnable(){
                        @Override
                        public void run(){
                            paramEntityStack.pop();
                        }
                    };
                }catch(IOException ex){
                    throw new RuntimeException(ex);
                }
            }else{
                if(feeder.parser==xmlScanner && feeder.getParent()==null)
                    throw fatalError("The parameter entity reference \"%"+data+";\" cannot occur within markup in the internal subset of the DTD.");
                feeder.setChild(new XMLFeeder(this, feeder.parser, entityValue.inputSource(true), entityValue.prologParser()));
            }
        }
    }

    void valueEnd(){
        valueStarted = false;
        entityValue = false;
    }

    /*-------------------------------------------------[ Start Element ]---------------------------------------------------*/

    private String namespaces[] = new String[20];
    private int nsFree;

    public String getNamespaceURI(String prefix){
        for(int i=nsFree-2; i>=0; i-=2){
            if(namespaces[i].equals(prefix))
                return namespaces[i+1];
        }
        return null;
    }
    
    private final DTD _dtd = new DTD(this);
    private DTD dtd ;
    private final AttributesImpl attrs = new AttributesImpl();
    private Element elem = new Element();
    private Element elements[] = new Element[10];
    private int elemDepth = 0;
    private int elemLock = 0;
    private boolean resolveAttributePrefixes;

    void attributesStart() throws SAXException{
        if(curQName.prefix.equals("xmlns"))
            throw fatalError("Element \""+curQName.name+"\" cannot have \"xmlns\" as its prefix");

        if(attrs.getLength()>0)
            attrs.clear();
        resolveAttributePrefixes = false;
        if(elemDepth==elements.length-1)
            elements = Arrays.copyOf(elements, elemDepth<<1);

        String defaultNamespace = elem.defaultNamespace;
        elem = elements[++elemDepth];
        if(elem==null)
            elements[elemDepth] = elem = new Element();
        elem.init(curQName, nsFree, defaultNamespace);
    }

    void attributeEnd() throws SAXException{
        String attrName = curQName.name;
        String type, attrValue;
        if(dtd==null){
            type = "CDATA";
            attrValue = value.toString();
        }else{
            AttributeType attrType = dtd.attributeType(elem.qname.name, attrName);
            type = attrType.name();
            attrValue = attrType.normalize(value.toString());
        }

        String attrLocalName = curQName.localName;
        if(attrName.startsWith("xmlns", 0)){
            String nsPrefix = null;
            if(attrName.length()==5){
                nsPrefix = "";
                elem.defaultNamespace = attrValue;
            }else if(attrName.charAt(5)==':'){
                if(attrLocalName.equals(XML_NS_PREFIX)){
                    if(!attrValue.equals(XML_NS_URI))
                        throw fatalError("prefix "+ XML_NS_PREFIX+" must refer to "+ XML_NS_URI);
                    return;
                }else if(attrLocalName.equals(XMLNS_ATTRIBUTE))
                    throw fatalError("prefix "+ XMLNS_ATTRIBUTE+" must not be declared");
                else if(attrValue.length()==0)
                    throw fatalError("No Prefix Undeclaring: "+attrLocalName);
                else
                    nsPrefix = attrLocalName;
            }
            if(nsPrefix!=null){
                if(attrValue.equals(XML_NS_URI))
                    throw fatalError(XML_NS_URI+" must be bound to "+ XML_NS_PREFIX);
                else if(attrValue.equals(XMLNS_ATTRIBUTE_NS_URI))
                    throw fatalError(XMLNS_ATTRIBUTE_NS_URI+" must be bound to "+ XMLNS_ATTRIBUTE);

                if(nsFree+2>namespaces.length)
                    namespaces = Arrays.copyOf(namespaces, nsFree<<1);
                namespaces[nsFree] = nsPrefix;
                namespaces[nsFree+1] = attrValue;
                nsFree += 2;
                if(contentHandler!=null)
                    contentHandler.startPrefixMapping(nsPrefix, attrValue);
                return;
            }            
        }

        String prefix = curQName.prefix;
        if(prefix.length()>0)
            resolveAttributePrefixes = true;
        attrs.addAttribute(prefix, attrLocalName, attrName, type, attrValue);
    }

    void attributesEnd() throws SAXException{
        int attrCount = attrs.getLength();
        if(resolveAttributePrefixes){
            for(int i=0; i<attrCount; i++){
                String prefix = attrs.getURI(i);
                if(prefix.length()>0){
                    String uri = getNamespaceURI(prefix);
                    if(uri==null)
                        throw fatalError("Unbound prefix: "+prefix);
                    attrs.setURI(i, uri);
                }
            }
        }
        if(attrCount>1){
            for(int i=1; i<attrCount; i++){
                if(attrs.getIndex(attrs.getURI(i), attrs.getLocalName(i))<i)
                    throw fatalError("Attribute \""+ ClarkName.valueOf(attrs.getURI(i), attrs.getLocalName(i))+"\" was already specified for element \""+elem.qname.name+"\"");
            }
        }

        QName elemQName = elem.qname;
        if(dtd!=null)
            dtd.addMissingAttributes(elemQName.name, attrs);

        String uri;
        if(elemQName.prefix.length()==0)
            uri = elem.defaultNamespace;
        else{
            uri = getNamespaceURI(elemQName.prefix);
            if(uri==null)
                throw fatalError("Unbound prefix: "+elemQName.prefix);
        }
        elem.uri = uri;
        if(contentHandler!=null)
            contentHandler.startElement(uri, elemQName.localName, elemQName.name, attrs);
    }

    void endingElem(){
        feeder.parser.dynamicStringToBeMatched = elem.qname.chars;
    }

    void elementEnd() throws SAXException{
        if(elemDepth==elemLock)
            throw fatalError("The element \""+elem.qname.name+"\" must start and end within the same entity");

        if(contentHandler!=null){
            contentHandler.endElement(elem.uri, elem.qname.localName, elem.qname.name);
            for(int i=elem.nsStart; i<nsFree; i+=2)
                contentHandler.endPrefixMapping(namespaces[i]);
        }

        nsFree = elem.nsStart;
        elem = elements[--elemDepth];        

        if(elemDepth==0)
            feeder.parser.pop = true;
    }

    void rootElementEnd() throws SAXException{
        if(elemDepth>0)
            throw fatalError("expected </"+elem.qname.name+">");
    }

    /*-------------------------------------------------[ PI ]---------------------------------------------------*/

    private String piTarget;
    void piTarget(Chars data) throws SAXException{
        piTarget = data.toString();
        if(piTarget.equalsIgnoreCase("xml"))
            throw fatalError("The processing instruction target matching \"[xX][mM][lL]\" is not allowed");
    }

    void piData(Chars piData) throws SAXException{
        if(contentHandler!=null)
            contentHandler.processingInstruction(piTarget, piData.length()>0 ? piData.toString() : "");
    }

    void piData() throws SAXException{
        if(contentHandler!=null)
            contentHandler.processingInstruction(piTarget, "");
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
        if(contentHandler!=null){
            int len = data.length();
            if(len>0){
                if(dtd!=null && dtd.nonMixedElements.contains(elem.qname.name) && isWhitespace(data))
                    contentHandler.ignorableWhitespace(data.array(), data.offset(), len);
                else
                    contentHandler.characters(data.array(), data.offset(), len);
            }
        }
    }

    void cdata(Chars data) throws SAXException{
        if(lexicalHandler!=null)
            lexicalHandler.startCDATA();
        if(contentHandler!=null)
            contentHandler.characters(data.array(), data.offset(), data.length());
        if(lexicalHandler!=null)
            lexicalHandler.endCDATA();
    }

    void comment(Chars data) throws SAXException{
        if(lexicalHandler!=null)
            lexicalHandler.comment(data.array(), data.offset(), data.length());
    }

    @Override
    public SAXException fatalError(String message){
        return fatalError(new SAXParseException(message, this));
    }

    public SAXException fatalError(SAXParseException ex){
        try{
            try{
                if(errorHandler!=null)
                    errorHandler.fatalError(ex);
                return ex;
            }finally{
                if(contentHandler!=null)
                    contentHandler.endDocument();
                if(XMLScanner.SHOW_STATS)
                    xmlScanner.printStats();
            }
        }catch(SAXException e){
            return ex;
        }
    }

    @Override
    public void onSuccessful() throws SAXException{
        if(feeder.getParent()==null){
            if(contentHandler!=null)
                contentHandler.endDocument();
            if(XMLScanner.SHOW_STATS)
                xmlScanner.printStats();
        }
    }

    /*-------------------------------------------------[ DTD ]---------------------------------------------------*/

    void dtdRoot(Chars data){
        dtd = _dtd;
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

    void dtdStart() throws SAXException, IOException{
        if(lexicalHandler!=null)
            lexicalHandler.startDTD(dtd.root, publicID, systemID);
        if(publicID!=null || systemID!=null){
            dtd.externalDTD = feeder.resolve(publicID, systemID);
            publicID = systemID = null;
        }
    }

    private String notationName;
    void notationName(Chars data){
        notationName = data.toString();
    }

    void notationEnd() throws SAXException, IOException{
        systemID = feeder.resolve(systemID);
        if(dtdHandler!=null)
            dtdHandler.notationDecl(notationName, publicID, systemID);
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
        if(dtd.externalDTD!=null){
            InputSource inputSource = dtd.externalDTD;
            dtd.externalDTD = null;

            InputSource is = null;
            if(entityResolver!=null)
                is = entityResolver.resolveEntity(inputSource.getPublicId(), inputSource.getSystemId());

            XMLScanner dtdScanner = new XMLScanner(this, XMLScanner.RULE_EXT_SUBSET_DECL);
            dtdScanner.coalesceNewLines = true;
            encoding = null;
            declScanner.reset(XMLScanner.RULE_TEXT_DECL);
            feeder.setChild(new XMLFeeder(this, dtdScanner, is==null?inputSource:is, declScanner));

        }
        if(lexicalHandler!=null)
            lexicalHandler.endDTD();
    }

    /*-------------------------------------------------[ Entity Definition ]---------------------------------------------------*/

    private String entityName;
    void entityName(Chars data){
        entityName = data.toString();
    }

    private boolean unparsedEntity;
    void notationReference(Chars data) throws IOException, SAXException{
        if(dtdHandler!=null)
            dtdHandler.unparsedEntityDecl(entityName, publicID, feeder.resolve(systemID) , data.toString());
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
            externalDefinition = feeder.getParent()!=null;
            unparsed = unparsedEntity;

            if(systemID==null && publicID==null)
                content = value.toString().toCharArray();
            else{
                externalValue = true;
                inputSource = feeder.resolve(publicID, systemID);
            }
        }

        public InputSource inputSource(boolean wrapWithSpace) throws IOException, SAXException{
            if(inputSource==null){
                InputSource is = new InputSource(getSystemId());
                is.setPublicId(getPublicId());
                is.setCharacterStream(wrapWithSpace ? new SpaceWrappedReader(content) : new CharArrayReader(content));
                return is;
            }else{
                InputSource is = null;
                if(entityResolver!=null)
                    is = entityResolver.resolveEntity(inputSource.getPublicId(), inputSource.getSystemId());
                return is!=null ? is : inputSource;
            }
        }

        public XMLScanner prologParser(){
            XMLScanner prologParser = null;
            if(externalValue){
                prologParser = declScanner;
                prologParser.reset(XMLScanner.RULE_TEXT_DECL);
                encoding = null;
            }
            return prologParser;
        }

        public XMLFeeder parse(int rule) throws IOException, SAXException{
            XMLScanner scanner = new XMLScanner(AsyncXMLReader.this, rule);
            XMLScanner prologParser = prologParser();
            scanner.coalesceNewLines = externalValue;

            XMLFeeder childFeeder = new XMLFeeder(AsyncXMLReader.this, scanner, inputSource(false), prologParser);
            feeder.setChild(childFeeder);
            return childFeeder;
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
            throw fatalError(message.toString());
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

    private Map<String, DTDAttribute> attributeList;
    private DTDAttribute dtdAttribute;

    void dtdAttributesStart(Chars data){
        String dtdElementName = data.toString();
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
            dtdAttribute.fire(declHandler);
        }
        value.setLength(0);
    }

    void attributeRequired() throws SAXException{
        if(dtdAttribute!=null){
            dtdAttribute.valueType = AttributeValueType.REQUIRED;
            dtdAttribute.fire(declHandler);
        }
    }

    void attributeImplied() throws SAXException{
        if(dtdAttribute!=null){
            dtdAttribute.valueType = AttributeValueType.IMPLIED;
            dtdAttribute.fire(declHandler);
        }
    }

    void attributeFixedValue() throws SAXException{
        if(dtdAttribute!=null){
            dtdAttribute.valueType = AttributeValueType.FIXED;
            dtdAttribute.value = dtdAttribute.type.normalize(value.toString());
            dtdAttribute.fire(declHandler);
        }
        value.setLength(0);
    }

    void dtdAttributesEnd(){}
}
