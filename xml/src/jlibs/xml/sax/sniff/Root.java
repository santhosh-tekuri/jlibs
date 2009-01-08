/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.sax.sniff;

import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.Namespaces;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.regex.Pattern;

/**
 * @author Santhosh Kumar T
 */
class Root extends Node{
    protected int totalMinHits;
    protected int timer;
    private NamespaceContext nsContext;

    public Root(NamespaceContext nsContext){
        super(null);
        root = this;
        this.nsContext = nsContext;
    }

    protected void reset(){
        timer = totalMinHits;
        super.reset();
    }
    
    public Node add(String xpath, int minHits){
        Node node = add(xpath);
        node.setMinHits(minHits);
        return node;
    }
    
    private Node add(String xpath){
        if(xpath.startsWith("/"))
            xpath = xpath.substring(1);

        Node node = this;
        String tokens[] = Pattern.compile("/", Pattern.LITERAL).split(xpath);
        for(String token: tokens){
            if(token.equals("text()")){
                Text text = node.findText();
                if(text==null)
                    text = new Text(node);
                node = text;
            }else if(token.equals("")){
                Descendant desc = node.findDescendant(true);
                if(desc==null)
                    desc = new DescendantSelf(node);
                node = desc;
            }else{
                boolean attribute = token.startsWith("@");
                if(attribute)
                    token = token.substring(1);

                QName qname;
                int colon = token.indexOf(':');
                if(colon!=-1){
                    String prefix = token.substring(0, colon);
                    String uri = nsContext.getNamespaceURI(prefix);
                    qname = new QName(uri, token.substring(colon+1), prefix);
                }else
                    qname = new QName("", token, "");
                if(attribute){
                    Attribute attr = node.findAttribute(qname.getNamespaceURI(), qname.getLocalPart());
                    if(attr==null)
                        attr = new Attribute(node, qname);
                    node = attr;
                }else{
                    Element elem = node.findElement(qname);
                    if(elem==null)
                        elem = new Element(node, qname);
                    node = elem;
                }
            }
        }
        return node;
    }

    @Override
    protected String getStep(){
        return "";
    }

    public static void main(String[] args){
        DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
        nsContext.declarePrefix("xsd", Namespaces.URI_XSD);
        Root root = new Root(nsContext);

        root.add("xsd:schema//xsd:import/@xsd:test");
        root.add("xsd:schema/xsd:import/@xsd:test1");
        System.out.println("");
    }
}
