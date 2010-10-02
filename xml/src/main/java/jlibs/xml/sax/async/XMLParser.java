package jlibs.xml.sax.async;

import jlibs.nbp.Chars;
import jlibs.nbp.NBHandler;
import jlibs.xml.NamespaceMap;
import jlibs.xml.sax.SAXDelegate;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class XMLParser implements NBHandler<SAXException>{
    private SAXDelegate delegate = new SAXDelegate();
    private Map<String, char[]> entities = new HashMap<String, char[]>();

    public XMLParser(){
        entities.put("amp",  new char[]{ '&' });
        entities.put("lt",   new char[]{ '<' });
        entities.put("gt",   new char[]{ '>' });
        entities.put("apos", new char[]{ '\'' });
        entities.put("quot", new char[]{ '"' });
    }

    /*-------------------------------------------------[ Document ]---------------------------------------------------*/

    void documentStart() throws SAXException{
        delegate.startDocument();
    }

    void documentEnd() throws SAXException{
        delegate.endDocument();
    }

    /*-------------------------------------------------[ XML Decleration ]---------------------------------------------------*/

    void version(Chars data) throws SAXException{
        if(!"1.0".contentEquals(data))
            throw new SAXException("Unsupported XML Version: "+data);
    }

    void encoding(Chars data) throws SAXException{
        System.out.println("encoding: "+data);
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

    boolean valueStarted = true;
    void valueStart(){
        value.setLength(0);
        valueStarted = true;
    }

    void rawValue(Chars data){
        value.append(data);
    }

    void hexCode(Chars data) throws SAXException{
        int codePoint = Integer.parseInt(data.toString(), 16);
        if(valueStarted)
            value.appendCodePoint(codePoint);
        else{
            char chars[] = Character.toChars(codePoint);
            delegate.characters(chars, 0, chars.length);
        }
    }

    void asciiCode(Chars data) throws SAXException{
        int codePoint = Integer.parseInt(data.toString(), 10);
        if(valueStarted)
            value.appendCodePoint(codePoint);
        else{
            char chars[] = Character.toChars(codePoint);
            delegate.characters(chars, 0, chars.length);
        }
    }

    void entityReference(Chars data) throws SAXException{
        String entity = data.toString();
        char[] entityValue = entities.get(entity);
        if(entityValue==null)
            throw new SAXException("Undefined entityReference: "+entity);
        if(valueStarted)
            value.append(entityValue);
        else
            delegate.characters(entityValue, 0, entityValue.length);
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
            delegate.startPrefixMapping("", value);
        }else if(prefix.equals("xmlns")){
            namespaces.put(localName, value);
            delegate.startPrefixMapping(localName, value);
        }else
            attributes.addAttribute("", localName, qname, "CDATA", value);

        clearQName();
    }

    void attributesEnd() throws SAXException{
        int attrCount = attributes.getLength();
        for(int i=0; i<attrCount; i++)
            attributes.setURI(i, namespaces.getPrefix(attributes.getURI(i)));

        delegate.startElement(namespaces.getNamespaceURI(elementsPrefixes.peek()), elementsLocalNames.peek(), elementsQNames.peek(), attributes);
    }

    void elementEnd() throws SAXException{
        String startQName = elementsQNames.pop();
        if(!startQName.equals(qname))
            throw new SAXException("expected </"+startQName+">");
        String namespaceURI = namespaces.getNamespaceURI(elementsPrefixes.pop());
        delegate.endElement(namespaceURI, elementsLocalNames.pop(), qname);

        for(String nsPrefix: namespaces.map().keySet())
            delegate.endPrefixMapping(nsPrefix);
        namespaces = namespaces.parent();
    }
    
    /*-------------------------------------------------[ PI ]---------------------------------------------------*/

    private String piTarget;
    void piTarget(Chars data){
        piTarget = data.toString();
    }

    void piData(Chars piData) throws SAXException{
        delegate.processingInstruction(piTarget, piData.toString());
    }

    /*-------------------------------------------------[ Misc ]---------------------------------------------------*/

    void characters(Chars data) throws SAXException{
        delegate.characters(data.array(), data.offset(), data.length());
    }

    void cdata(Chars data) throws SAXException{
        delegate.startCDATA();
        delegate.characters(data.array(), data.offset(), data.length());
        delegate.endCDATA();
    }

    void comment(Chars data) throws SAXException{
        delegate.comment(data.array(), data.offset(), data.length());
    }
    
    @Override
    public void fatalError(String message) throws SAXException{
        throw new SAXException(message);
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

    public void dtdStart() throws SAXException{
        delegate.startDTD(dtdRoot, publicID, systemID);
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

    public void dtdEnd() throws SAXException{
        delegate.endDTD();
    }
}
