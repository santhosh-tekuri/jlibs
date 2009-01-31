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
import jlibs.xml.sax.sniff.model.UserResults;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class ContextSensitiveFilteredNodeSet extends FilteredNodeSet{
    public ContextSensitiveFilteredNodeSet(Node member, UserResults filter){
        super(member, filter);
        member.cleanupObservers.add(this);
    }

    private LinkedHashMap<Object, Map<ComputedResults, CachedResults>> contextMap = new LinkedHashMap<Object, Map<ComputedResults, CachedResults>>();

    /*-------------------------------------------------[ AddContext ]---------------------------------------------------*/

    @Override
    public void memberHit(UserResults member, Context context, Event event){
        if(member==members.get(0))
            addContext(context);
        super.memberHit(member, context, event);
    }

    public void addContext(Context context){
        if(debug)
            debugger.println("addingContext("+ContextSensitiveFilteredNodeSet.this+')');

        Map<ComputedResults, CachedResults> map = new LinkedHashMap<ComputedResults, CachedResults>();
        contextMap.put(context.identity(), map);

        resultCache = createResultCache();
        map.put(ContextSensitiveFilteredNodeSet.this, resultCache);
        createResultCachesForMembers(ContextSensitiveFilteredNodeSet.this, map);
//        if(hasUserGivenFilterObserver(ContextSensitiveFilteredNodeSet.this))
            createResultCachesForObservers(ContextSensitiveFilteredNodeSet.this, map);
    }

    private void createResultCachesForMembers(ComputedResults node, Map<ComputedResults, CachedResults> map){
        for(UserResults member: node.members()){
            if(member instanceof ComputedResults){
                ComputedResults computedMember = (ComputedResults)member;
                computedMember.resultCache = computedMember.createResultCache();
                map.put(computedMember, computedMember.resultCache);
                createResultCachesForMembers(computedMember, map);
            }
        }
    }

    private boolean hasFilterObserver(ComputedResults node){
        if(node instanceof FilteredNodeSet)
            return true;

        for(ComputedResults observer: node.observers()){
            if(observer instanceof FilteredNodeSet)
                return true;
            if(hasFilterObserver(observer))
                return true;
        }
        return false;
    }
    
    private void createResultCachesForObservers(ComputedResults node, Map<ComputedResults, CachedResults> map){
        for(ComputedResults observer: node.observers()){
            if(hasFilterObserver(observer)){
                observer.resultCache = observer.createResultCache();
                map.put(observer, observer.resultCache);
                createResultCachesForObservers(observer, map);
            }
        }
    }

    /*-------------------------------------------------[ RemoveContext ]---------------------------------------------------*/

    @Override
    public void endingContext(Context context){
        removeContext(context);
    }

    public void removeContext(Context context){
        if(debug)
            debugger.println("removingContext("+ContextSensitiveFilteredNodeSet.this+')');

        if(debug){
            debugger.println("prepareResult("+this+')');
            debugger.indent++;
        }
        prepareResult(ContextSensitiveFilteredNodeSet.this, context);
        prepareObserverResults(ContextSensitiveFilteredNodeSet.this, context);
        if(debug)
            debugger.indent--;

        if(resultCache.hasResult() && userGiven)
            addAllResults(resultCache);

        contextMap.remove(context.identity());
        Map<ComputedResults, CachedResults> map = contextMap.get(context.parentContext().identity());
        if(map!=null){
            for(Map.Entry<ComputedResults, CachedResults> entry: map.entrySet())
                entry.getKey().resultCache = entry.getValue();
        }
    }

    private void prepareResult(ComputedResults node, Context context){
        CachedResults resultCache = node.getResultCache();
        if(!resultCache.hasResult()){
            for(UserResults member: node.members){
                if(member instanceof ComputedResults)
                    prepareResult((ComputedResults)member, context);
            }
        }
        if(resultCache.prepareResult())
            node.notifyObservers(context, null);
    }

    private void prepareObserverResults(ComputedResults node, Context context){
        for(ComputedResults observer: node.observers()){
            if(observer instanceof FilteredNodeSet){
                if(observer.getResultCache().prepareResult())
                    observer.notifyObservers(context, null);
            }else
                observer.memberHit(node, context, null);
            prepareObserverResults(observer, context);
        }
    }

    /*-------------------------------------------------[ ToString ]---------------------------------------------------*/
    
    @Override
    public String getName(){
        return "Cfilter";
    }
}
