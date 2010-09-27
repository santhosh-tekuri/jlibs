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

import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class Path extends ArrayList<Object>{
    public Path(Deque<Object> stack){
        super(stack);
        Collections.reverse(this);
    }

    public Edge matcherEdge(){
        if(size()>1 && get(size()-2) instanceof Edge)
            return (Edge)get(size()-2);
        else
            return null;
    }

    @SuppressWarnings({"SimplifiableConditionalExpression"})
    public boolean fallback(){
        for(Object obj: this){
            if(obj instanceof Edge){
                Edge edge = (Edge)obj;
                if((edge.matcher!=null || edge.ruleTarget!=null) && edge.fallback)
                    return true;
            }
        }
        return false;
    }

    public Matcher matcher(){
        Edge matcherEdge = matcherEdge();
        return matcherEdge!=null ? matcherEdge.matcher : null;
    }

    public boolean clashesWith(Path that){
        if(this.depth!=that.depth)
            throw new IllegalArgumentException("depths are not same: "+this.depth+"!="+that.depth);
        if(depth>1){
            if(!this.parent.clashesWith(that.parent))
                return false;
        }
        return this.matcher().clashesWith(that.matcher());
    }

    public Paths children;
    public Path parent;
    public int depth;
    public int branch;

    public boolean hasLoop(){
        Node lastNode = (Node)get(size()-1);
        if(subList(0, size()-1).contains(lastNode))
            return true;
        Path path = parent;
        while(path!=null){
            if(path.contains(lastNode))
                return true;
            path = path.parent;
        }
        return false;
    }

    public Path[] route(){
        List<Path> route = new LinkedList<Path>();
        Path p = this;
        while(p!=null){
            route.add(0, p);
            p = p.parent;
        }
        return route.toArray(new Path[route.size()]);
    }

    @Override
    public String toString(){
        String str;
        Matcher matcher = matcher();
        if(matcher==null)
            str = "<EOF>";
        else if(matcher.name!=null)
            str = '<'+matcher.name+'>';
        else
            str = matcher.toString();
        return parent!=null ? parent+str : str;
    }
}
