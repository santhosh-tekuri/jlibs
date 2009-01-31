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
import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.UserResults;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Santhosh Kumar T
 */
public class FilteredNodeSet extends ComputedResults{
    public FilteredNodeSet(Node member, UserResults filter){
        super(false, ResultType.NODESET, ResultType.BOOLEAN);
        addMember(member, null);
        addMember(filter, null);
    }

    private class ResultCache extends CachedResults{
        private TreeMap<Integer, String> pendingResults = new TreeMap<Integer, String>();
        private Boolean accept;

        private boolean hit(UserResults member, Event event){
            if(member==members.get(0))
                pendingResults.put(event.order(), event.getResult());
            else if(member==members.get(1))
                accept = ((ComputedResults)member).getResultCache().asBoolean(ResultType.BOOLEAN);

            return prepareResult();
        }

        @Override
        public boolean prepareResult(){
            if(accept!=null && pendingResults.size()>0){
                if(accept){
                    for(Map.Entry<Integer, String> entry: pendingResults.entrySet())
                        addResult(entry.getKey(), entry.getValue());
                    pendingResults.clear();
                }else{
                    if(pendingResults==null)
                        pendingResults = new TreeMap<Integer, String>();
                }
                return true;
            }else
                return false;
        }
    }

    @NotNull
    @Override
    protected ResultCache createResultCache(){
        return new ResultCache();
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public ResultCache getResultCache(){
        return super.getResultCache();
    }

    @Override
    public void memberHit(UserResults member, Context context, Event event){
        if(getResultCache().hit(member, event))
            notifyObservers(context, event);
    }

    /*-------------------------------------------------[ ToString ]---------------------------------------------------*/

    @Override
    public String getName(){
        return "filter";
    }

    @Override
    public void prepareResults(){
        if(!hasResult()){
            getResultCache().prepareResult();
            addAllResults(getResultCache());
        }
        super.prepareResults();
    }
}