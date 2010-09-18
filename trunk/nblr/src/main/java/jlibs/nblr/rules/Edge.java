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

import jlibs.nblr.matchers.Matcher;
import jlibs.xml.sax.SAXProducer;
import jlibs.xml.sax.XMLDocument;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;

/**
 * @author Santhosh Kumar T
 */
public class Edge implements SAXProducer{
    public Node source;
    public Node target;

    public Edge(Node source, Node target){
        setSource(source);
        setTarget(target);
    }

    public void setSource(Node source){
        if(this.source!=null)
            this.source.outgoing.remove(this);
        this.source = source;
        if(source!=null)
            source.outgoing.add(this);
    }

    public void setTarget(Node target){
        if(this.target!=null)
            this.target.incoming.remove(this);
        this.target = target;
        if(target!=null)
            target.incoming.add(this);
    }

    public Matcher matcher;
    public Rule rule;

    @Override
    public String toString(){
        if(matcher!=null)
            return matcher.name==null ? matcher.toString() : '<'+matcher.name+'>';
        else
            return rule==null ? "" : rule.name;
    }


    /*-------------------------------------------------[ SAXProducer ]---------------------------------------------------*/

    @Override
    public void serializeTo(QName rootElement, XMLDocument xml) throws SAXException{
        xml.startElement("edge");
        xml.addAttribute("source", ""+source.id);
        xml.addAttribute("target", ""+target.id);
        if(matcher!=null){
            if(matcher.name==null)
                xml.add(matcher);
            else{
                xml.startElement("matcher");
                xml.addAttribute("name", matcher.name);
                xml.endElement();
            }
        }else if(rule!=null){
            xml.startElement("rule");
            xml.addAttribute("name", rule.name);
            xml.endElement();
        }
        xml.endElement();
    }

    /*-------------------------------------------------[ Layout ]---------------------------------------------------*/

    public int con;

    public boolean loop(){
        return source==target;
    }

    public boolean sameRow(){
        return source.row==target.row;
    }

    public boolean sameRow(int row){
        return sameRow() && source.row==row;
    }

    public Node min(){
        return source.col<target.col ? source : target;
    }

    public Node max(){
        return source.col>target.col ? source : target;
    }

    public boolean forward(){
        return source.col<target.col;
    }

    public boolean backward(){
        return source.col>target.col;
    }

    public int jump(){
        return Math.abs(source.col-target.col)-1;
    }
}
