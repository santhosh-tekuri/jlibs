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

import jlibs.nblr.matchers.Matcher;
import jlibs.nblr.rules.Rule;
import jlibs.xml.sax.SAXProducer;
import jlibs.xml.sax.XMLDocument;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.util.LinkedHashMap;
import java.util.Map;

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
        rule.name = name;
        rules.put(name, rule);
        
        int id = 0;
        for(Rule r: rules.values())
            r.id = id++;
        
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
}
