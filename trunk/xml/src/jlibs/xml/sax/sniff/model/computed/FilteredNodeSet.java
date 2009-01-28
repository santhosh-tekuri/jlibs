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
public class FilteredNodeSet extends ComputedResults{
    public FilteredNodeSet(Node member, Results filter){
//        if(filter.resultType()!=ResultType.BOOLEAN)
//            throw new IllegalArgumentException("filter should be of boolean type");
        addMember(member);
        addMember(filter);
        hits.totalHits = member.hits.totalHits;
    }

    private class Result{
        int order;
        String result;

        public Result(Event event){
            order = event.order();
            result = event.getResult();
        }
    }

    private class MemberResults{
        private Result result;
        private boolean accept;

        private boolean hit(Results member, Event event){
            if(member==members.get(0))
                result = new Result(event);
            else if(member==members.get(1))
                accept = true;

            if(accept && result!=null){
                addResult(result.order, result.result);
                result = null;
                return true;
            }
            return false;
        }

        private void clear(){
            result = null;
            accept = false;
        }
    }

    private MemberResults memberResults = new MemberResults();
//    private LinkedHashMap<Object, MemberResults> map = new LinkedHashMap<Object, MemberResults>();

    @Override
    public void memberHit(Results member, Context context, Event event){
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

    @Override
    public void endingContext(Context context){
        if(members.get(1) instanceof FilteredNodeSet)
            return;
        else if(members.get(0)==context.node)
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
        memberResults.clear();
        super.clearResults();
    }

    public void clearResults(Results member){
        if(member==members.get(0))
            memberResults.result = null;
        else if(member==members.get(1))
            memberResults.accept = false;
    }
}
