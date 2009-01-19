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
 * @unused
 */
public class PositionTracker{
    Map<Position, Map<Object, Integer>> childHitCount = new HashMap<Position, Map<Object, Integer>>();

    public boolean hit(Context context, Position position){
        if(position.parent!=context.node)
            context = context.parent;
        
        Map<Object, Integer> map = childHitCount.get(position);
        if(map==null)
            childHitCount.put(position, map=new HashMap<Object, Integer>());
        Integer pos = map.get(context.identity());
        if(pos==null)
            pos = 1;
        else
            pos++;
        map.put(context.identity(), pos);
        return pos==position.pos;
    }

    public void contextEnded(Context context){
        for(Node child: context.node.children())
            clearHitCounts(context, child);
    }

    private void clearHitCounts(Context context, Node node){
        for(Node constraint: node.constraints()){
            if(constraint instanceof Position){
                Position position = (Position)constraint;
                Map<Object, Integer> map = childHitCount.get(position);
                if(map!=null)
                    map.remove(context.identity());
            }
            clearHitCounts(context, constraint);
        }
    }

    public int getHitCount(Context context, Position position){
        Map<Object, Integer> map = childHitCount.get(position);
        if(map==null)
            childHitCount.put(position, map=new HashMap<Object, Integer>());
        Integer pos = map.get(context.identity());
        if(pos==null)
            pos = 0;

        return pos;
    }
}
