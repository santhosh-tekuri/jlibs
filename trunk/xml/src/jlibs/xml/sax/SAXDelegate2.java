package jlibs.xml.sax;

import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.SAXException;
import org.jetbrains.annotations.Nullable;

/**
 * @author Santhosh Kumar T
 */
public class SAXDelegate2 extends SAXDelegate implements LexicalHandler, DeclHandler{

    /*-------------------------------------------------[ LexicalHandler ]---------------------------------------------------*/

    private @Nullable LexicalHandler lexicalHandler;

    @Nullable
    public LexicalHandler getLexicalHandler(){
        return lexicalHandler;
    }

    public void setLexicalHandler(@Nullable LexicalHandler lexicalHandler){
        this.lexicalHandler = lexicalHandler;
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException{
        if(lexicalHandler!=null)
            lexicalHandler.startDTD(name, publicId, systemId);
    }

    public void endDTD() throws SAXException{
        if(lexicalHandler!=null)
            lexicalHandler.endDTD();
    }

    public void startEntity(String name) throws SAXException{
        if(lexicalHandler!=null)
            lexicalHandler.startEntity(name);
    }

    public void endEntity(String name) throws SAXException{
        if(lexicalHandler!=null)
            lexicalHandler.endEntity(name);
    }

    public void startCDATA() throws SAXException{
        if(lexicalHandler!=null)
            lexicalHandler.startCDATA();
    }

    public void endCDATA() throws SAXException{
        if(lexicalHandler!=null)
            lexicalHandler.endCDATA();
    }

    public void comment(char[] ch, int start, int length) throws SAXException{
        if(lexicalHandler!=null)
            lexicalHandler.comment(ch, start, length);
    }

    /*-------------------------------------------------[ DeclHandler ]---------------------------------------------------*/

    private @Nullable DeclHandler declHandler;

    @Nullable
    public DeclHandler getDeclHandler(){
        return declHandler;
    }

    public void setDeclHandler(@Nullable DeclHandler declHandler){
        this.declHandler = declHandler;
    }

    public void elementDecl(String name, String model) throws SAXException{
        if(declHandler!=null)
            declHandler.elementDecl(name, model);
    }

    public void attributeDecl(String eName, String aName, String type, String mode, String value) throws SAXException{
        if(declHandler!=null)
            declHandler.attributeDecl(eName, aName, type, mode, value);
    }

    public void internalEntityDecl(String name, String value) throws SAXException{
        if(declHandler!=null)
            declHandler.internalEntityDecl(name, value);
    }

    public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException{
        if(declHandler!=null)
            declHandler.externalEntityDecl(name, publicId, systemId);
    }
}
