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

import java.util.HashMap;
import java.util.LinkedHashMap;
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

    private class MemberResults{
        private TreeMap<Integer, String> results = new TreeMap<Integer, String>();
        private Boolean accept;

        private boolean hit(UserResults member, Event event){
            if(member==members.get(0))
                results.put(event.order(), event.getResult());
            else if(member==members.get(1))
                accept = ResultType.BOOLEAN.asBoolean(((ComputedResults)member).getResultCache().results);

            if(accept!=null && results.size()>0){
                if(accept){
                    for(Map.Entry<Integer, String> entry: results.entrySet())
                        addResult(entry.getKey(), entry.getValue());
                }
                results.clear();
                return true;
            }
            return false;
        }

        private void clear(){
            results.clear();
            accept = null;
        }
    }

    public class ResultCache extends CachedResults{
        MemberResults memberResults = new MemberResults();
        Map<UserResults, Object> memberCacheMap = new HashMap<UserResults, Object>();

        public Object getResultCache(ComputedResults member){
            Object cache = memberCacheMap.get(member);
            if(cache==null)
                memberCacheMap.put(member, cache=member.createResultCache());
            return cache; 
        }
    }

    @NotNull
    @Override
    protected ResultCache createResultCache(){
        return new ResultCache();
    }


    public ResultCache resultCache = new ResultCache();
    private LinkedHashMap<Object, ResultCache> map = new LinkedHashMap<Object, ResultCache>();

    @SuppressWarnings({"unchecked"})
    public ResultCache getResultCache(UserResults member, Context context){
        if(contextSensitive){
            if(member==members.get(0)){
                resultCache = map.get(context.identity());
                if(resultCache==null)
                    map.put(context.identity(), resultCache=new ResultCache());
            }
        }
        return resultCache;
    }

    @Override
    public void memberHit(UserResults member, Context context, Event event){
        resultCache = getResultCache(member, context);

        if(resultCache.memberResults.hit(member, event))
            notifyObservers(context, event);
    }

    public boolean contextSensitive;
    public void contextSensitive(){
        contextSensitive = true;
        members.get(0).cleanupObservers.add(this);
    }
    
    @Override
    public void endingContext(Context context){
        if(contextSensitive)
            map.remove(context.identity());
        clearResults(context);
    }

    @Override
    protected void clearResults(Context context){
        if(!contextSensitive)
            resultCache.memberResults.clear();
        super.clearResults(context);
    }

    @Override
    public void clearResults(UserResults member, Context context){
        resultCache.memberResults.clear();
        for(ComputedResults observer: observers())
            observer.clearResults(this, context);
    }
}
