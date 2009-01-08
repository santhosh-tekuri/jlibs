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

import org.xml.sax.Attributes;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class Node{
    static final RuntimeException EVALUATION_FINISHED = new RuntimeException("Evaluation Finished");
    protected Root root;
    protected Node parent;
    protected List<Node> children = new ArrayList<Node>();

    protected Node(Node parent){
        this.parent = parent;
        if(parent!=null){
            parent.children.add(this);
            root = parent.root;
        }
    }

    protected void reset(){
        hitCount = 0;
        result.clear();
        depth = 0;
        for(Node child: children)
            child.reset();
    }

    /*-------------------------------------------------[ Finding ]---------------------------------------------------*/
    
    protected Element findElement(QName qname){
        for(Node node: children){
            if(node instanceof Element){
                Element elem = (Element)node;
                if(elem.matchesElement(qname.getNamespaceURI(), qname.getLocalPart()))
                    return elem;
            }
        }
        return null;
    }

    protected Attribute findAttribute(String uri, String name){
        for(Node node: children){
            if(node instanceof Attribute){
                Attribute attr = (Attribute)node;
                if(attr.matchesAttribute(uri, name))
                    return attr;
            }
        }
        return null;
    }

    protected Text findText(){
        for(Node node: children){
            if(node instanceof Text)
                return (Text)node;
        }
        return null;
    }

    protected Descendant findDescendant(boolean self){
        for(Node node: children){
            if(node instanceof Self==self){
                if(node instanceof Descendant)
                    return (Descendant)node;
            }
        }
        return null;
    }

    /*-------------------------------------------------[ Hitting ]---------------------------------------------------*/
    
    private int hitCount;
    private List<String> result = new ArrayList<String>();

    protected Node hit(){
        hitCount++;
        if(minHits!=-1){
            root.timer--;
            if(root.timer==0)
                throw EVALUATION_FINISHED;
        }
        return this;
    }

    protected Node hit(String value){
        try{
            return hit();
        }finally{
            result.add(value);
        }
    }

    private int minHits = -1;
    protected void setMinHits(int hits){
        if(minHits!=-1)
            root.totalMinHits -= minHits;
        this.minHits = hits;
        root.totalMinHits += minHits;
    }

    public int getHitCount(){
        return hitCount;
    }

    public List<String> getResult(){
        return result;
    }
    
    /*-------------------------------------------------[ Matching ]---------------------------------------------------*/

    protected boolean matchesElement(String uri, String name){
        return false;
    }

    protected boolean matchesAttribute(String uri, String name){
        return false;
    }

    protected int depth;
    protected List<Node> matchStartElement(String uri, String name){
        List<Node> list = new ArrayList<Node>();
        for(Node child: children){
            if(child.matchesElement(uri, name)){
                list.add(child.hit());
                Descendant desc = child.findDescendant(true);
                if(desc!=null){
                    list.add(desc.hit());
                }
            }
        }
        if(list.size()==0){
            depth++;
            list.add(this);
        }
        return list;
    }

    protected Node matchEndElement(){
        if(depth>0){
            depth--;
            return this;
        }else{
            if(this instanceof Self){
                if(parent.parent==null)
                    return parent;
                else
                    return parent.parent;
            }else
                return parent;
        }
    }

    protected void matchAttributes(Attributes attributes){
        if(depth==0){
            for(int i=0; i<attributes.getLength(); i++){
                Attribute attribute = findAttribute(attributes.getURI(i), attributes.getLocalName(i));
                if(attribute!=null)
                    attribute.hit(attributes.getValue(i));
            }
        }
    }

    protected void matchText(StringContent content){
        if(depth==0){
            Text text = findText();
            if(text!=null)
                text.hit(content.toString());
        }
    }

    protected abstract String getStep();
    
    public String getXPath(){
        StringBuilder buff = new StringBuilder();
        Node node = this;
        while(node!=null){
            if(buff.length()>0)
                buff.insert(0, '/');
            buff.insert(0, node.getStep());
            node = node.parent;
        }
        if(buff.length()>0 && buff.charAt(0)!='/')
            buff.insert(0, '/');
        return buff.toString();
    }

    @Override
    public String toString(){
        StringBuilder buff = new StringBuilder(getXPath()+"\n");
        if(hitCount==0)
            buff.append("\tno matches\n");
        else{
            if(result.size()==0)
                buff.append("\t").append(hitCount).append(" matches\n");
            else{
                for(int i=0; i<result.size(); i++)
                    buff.append("\t").append(i + 1).append(": ").append(result.get(i)).append("\n");
            }
        }
        return buff.toString();
    }
}
