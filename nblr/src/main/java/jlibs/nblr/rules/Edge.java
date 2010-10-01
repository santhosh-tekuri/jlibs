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

/**
 * @author Santhosh Kumar T
 */
public class Edge{
    public Node source;
    public Node target;
    public boolean fallback;

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

    public void delete(){
        setSource(null);
        setTarget(null);
    }

    public Matcher matcher;
    public RuleTarget ruleTarget;
//    public Rule rule;
//    public String name;

    @Override
    public String toString(){
        String prefix = fallback ? "#" : "";
        if(matcher!=null)
            return prefix+(matcher.name==null ? matcher.toString() : '<'+matcher.name+'>');
        else if(ruleTarget!=null)
            return prefix+ruleTarget;
        else
            return "";
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
