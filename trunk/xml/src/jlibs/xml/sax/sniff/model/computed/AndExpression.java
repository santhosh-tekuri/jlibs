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

package jlibs.xml.sax.sniff.model.computed;

import jlibs.xml.sax.sniff.Context;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Results;

/**
 * @author Santhosh Kumar T
 */
public class AndExpression extends ComputedResults{
    public AndExpression(Node member1, Node member2){
        addMember(member1);
        addMember(member2);
        hits.totalHits = member1.hits.totalHits;
    }

    @Override
    public void memberHit(Results member, Context context, Event event){
        if(!hasResult()){
            if(!member.asBoolean()){
                addResult(-1, "false");
                notifyObservers(context, event);
            }else{
                if(members.get(0).hasResult() && members.get(1).hasResult()){
                    addResult(-1, String.valueOf(members.get(0).asBoolean() && members.get(1).asBoolean()));
                    notifyObservers(context, event);
                }
            }
        }
    }
}
