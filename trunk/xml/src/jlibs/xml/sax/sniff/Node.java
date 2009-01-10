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
import java.util.Map;

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

//    protected Descendant findDescendant(boolean self){
//        for(Node node: children){
//            if(node instanceof AutoMatch==self){
//                if(node instanceof Descendant)
//                    return (Descendant)node;
//            }
//        }
//        return null;
//    }

    protected List<Match> findMatches(){
        List<Match> list = new ArrayList<Match>(children.size());

        for(Node child: children){
            if(child instanceof Match)
                list.add((Match)child);
        }
        return list;
    }

    protected List<Node> findAutoMatches(){
        List<Node> list = new ArrayList<Node>(children.size());

        for(Node child: children){
            if(child instanceof AutoMatch)
                list.add(child);
        }
        return list;
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
    protected final List<Node> matchChildren(String uri, String name, int pos){
        List<Node> list = new ArrayList<Node>();
        for(Node child: children){
            if(child.matchesElement(uri, name)){
                list.add(child.hit());

                for(Match matchNode: child.findMatches()){
                    if(matchNode.matchesStartElement(uri, name, pos))
                        list.add(((Node)matchNode).hit());
                }

                for(Node autoMatchNode: child.findAutoMatches())
                    list.add(autoMatchNode.hit());
            }
        }
        return list;
    }

    protected List<Node> matchStartElement(String uri, String name, int pos){
        List<Node> list = matchChildren(uri, name, pos);

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
            if(this instanceof Match || this instanceof AutoMatch){
                if(parent.parent==null)
                    return parent;
                else
                    return parent.parent;
            }else
                return parent;
        }
    }

    protected final void _matchAttributes(Attributes attributes){
        for(int i=0; i<attributes.getLength(); i++){
            Attribute attribute = findAttribute(attributes.getURI(i), attributes.getLocalName(i));
            if(attribute!=null){
                attribute.hit(attributes.getValue(i));

                if(XMLDog.debug){
                    System.out.print("attributeHit -> ");
                    attribute.println();
                }
            }
        }
    }

    protected void matchAttributes(Attributes attributes){
        if(depth==0)
            _matchAttributes(attributes);
    }

    protected final void _matchText(StringContent content){
        Text text = findText();
        if(text!=null)
            text.hit(content.toString());
    }

    protected void matchText(StringContent content){
        if(depth==0)
            _matchText(content);
    }

    protected abstract String getStep();
    
    public String getXPath(){
        StringBuilder buff = new StringBuilder();
        Node node = this;
        String separator = "/";
        while(node!=null){
            if(buff.length()>0)
                buff.insert(0, separator);
            buff.insert(0, node.getStep());
            if(node instanceof Position)
                separator = "";
            else
                separator = "/";
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

    protected abstract boolean canMerge(Node node);

    protected final Map<Node, Node> merge(Node that, Map<Node, Node> map){
        for(Node thatChild: that.children){
            boolean merged = false;
            for(Node thisChild: this.children){
                if(thatChild.canMerge(thisChild)){
                    map.put(thatChild, thisChild);
                    thisChild.merge(thatChild, map);
                    merged = true;
                    break;
                }
            }
            if(!merged){
                this.children.add(thatChild);
                thatChild.parent = this;
                thatChild.setRoot(root);
            }
        }
        return map;
    }

    private void setRoot(Root root){
        this.root = root;
        for(Node child: children)
            child.setRoot(root);
    }

    protected void verifyIntegrity(){
        if(parent!=null && root!=parent.root)
            throw new IllegalStateException();
        for(Node child: children){
            if(child.parent!=this)
                throw new IllegalStateException();
            child.verifyIntegrity();
        }
    }

    protected void println(){
        System.out.format(" :: depth:%d hitcount:%d lastresult:%s%n", depth, hitCount, result.size()>0?result.get(result.size()-1):"");
    }
}
