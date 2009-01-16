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

package jlibs.xml.sax.sniff.position;

import jlibs.xml.sax.sniff.ContextManager;
import jlibs.xml.sax.sniff.model.Position;
import jlibs.xml.sax.sniff.model.Node;

import java.util.Map;
import java.util.HashMap;

import org.jaxen.saxpath.Axis;

/**
 * @author Santhosh Kumar T
 */
public class ChildPositionTracker implements PositionTracker{
    Map<Position, Map<ChildContext, Integer>> childHitCount = new HashMap<Position, Map<ChildContext, Integer>>();

    public boolean hit(ContextManager.Context context, Position position){
        Map<ChildContext, Integer> map = childHitCount.get(position);
        if(map==null)
            childHitCount.put(position, map=new HashMap<ChildContext, Integer>());
        Integer pos = map.get(new ChildContext(context));
        if(pos==null)
            pos = 1;
        else
            pos++;
        map.put(new ChildContext(context), pos);
        return pos==position.pos;
    }

    public void contextEnded(ContextManager.Context context){
        for(Node child: context.node.children())
            clearHitCounts(context, child);
    }

    DescendantPositionTracker descendantPositionTracker;
    private void clearHitCounts(ContextManager.Context context, Node node){
        for(Node constraint: node.constraints){
            if(constraint instanceof Position){
                Position position = (Position)constraint;
                if(position.axis==Axis.CHILD){
                    Map<ChildContext, Integer> map = childHitCount.get(position);
                    if(map!=null){
                        map.remove(new ChildContext(context));
                        if(position.selfPosition!=null)
                            descendantPositionTracker.clearHitCounts(position.selfPosition);
                    }
                }
            }
            clearHitCounts(context, constraint);
        }
    }

    public int getHitCount(ContextManager.Context context, Position position){
        Map<ChildContext, Integer> map = childHitCount.get(position);
        if(map==null)
            childHitCount.put(position, map=new HashMap<ChildContext, Integer>());
        Integer pos = map.get(new ChildContext(context));
        if(pos==null)
            pos = 0;

        return pos;
    }

    class ChildContext{
        ContextManager.Context context;
        int depth;

        ChildContext(ContextManager.Context context){
            this.context = context;
            depth = context.depth;
        }

        @Override
        public boolean equals(Object obj){
            if(obj instanceof ChildContext){
                ChildContext that = (ChildContext)obj;
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
