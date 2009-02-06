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

import jlibs.xml.sax.sniff.engine.context.Context;
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

    protected class ResultCache extends CachedResults{
        private TreeMap<Integer, String> pendingResults = new TreeMap<Integer, String>();
        private Boolean accept;

        private boolean hit(UserResults member, Event event){
            if(member==members.get(0)){
                pendingResults.put(event.order(), event.getResult());
                if(debug)
                    debugger.println("PendingResults %d: %s ---> %s", event.order(), FilteredNodeSet.this, event.getResult());
                return prepareResult();
            }else if(member==members.get(1) && accept==null){
                accept = ((ComputedResults)member).getResultCache().asBoolean(ResultType.BOOLEAN);
                if(debug)
                    debugger.println("accept : %s ---> %s", FilteredNodeSet.this, accept);
                return prepareResult();
            }

            return false;
        }

        private void promotePending(){
            for(Map.Entry<Integer, String> entry: pendingResults.entrySet())
                addResult(entry.getKey(), entry.getValue());
            pendingResults.clear();
        }

        @Override
        public boolean prepareResult(){
            if(accept!=null && pendingResults.size()>0){
                if(accept){
                    if(contextSensitiveFilterMember==null){
                        promotePending();
                        return true;
                    }else if(contextSensitiveFilterMember.resultCache.results!=null){
                        if(contextSensitiveFilterMember.resultCache.hasResult())
                            promotePending();
                        else{
                            accept = false;
                            if(results==null)
                                results = new TreeMap<Integer, String>();
                        }
                        return true;
                    }else
                        return false;
                }else{
                    if(results==null)
                        results = new TreeMap<Integer, String>();
                    pendingResults.clear();
                    return true;
                }
            }else
                return false;
        }

        public boolean forcePrepareResult(){
            if(pendingResults.size()==0 && accept==null){
                results = new TreeMap<Integer, String>();
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