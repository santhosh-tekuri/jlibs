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

package jlibs.nblr;

import jlibs.core.annotation.processing.Printer;
import jlibs.nblr.codegen.JavaCodeGenerator;
import jlibs.nblr.matchers.Matcher;
import jlibs.nblr.rules.Rule;
import jlibs.xml.sax.SAXProducer;
import jlibs.xml.sax.XMLDocument;
import jlibs.xml.sax.binding.BindingHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamResult;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import static jlibs.nblr.matchers.Matcher.*;

/**
 * @author Santhosh Kumar T
 */
public class Syntax implements SAXProducer{
    public final Map<String, Matcher> matchers = new LinkedHashMap<String, Matcher>();
    public Matcher add(String name, Matcher matcher){
        matcher.name = name;
        matchers.put(name, matcher);
        return matcher;
    }

    public final Map<String, Rule> rules = new LinkedHashMap<String, Rule>();
    public Rule add(String name, Rule rule){
        rule.id = rules.size();
        rule.name = name;
        rules.put(name, rule);
        return rule;
    }

    /*-------------------------------------------------[ SAXProducer ]---------------------------------------------------*/
    
    @Override
    public void serializeTo(QName rootElement, XMLDocument xml) throws SAXException{
        xml.startElement("syntax");
        for(Matcher matcher: matchers.values())
            xml.add(matcher);
        for(Rule rule: rules.values())
            xml.add(rule);
        xml.endElement();
    }

    /*-------------------------------------------------[ Test ]---------------------------------------------------*/
    
    public static void main(String[] args) throws Exception{
        Syntax syntax = new Syntax();

        Matcher GT              = syntax.add("GT",             ch('>'));
        Matcher BRACKET_CLOSE   = syntax.add("BRACKET_CLOSE",  ch(']'));
        Matcher Q               = syntax.add("Q",              ch('\''));
        Matcher DQ              = syntax.add("DQ",             ch('"'));
        Matcher DIGIT           = syntax.add("DIGIT",          range("0-9"));
        Matcher WS              = syntax.add("WS",             any(" \t\n\r"));
        Matcher ENCODING_START  = syntax.add("ENCODING_START", or(range("A-Z"), range("a-z")));
        Matcher ENCODING_PART   = syntax.add("ENCODING_PART",  or(ENCODING_START, DIGIT, any("._-")));
        Matcher CHAR            = syntax.add("CHAR",           or(any("\t\n\r"), range(" -\uD7FF"), range("\uE000-\uFFFD")/*, range("\u10000-\u10FFFF")*/));
        Matcher DASH            = syntax.add("DASH",           ch('-'));
        Matcher NDASH           = syntax.add("NDASH",          minus(CHAR, ch('-')));

        Printer printer = new Printer(new PrintWriter(System.out, true));
        new JavaCodeGenerator(syntax, printer).generateCode();
        
        String file = "syntax.xml";

        XMLDocument xml = new XMLDocument(new StreamResult(file), true, 4, null);
        xml.startDocument();
        xml.add(syntax);
        xml.endDocument();

        BindingHandler handler = new BindingHandler(SyntaxBinding.class);
        Syntax newSyntax = (Syntax)handler.parse(new InputSource(file));

        xml = new XMLDocument(new StreamResult(System.out), true, 4, null);
        xml.startDocument();
        xml.add(newSyntax);
        xml.endDocument();
    }
}
