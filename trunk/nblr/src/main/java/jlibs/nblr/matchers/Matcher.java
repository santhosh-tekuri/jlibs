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

package jlibs.nblr.matchers;

import jlibs.core.lang.StringUtil;
import jlibs.xml.sax.SAXProducer;
import jlibs.xml.sax.XMLDocument;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class Matcher implements SAXProducer{
    public String name;

    public abstract String toString();
    protected final String _toString(){
        if(name==null)
            return toString();
        else
            return "[<"+name+">]";
    }

    protected String toJava(int codePoint){
        if(codePoint>0x0020 && codePoint<0x007f) // visible character in ascii)
            return '\''+StringUtil.toLiteral((char)codePoint, false)+'\'';
        else
            return "0x"+Integer.toHexString(codePoint);
    }
    
    private static String SPECIALS = "\\#.[]-^&";
    protected static String encode(int... chars){
        if(chars==null || chars.length==0)
            return ".";
        StringBuilder buff = new StringBuilder();
        for(int ch: chars){
            if(SPECIALS.indexOf(ch)!=-1)
                buff.append('\\').append(ch);
            else{
                if(ch=='"')
                    buff.append(ch);
                else{
                    if(ch>0x0020 && ch<0x007f) // visible character in ascii)
                        buff.append(StringUtil.toLiteral(""+(char)ch, false));
                    else
                        buff.append("#x").append(Integer.toHexString(ch)).append(';');
                }
            }
        }
        return buff.toString();
    }

    public abstract String javaCode(String variable);
    public final String _javaCode(String variable){
        if(name==null)
            return javaCode(variable);
        else
            return name+'('+variable+')';
    }

    /*-------------------------------------------------[ Ranges ]---------------------------------------------------*/

    public abstract List<jlibs.core.util.Range> ranges();

    public boolean clashesWith(Matcher that){
        return !jlibs.core.util.Range.intersection(this.ranges(), that.ranges()).isEmpty();
    }

    public boolean same(Matcher that){
        return jlibs.core.util.Range.same(this.ranges(), that.ranges());
    }

    /*-------------------------------------------------[ SAXProducer ]---------------------------------------------------*/

    protected final void serializeTo(XMLDocument xml) throws SAXException{
        if(name==null)
            serializeTo(null, xml);
        else{
            xml.startElement("matcher");
            xml.addAttribute("name", name);
            xml.endElement();
        }
    }

    @Override
    public void serializeTo(QName rootElement, XMLDocument xml) throws SAXException{
        xml.startElement(getClass().getSimpleName().toLowerCase());
        if(name!=null)
            xml.addAttribute("name", name);
        addBody(xml);
        xml.endElement();
    }

    protected abstract void addBody(XMLDocument xml) throws SAXException;

    /*-------------------------------------------------[ Factory ]---------------------------------------------------*/

    public static Matcher any(String chars){
        return new Any(chars);
    }

    public static Matcher any(){
        return new Any();
    }

    public static Matcher ch(char ch){
        return new Any(ch);
    }

    public static Matcher range(String range){
        return new Range(range);
    }

    public static Matcher not(Matcher ch){
        return new Not(ch);
    }

    public static Matcher and(Matcher... operands){
        return new And(operands);
    }

    public static Matcher or(Matcher... operands){
        return new Or(operands);
    }

    public static Matcher minus(Matcher lhs, Matcher rhs){
        return new And(lhs, not(rhs));
    }

    public static void main(String[] args){
        
    }
}