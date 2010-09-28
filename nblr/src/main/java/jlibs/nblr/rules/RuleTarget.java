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

package jlibs.nblr.rules;

import jlibs.xml.sax.SAXProducer;
import jlibs.xml.sax.XMLDocument;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;

/**
 * @author Santhosh Kumar t
 */
public class RuleTarget implements SAXProducer{
    public Rule rule;
    public String name;

    public Node node(){
        return name==null ? rule.node : rule.nodeWithName(name);
    }
    
    public String toString(){
        return name==null ? rule.name : rule.name+':'+name;
    }

    /*-------------------------------------------------[ SAXProducer ]---------------------------------------------------*/
    
    @Override
    public void serializeTo(QName rootElement, XMLDocument xml) throws SAXException{
        xml.startElement("rule");
        xml.addAttribute("name", rule.name);
        xml.addAttribute("node", name);
        xml.endElement();
    }
}
