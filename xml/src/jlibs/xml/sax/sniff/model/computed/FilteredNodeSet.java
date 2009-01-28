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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Santhosh Kumar T
 */
public class FilteredNodeSet extends ComputedResults{
    public FilteredNodeSet(Node member, Results filter){
//        if(filter.resultType()!=ResultType.BOOLEAN)
//            throw new IllegalArgumentException("filter should be of boolean type");
        addMember(member);
        addMember(filter);
        hits.totalHits = member.hits.totalHits;
    }

    private class MemberResults{
        private TreeMap<Integer, String> results = new TreeMap<Integer, String>();
        private boolean accept;

        private boolean hit(Results member, Event event){
            if(member==members.get(0))
                results.put(event.order(), event.getResult());
            else if(member==members.get(1))
                accept = true;

            if(accept && results.size()>0){
                for(Map.Entry<Integer, String> entry: results.entrySet())
                    addResult(entry.getKey(), entry.getValue());
                results.clear();
                return true;
            }
            return false;
        }

        private void clear(){
            results.clear();
            accept = false;
        }
    }

    private MemberResults memberResults = new MemberResults();
    private LinkedHashMap<Object, MemberResults> map = new LinkedHashMap<Object, MemberResults>();

    @Override
    public void memberHit(Results member, Context context, Event event){
        if(contextSensitive){
            if(member==members.get(0)){
                memberResults = map.get(context.identity());
                if(memberResults==null)
                    map.put(context.identity(), memberResults=new MemberResults());
            }
        }
//        MemberResults memberResults = null;
//        if(members.get(0)==member)
//            memberResults = map.get(context.identity());
//        else{
//            for(MemberResults mr: map.values()){
//                memberResults = mr;
//            }
//        }
//
//        if(memberResults==null)
//            map.put(context.identity(), memberResults=new MemberResults());

        if(memberResults.hit(member, event))
            notifyObservers(context, event);
    }

    private boolean contextSensitive;
    public void contextSensitive(){
        contextSensitive = true;
        members.get(0).cleanupObservers.add(this);
    }
    
    @Override
    public void endingContext(Context context){
//        if(members.get(1) instanceof FilteredNodeSet)
//            return;
//        else if(members.get(0)==context.node)
        if(contextSensitive)
            map.remove(context.identity());
        
        clearResults();

//        Node n = (Node)members.get(1);
//        while(n!=null){
//            n = n.parent;
//            if(n==context.node){
//                clearResults();
//                break;
//            }
//        }
    }

    protected void clearResults(){
        if(!contextSensitive)
            memberResults.clear();
        super.clearResults();
    }

    public void clearResults(Results member){
        memberResults.clear();
//        if(member==members.get(0))
//            memberResults.results.clear();
//        else if(member==members.get(1))
//            memberResults.accept = false;
    }
}
