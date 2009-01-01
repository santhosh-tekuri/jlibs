package jlibs.xml.sax;

import jlibs.core.lang.StringUtil;
import jlibs.xml.sax.helpers.MyNamespaceSupport;
import jlibs.xml.sax.helpers.NamespaceSupportReader;
import jlibs.xml.transform.TransformerUtil;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Enumeration;
import java.util.Stack;

/**
 * @author Santhosh Kumar T
 */
public abstract class ObjectInputSource<E> extends InputSource{
    private final E obj;

    public ObjectInputSource(E obj){
        this.obj = obj;
    }

    public E getObject(){
        return obj;
    }

    /*-------------------------------------------------[ bootstrap ]---------------------------------------------------*/
    
    private XMLWriter xml;
    void writeTo(XMLWriter xml)throws SAXException{
        this.xml = xml;
        nsSupport.reset();
        attrs.clear();
        elemStack.clear();
        elem = null;
        nsSupport.pushContext();
        
        try{
            xml.startDocument();
            mark();
            write(obj);
            release(0);
            xml.endDocument();
        }catch(RuntimeException ex){
            throw new SAXException(ex);
        }finally{
            this.xml = null;
        }
    }
    
    protected abstract void write(E obj) throws SAXException;

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

    private String toString(String uri, String localPart){
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

    public ObjectInputSource<E> startElement(String name) throws SAXException{
        finishStartElement();
        return startElement("", name);
    }

    public ObjectInputSource<E> startElement(String uri, String name) throws SAXException{
        finishStartElement();
        elem = declareQName(uri, name);
        return this;
    }

    /*-------------------------------------------------[ Add-Element ]---------------------------------------------------*/

    public ObjectInputSource<E> addElement(String name, String text, boolean cdata) throws SAXException{
        return addElement("", name, text, cdata);
    }
    
    public ObjectInputSource<E> addElement(String uri, String name, String text, boolean cdata) throws SAXException{
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
    
    public ObjectInputSource<E> addElement(String name, String text) throws SAXException{
        return addElement("", name, text, false);
    }

    public ObjectInputSource<E> addElement(String uri, String name, String text) throws SAXException{
        return addElement(uri, name, text, false);
    }

    public ObjectInputSource<E> addCDATAElement(String name, String text) throws SAXException{
        return addElement("", name, text, true);
    }

    public ObjectInputSource<E> addCDATAElement(String uri, String name, String text) throws SAXException{
        return addElement(uri, name, text, true);
    }

    /*-------------------------------------------------[ Attributes ]---------------------------------------------------*/

    private AttributesImpl attrs = new AttributesImpl();

    public ObjectInputSource<E> addAttribute(String name, String value) throws SAXException{
        return addAttribute("", name, value);
    }

    public ObjectInputSource<E> addAttribute(String uri, String name, String value) throws SAXException{
        if(elem==null)
            throw new SAXException("no start element found to associate this attribute");
        if(value!=null)
            attrs.addAttribute(uri, name, toString(uri, name), "CDATA", value);
        return this;
    }

    /*-------------------------------------------------[ Text ]---------------------------------------------------*/

    public ObjectInputSource<E> addText(String text) throws SAXException{
        if(!StringUtil.isEmpty(text)){
            finishStartElement();
            xml.characters(text.toCharArray(), 0, text.length());
        }
        return this;
    }

    public ObjectInputSource<E> addCDATA(String text) throws SAXException{
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

    private ObjectInputSource<E> endElement(QName qname) throws SAXException{
        xml.endElement(qname.getNamespaceURI(), qname.getLocalPart(), toString(qname));

        endPrefixMapping(nsSupport);
        nsSupport.popContext();
        return this;
    }

    public ObjectInputSource<E> endElement(String uri, String name) throws SAXException{
        QName qname = findEndElement();
        if(!qname.getNamespaceURI().equals(uri) || !qname.getLocalPart().equals(name))
            throw new SAXException("expected </"+toString(qname)+'>');
        return endElement(qname);
    }

    public ObjectInputSource<E> endElement(String name) throws SAXException{
        return endElement("", name);
    }

    public ObjectInputSource<E> endElement() throws SAXException{
        return endElement(findEndElement());
    }

    /*-------------------------------------------------[ end-elements ]---------------------------------------------------*/
    
    public ObjectInputSource<E> endElements(String uri, String name) throws SAXException{
        QName qname = findEndElement();
        while(true){
            endElement(qname);
            if(qname.getNamespaceURI().equals(uri) && qname.getLocalPart().equals(name))
                break;
        }
        return this;
    }

    public ObjectInputSource<E> endElements(String name) throws SAXException{
        return endElements("", name);
    }

    public ObjectInputSource<E> endElements() throws SAXException{
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

    public ObjectInputSource<E> addXML(String xmlString, boolean excludeRoot) throws SAXException{
        if(!StringUtil.isWhitespace(xmlString))
            addXML(new InputSource(new StringReader(xmlString)), excludeRoot);
        return this;
    }

    public ObjectInputSource<E> addXML(InputSource is, boolean excludeRoot) throws SAXException{
        finishStartElement();
        try{
            if(excludeRoot){
                NamespaceSupportReader nsReader = new NamespaceSupportReader(false){
                    private int depth = 0;

                    @Override
                    public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts) throws SAXException{
                        depth++;
                        if(depth==2)
                            ObjectInputSource.this.startPrefixMapping(getNamespaceSupport());
                        super.startElement(namespaceURI, localName, qualifiedName, atts);

                    }

                    @Override
                    public void endElement(String uri, String localName, String qName) throws SAXException{
                        super.endElement(uri, localName, qName);
                        if(depth==2)
                            ObjectInputSource.this.endPrefixMapping(getNamespaceSupport());
                        depth--;
                    }
                };
                SAXDelegate delegate = new SAXDelegate(){
                    private int depth = 0;

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
                        if(depth>0)
                            super.startPrefixMapping(prefix, uri);
                    }

                    @Override
                    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException{
                        depth++;
                        if(depth>1)
                            super.startElement(uri, localName, qName, atts);
                    }

                    @Override
                    public void endElement(String uri, String localName, String qName) throws SAXException{
                        if(depth>1)
                            super.endElement(uri, localName, qName);
                        depth--;
                    }

                    @Override
                    public void endPrefixMapping(String prefix) throws SAXException{
                        if(depth>0)
                            super.endPrefixMapping(prefix);
                    }
                };
                delegate.setDefaultHandler(xml);
                nsReader.parse(is, delegate);
            }else{
                SAXDelegate delegate = new SAXDelegate(){
                    @Override public void startDocument(){}
                    @Override public void endDocument(){}
                    @Override public void setDocumentLocator(Locator locator){}
                };
                delegate.setDefaultHandler(xml);
                SAXUtil.newSAXParser(true, false).parse(is, delegate);
            }
        }catch(ParserConfigurationException ex){
            throw new SAXException(ex);
        }catch(IOException ex){
            throw new SAXException(ex);
        }
        return this;
    }

    /*-------------------------------------------------[ Others ]---------------------------------------------------*/

    public ObjectInputSource<E> addProcessingInstruction(String target, String data) throws SAXException{
        xml.processingInstruction(target, data);
        return this;
    }

    public ObjectInputSource<E> addComment(String text) throws SAXException{
        if(!StringUtil.isEmpty(text)){
            finishStartElement();
            xml.comment(text.toCharArray(), 0, text.length());
        }
        return this;
    }

    public ObjectInputSource<E> add(SAXProducer saxProducer) throws SAXException{
        if(saxProducer!=null){
            QName qname = saxProducer.getRootElement();
            add(saxProducer, qname.getNamespaceURI(), qname.getLocalPart());
        }
        return this;
    }

    public ObjectInputSource<E> add(SAXProducer saxProducer, String name) throws SAXException{
        return add(saxProducer, "", name);
    }
    
    public ObjectInputSource<E> add(SAXProducer saxProducer, String uri, String name) throws SAXException{
        if(saxProducer!=null){
            mark();
            startElement(uri, name);
            saxProducer.writeAttributes(this);
            saxProducer.writeContent(this);
            endElements();
            release();
        }
        return this;
    }

    /*-------------------------------------------------[ DTD ]---------------------------------------------------*/

    public ObjectInputSource<E> addPublicDTD(String name, String publicId, String systemID) throws SAXException{
        xml.startDTD(name, publicId, systemID);
        xml.endDTD();
        return this;
    }

    public ObjectInputSource<E> addSystemDTD(String name, String systemId) throws SAXException{
        xml.startDTD(name, null, systemId);
        xml.endDTD();
        return this;
    }

    /*-------------------------------------------------[ Serialization ]---------------------------------------------------*/

    private SAXSource createSource(){
        return new SAXSource(new XMLWriter(), this);
    }

    public void writeTo(Writer writer, boolean omitXMLDeclaration, int indentAmount) throws TransformerException{
        Transformer transformer = TransformerUtil.newTransformer(null, omitXMLDeclaration, indentAmount, null);
        transformer.transform(createSource(), new StreamResult(writer));
    }

    public void writeTo(OutputStream out, boolean omitXMLDeclaration, int indentAmount, String encoding) throws TransformerException{
        Transformer transformer = TransformerUtil.newTransformer(null, omitXMLDeclaration, indentAmount, encoding);
        transformer.transform(createSource(), new StreamResult(out));
    }

    public void writeTo(String systemID, boolean omitXMLDeclaration, int indentAmount, String encoding) throws TransformerException{
        Transformer transformer = TransformerUtil.newTransformer(null, omitXMLDeclaration, indentAmount, encoding);
        transformer.transform(createSource(), new StreamResult(systemID));
    }

    /*-------------------------------------------------[ Test ]---------------------------------------------------*/
    
    public static void main(String[] args) throws Exception{
        new ObjectInputSource<String>(null){
            @Override
            protected void write(String obj) throws SAXException{
                String google = "http://google.com";
                String yahoo = "http://yahoo.com";

                addProcessingInstruction("san", "test='1.2'");

                declarePrefix("google", google);
                declarePrefix("yahoo", yahoo);
                declarePrefix("http://msn.com");
                
                startElement(google, "hello");
                addAttribute("name", "value");
                addElement("xyz", "helloworld");
                addElement(google, "hai", "test");
                addXML(new InputSource("xml/xsds/note.xsd"), true);
                addComment("this is comment");
                addCDATA("this is sample cdata");
            }
        }.writeTo(new OutputStreamWriter(System.out, "utf-8"), false, 4);
    }
}
