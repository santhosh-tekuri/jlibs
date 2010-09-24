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

    public abstract boolean matches(char ch);

    public abstract String toString();
    protected final String _toString(){
        if(name==null)
            return toString();
        else
            return "[<"+name+">]";
    }

    private static String SPECIALS = ".[]-^&";
    protected static String encode(char... chars){
        if(chars==null || chars.length==0)
            return ".";
        StringBuilder buff = new StringBuilder();
        for(char ch: chars){
            if(SPECIALS.indexOf(ch)!=-1)
                buff.append('\\');
            if(ch=='"')
                buff.append(ch);
            else
                buff.append(StringUtil.toLiteral(""+ch, false));
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

    public abstract List<jlibs.core.util.Range> ranges();
    public boolean clashesWith(Matcher that){
        if(this.toString().equals("[.]") || that.toString().equals("[.]"))
            return false;
        else
            return !jlibs.core.util.Range.intersection(this.ranges(), that.ranges()).isEmpty();
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