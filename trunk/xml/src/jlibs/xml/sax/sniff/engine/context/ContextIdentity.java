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

package jlibs.xml.sax.sniff.engine.context;

import jlibs.xml.sax.sniff.model.DocumentNode;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class ContextIdentity{
    Context context;
    int depth;
    List<Integer> depths = new ArrayList<Integer>();
//        LinkedHashMap<Context, Integer> map = new LinkedHashMap<Context, Integer>();
    public long order;

    ContextIdentity(Context context){
        this(context, context.depth);
    }

    ContextIdentity(Context context, int depth){
        this.context = context;
        this.depth = depth;
        order = context.order;

        int diff = context.depth-depth;
        while(context!=null){
            depths.add(context.depth-diff);
//                map.put(context, context.depth);
            context = context.parent;
        }
    }

    public ContextIdentity parentIdentity(){
        if(depth==0)
            return new ContextIdentity(context.parent, depths.get(1));
        else if(depth<0)
            return new ContextIdentity(context, depth+1);
        else
            return new ContextIdentity(context, depth-1);
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof ContextIdentity){
            ContextIdentity that = (ContextIdentity)obj;
            return this.context==that.context && this.depth==that.depth;
        }else if(obj instanceof Context){
            Context that = (Context)obj;
            return this.context==that && this.depth==that.depth;
        }else
            return false;
    }

    @Override
    public int hashCode(){
        return context.hashCode();
    }

    @Override
    public String toString(){
        return context+"["+depth+']';
    }

    public boolean isChild(Context c){
        if(c.node.depth<this.context.node.depth)
            return false;

        if(this.context==c)
            return Math.abs(this.depth)<Math.abs(c.depth);

        while(c.node.depth>this.context.node.depth)
            c = c.parent;

        if(c.node==this.context.node)
            return this.context==c;

        Node node = c.node;
        while(node!=null){
            if(node==this.context.node)
                return this.context==c;
            node = node.constraintParent;
            if(c==null)
                return false;
            c = c.constraintParent;
        }

        return false;
    }

    public int getDepthTo(Context c){
        int depth = 0;
        while(c!=null){
            if(this.context==c){
                depth += Math.abs(c.depth)-Math.abs(this.depth);
                return depth;
            }else{
                depth += Math.abs(c.depth);
                c = c.parent;
            }
        }
        return -1;
    }

    public boolean isParent(Context c){
        if(c.node instanceof Root || c.node instanceof DocumentNode)
            return true;

        int i = 0;
        Context c1 = this.context;

        while(c1!=null){
            c1 = c1.parent;
            i++;

            if(c1.node.depth<c.node.depth)
                return false;
            if(c1==c && depths.get(i)==c.depth)
                return true;
        }
        return false;
    }

    public boolean isParent(ContextIdentity cid){
        if(cid.context.node instanceof Root || cid.context.node instanceof DocumentNode)
            return true;

        int i = 0;
        Context c1 = this.context;

        while(c1!=null){
            c1 = c1.parent;
            i++;

            if(c1.node.depth<cid.context.node.depth)
                return false;
            if(c1==cid.context && depths.get(i)==cid.depth)
                return true;
        }
        return false;
    }
}
