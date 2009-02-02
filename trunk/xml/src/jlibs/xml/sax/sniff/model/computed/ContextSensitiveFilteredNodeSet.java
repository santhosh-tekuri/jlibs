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

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.sniff.Context;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.UserResults;

import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class ContextSensitiveFilteredNodeSet extends FilteredNodeSet{
    public ContextSensitiveFilteredNodeSet(Node member, UserResults filter){
        super(member, filter);
        member.cleanupObservers.add(this);
    }

    private Set<ComputedResults> dependantMembers;

    @Override
    protected UserResults _addMember(UserResults member, FilteredNodeSet filter){
        member = super._addMember(member, filter);
        if(dependantMembers==null)
            dependantMembers = new LinkedHashSet<ComputedResults>();
        populateDependants(member);
        return member;
    }

    private void populateDependants(UserResults node){
        if(node instanceof ComputedResults){
            ComputedResults computedNode = (ComputedResults)node;

            boolean add = true;
            for(UserResults member: computedNode.members()){
                if(member instanceof ContextSensitiveFilteredNodeSet)
                    add = false;
            }
            if(add){
                dependantMembers.add(computedNode);
                for(UserResults member: computedNode.members())
                    populateDependants(member);
            }
        }
    }

    private Set<ComputedResults> getDependentObservers(ComputedResults node, Set<ComputedResults> set){
        if(set==null)
            set = new LinkedHashSet<ComputedResults>();
        for(ComputedResults observer: node.observers()){
            if(observer.hasFilterObserver()){
                set.add(observer);
                getDependentObservers(observer, set);
            }
        }
        return set;
    }


    private Deque<Map<ComputedResults, CachedResults>> contextStack = new ArrayDeque<Map<ComputedResults, CachedResults>>();

    /*-------------------------------------------------[ AddContext ]---------------------------------------------------*/

    @Override
    public void memberHit(UserResults member, Context context, Event event){
        if(member==members.get(0))
            addContext(context);
        super.memberHit(member, context, event);
    }

    public void addContext(Context context){
        if(contextSensitiveFilterMember!=null && !contextSensitiveFilterMember.observers.get(0).resultCache.hasResult())
            return;

        if(debug)
            debugger.println("addingContext(%s)", ContextSensitiveFilteredNodeSet.this);

        Map<ComputedResults, CachedResults> map = new LinkedHashMap<ComputedResults, CachedResults>();
        contextStack.push(map);

        if(debug){
            debugger.println("createdNewContextResults(%s)", this);
            debugger.indent++;
        }

        resultCache = createResultCache();
        map.put(ContextSensitiveFilteredNodeSet.this, resultCache);
        if(debug)
            debugger.println(toString());

        for(ComputedResults dependantMember: dependantMembers){
            dependantMember.resultCache = dependantMember.createResultCache();
            map.put(dependantMember, dependantMember.resultCache);
            if(debug)
                debugger.println(dependantMember.toString());
        }
        if(debug)
            debugger.println("");
        for(ComputedResults dependantObserver: getDependentObservers(this, null)){
            dependantObserver.resultCache = dependantObserver.createResultCache();
            map.put(dependantObserver, dependantObserver.resultCache);
            if(debug)
                debugger.println(dependantObserver.toString());
        }

        if(debug)
            debugger.indent--;

        if(contextSensitiveFilterMember!=null && contextSensitiveFilterMember.getResultCache().hasResult())
            contextSensitiveFilterMember.notifyObservers(context, null);
    }

    /*-------------------------------------------------[ RemoveContext ]---------------------------------------------------*/

    @Override
    public void endingContext(Context context){
        removeContext(context);
    }

    public void removeContext(Context context){
        if(debug)
            debugger.println("removingContext(%s)", ContextSensitiveFilteredNodeSet.this);

        if(debug){
            debugger.println("prepareResult(%s)", this);
            debugger.indent++;
        }
        hasContextSensitiveMember = false;
        if(contextSensitiveFilterMember!=null && contextSensitiveFilterMember.getResultCache().hasResult())
            contextSensitiveFilterMember.observers.get(0).notifyObservers(context, null);
        prepareResult(ContextSensitiveFilteredNodeSet.this, context);
        if(debug)
            debugger.indent--;

        if(debug){
            debugger.println("prepareObserverResult(%s)",this);
            debugger.indent++;
        }
        prepareObserverResults(ContextSensitiveFilteredNodeSet.this, context);
        if(debug)
            debugger.indent--;

        if(resultCache.hasResult() && userGiven)
            addAllResults(resultCache);

        if(!contextStack.isEmpty())
            contextStack.pop();

        Map<ComputedResults, CachedResults> map = contextStack.peek();
        if(map!=null){
            for(Map.Entry<ComputedResults, CachedResults> entry: map.entrySet()){
                if(debug)
                    debugger.println("restoredEarlierContextResults(%s)", entry.getKey());
                entry.getKey().resultCache = entry.getValue();
            }
        }else if(resultCache.results!=null && !resultCache.asBoolean(ResultType.BOOLEAN)){
            if(debug)
                debugger.println("clearedContextResults(%s)", this);
            resultCache = createResultCache();
            for(ComputedResults dependantMember: dependantMembers){
                dependantMember.resultCache = dependantMember.createResultCache();
                if(debug)
                    debugger.println("clearedContextResults(%s)", dependantMember);
            }
            if(debug)
                debugger.println("");
            for(ComputedResults dependantObserver: getDependentObservers(this, null)){
                dependantObserver.resultCache = dependantObserver.createResultCache();
                if(debug)
                    debugger.println("clearedContextResults(%s)", dependantObserver);
            }
        }
    }

    private void prepareResult(ComputedResults node, Context context){
        CachedResults resultCache = node.getResultCache();
        if(!resultCache.hasResult()){
            for(UserResults member: node.members){
                if(member instanceof ComputedResults){
                    if(member instanceof FilteredNodeSet){
                        FilteredNodeSet filter = (FilteredNodeSet)member;
                        if(filter.contextSensitiveFilterMember==null)
                            prepareResult((ComputedResults)member, context);
                        if(filter.getResultCache().forcePrepareResult())
                            filter.notifyObservers(context, null);
                        return;
                    }
                    prepareResult((ComputedResults)member, context);
                }
            }
        }
        boolean canPrepareResult = canPrepareResult(node);
        if(canPrepareResult && resultCache.prepareResult())
            node.notifyObservers(context, null);
    }

    boolean hasContextSensitiveMember;
    boolean hasFilteredNodeSet;
    private void _prepareResult(ComputedResults node, Context context){
        CachedResults resultCache = node.getResultCache();
        if(!resultCache.hasResult()){
            for(UserResults member: node.members){
                if(member instanceof ContextSensitiveFilteredNodeSet){
                    if(!hasFilteredNodeSet){
                        hasContextSensitiveMember = true;
                        ContextSensitiveFilteredNodeSet contextSensitiveFilter = (ContextSensitiveFilteredNodeSet)member;
                        if(contextSensitiveFilter.getResultCache().hasResult())
                            contextSensitiveFilter.notifyObservers(context, null);
                    }
                }else if(member instanceof ComputedResults){
                    if(member instanceof FilteredNodeSet)
                        hasFilteredNodeSet = true;
                    prepareResult((ComputedResults)member, context);
                }
            }
        }
        boolean b = node.contextSensitiveFilterMember!=null && node instanceof FilteredNodeSet;
        if(hasContextSensitiveMember && node instanceof FilteredNodeSet){
            if(!b)
                throw new ImpossibleException();
            hasContextSensitiveMember = false;
            FilteredNodeSet filter = (FilteredNodeSet)node;
            if(filter.getResultCache().forcePrepareResult())
                filter.notifyObservers(context, null);
        }else{
            boolean canPrepareResult = canPrepareResult(node);
            if(canPrepareResult && resultCache.prepareResult())
                node.notifyObservers(context, null);
        }
    }

    private boolean canPrepareResult(ComputedResults node){
        for(UserResults member: node.members()){
            if(member instanceof ComputedResults){
                ComputedResults computedMember = (ComputedResults)member;
                if(computedMember.resultCache.results==null)
                    return false;
            }
        }
        return true;
    }

    private void prepareObserverResults(ComputedResults node, Context context){
        for(ComputedResults observer: node.observers()){
            boolean canPrepareResult = canPrepareResult(observer);
//            if(!canPrepareResult)
//                return;
            if(observer instanceof FilteredNodeSet){
                if(canPrepareResult && observer.getResultCache().prepareResult())
                    observer.notifyObservers(context, null);
            }else if(observer.hasFilterObserver() && canPrepareResult && observer.resultCache.results!=null)
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
