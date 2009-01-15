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
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Position;
import jlibs.xml.sax.sniff.model.axis.Descendant;
import org.jaxen.saxpath.Axis;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class DescendantPositionTracker implements PositionTracker{
    private ChildPositionTracker selfPositionTracker;
    private Map<Position, Integer> hitCounts = new HashMap<Position, Integer>();

    public DescendantPositionTracker(ChildPositionTracker selfPositionTracker){
        this.selfPositionTracker = selfPositionTracker;
        selfPositionTracker.descendantPositionTracker = this;
    }

    public boolean hit(ContextManager.Context context, Position position){
        Integer pos = hitCounts.get(position);
        if(pos==null){
            pos = 1;
            if(position.axis==Axis.DESCENDANT_OR_SELF)
                pos += selfPositionTracker.getHitCount(context.parent, position.selfPosition);
        }else
            pos++;
        hitCounts.put(position, pos);
        return pos==position.pos;
    }

    public void contextEnded(ContextManager.Context context){
        if(context.depth==0 && context.node instanceof Descendant){
            clearDescendantHitCounts(context.node);
        }
    }

    private void clearDescendantHitCounts(Node node){
        for(Node constraint: node.constraints){
            if(constraint instanceof Position){
                Position position = (Position)constraint;
                if(position.axis==Axis.DESCENDANT)
                    hitCounts.remove(position);
            }else
                clearDescendantHitCounts(constraint);
        }
    }

    void clearHitCounts(Position pos){
        hitCounts.remove(pos);
    }
}
