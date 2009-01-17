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

import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Position;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class PositionTracker{
    Map<Position, Map<ContextIdentity, Integer>> childHitCount = new HashMap<Position, Map<ContextIdentity, Integer>>();

    public boolean hit(ContextManager.Context context, Position position){
        if(position.parent!=context.node)
            context = context.parent;
        
        Map<ContextIdentity, Integer> map = childHitCount.get(position);
        if(map==null)
            childHitCount.put(position, map=new HashMap<ContextIdentity, Integer>());
        Integer pos = map.get(new ContextIdentity(context));
        if(pos==null)
            pos = 1;
        else
            pos++;
        map.put(new ContextIdentity(context), pos);
        return pos==position.pos;
    }

    public void contextEnded(ContextManager.Context context){
        for(Node child: context.node.children())
            clearHitCounts(context, child);
    }

    private void clearHitCounts(ContextManager.Context context, Node node){
        for(Node constraint: node.constraints()){
            if(constraint instanceof Position){
                Position position = (Position)constraint;
                Map<ContextIdentity, Integer> map = childHitCount.get(position);
                if(map!=null)
                    map.remove(new ContextIdentity(context));
            }
            clearHitCounts(context, constraint);
        }
    }

    public int getHitCount(ContextManager.Context context, Position position){
        Map<ContextIdentity, Integer> map = childHitCount.get(position);
        if(map==null)
            childHitCount.put(position, map=new HashMap<ContextIdentity, Integer>());
        Integer pos = map.get(new ContextIdentity(context));
        if(pos==null)
            pos = 0;

        return pos;
    }

    static final class ContextIdentity{
        ContextManager.Context context;
        int depth;

        ContextIdentity(ContextManager.Context context){
            this.context = context;
            depth = context.depth;
        }

        @Override
        public boolean equals(Object obj){
            if(obj instanceof ContextIdentity){
                ContextIdentity that = (ContextIdentity)obj;
                return this.context==that.context && this.depth==that.depth;
            }else
                return false;
        }

        @Override
        public int hashCode(){
            return System.identityHashCode(context)+depth;
        }
    }
}
