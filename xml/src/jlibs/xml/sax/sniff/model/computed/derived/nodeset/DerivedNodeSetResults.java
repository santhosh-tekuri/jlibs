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

package jlibs.xml.sax.sniff.model.computed.derived.nodeset;

import jlibs.xml.sax.sniff.Context;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.events.PI;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.UserResults;
import jlibs.xml.sax.sniff.model.axis.Descendant;
import jlibs.xml.sax.sniff.model.computed.ComputedResults;
import jlibs.xml.sax.sniff.model.computed.ContextSensitiveFilteredNodeSet;
import jlibs.xml.sax.sniff.model.computed.FilteredNodeSet;
import org.jaxen.saxpath.Axis;

/**
 * @author Santhosh Kumar T
 */
public abstract class DerivedNodeSetResults extends ComputedResults{
    private boolean descendants;
    private FilteredNodeSet filter;

    public DerivedNodeSetResults(){
        super(false, ResultType.NODESET);
    }

    private ContextSensitiveFilteredNodeSet getContextSensitiveFilteredNodeSet(ComputedResults node){
        if(node instanceof ContextSensitiveFilteredNodeSet)
            return (ContextSensitiveFilteredNodeSet)node;
        else{
            for(UserResults member: node.members()){
                if(member instanceof ComputedResults){
                    ContextSensitiveFilteredNodeSet filter = getContextSensitiveFilteredNodeSet((ComputedResults)member);
                    if(filter!=null)
                        return filter;
                }
            }
        }
        return null;
    }

    UserResults descCleanupMember;
    UserResults filterCleanupMember;
    @Override
    public void addMember(UserResults _member, FilteredNodeSet filter){
        this.filter = filter;
        Node member = (Node)_member;
        ContextSensitiveFilteredNodeSet cfilter = null;
        if(descendants=member.canBeContext()){
            if(filter!=null){
                cfilter = getContextSensitiveFilteredNodeSet(filter);
                if(cfilter!=null){
                    UserResults cleanupObserver = cfilter.members.get(0);
                    cleanupObserver.cleanupObservers.add(this);
                    filterCleanupMember = cleanupObserver;
                    cfilter.observers.add(this);
                }
            }
            member = member.addChild(new Descendant(Axis.DESCENDANT));
        }
        super.addMember(member, null);

        if(cfilter==null && filter!=null)
            filter.observers.add(this);
        if(filterCleanupMember!=_member)
            _member.cleanupObservers.add(this);
        descCleanupMember = _member;
    }

    protected abstract class ResultCache extends CachedResults{
        private StringBuilder buff;
        private boolean accept = filter==null;

        private void append(String str){
            if(buff==null)
                buff = new StringBuilder();
            buff.append(str);
        }

        public abstract void updatePending(String str);
        private void _updatePending(String str){
            if(str!=null)
                updatePending(str);
        }

        public abstract void promotePending();
        public abstract void resetPending();
        private void _promotePending(){
            if(!hasResult()){
                if(accept){
                    if(buff!=null)
                        _updatePending(buff.toString());
                    promotePending();
                    accept = filter==null;
                }
                buff = null;
                resetPending();
            }
        }

        public abstract void promote(String str);
        private void _promote(String str){
            if(str!=null){
                promote(str);
                accept = filter==null;
            }
        }

        public abstract void populateResults();
        private boolean preparing;
        public boolean prepareResult(){
            if(preparing)
                return false;
            preparing = true;
            try{
                if(!hasResult()){
                    if(accept && buff!=null)
                        _updatePending(buff.toString());
                    _promotePending();
                    populateResults();
                    buff = null;
                    return true;
                }else
                    return false;
            }finally{
                preparing = false;
            }
        }        
    }

    @Override
    public void memberHit(UserResults member, Context context, Event event){
        ResultCache resultCache = getResultCache();
        if(!resultCache.hasResult()){
            if(member instanceof FilteredNodeSet)
                resultCache.accept = ((ComputedResults) member).getResultCache().hasResult();
            else{
                String str = null;
                if(descendants){
                    if(event.type()==Event.TEXT)
                        resultCache.append(event.getResult());
                }else{
                    switch(event.type()){
                        case Event.TEXT:
                        case Event.COMMENT:
                        case Event.ATTRIBUTE:
                            str = event.getResult();
                            break;
                        case Event.PI:
                            str = ((PI)event).data;
                            break;
                    }
                    if(str!=null)
                        resultCache._promote(str);
                }
            }
        }
    }

    @Override
    public void endingContext(Context context){
        ResultCache resultCache = getResultCache();
        if(context.node==filterCleanupMember){
            if(resultCache.accept){
                if(resultCache.buff!=null)
                    resultCache._updatePending(resultCache.buff!=null ? resultCache.buff.toString() : "");
                resultCache._promotePending();
                resultCache.buff = null;
            }else
                resultCache.resetPending();
        }else if(context.node==descCleanupMember){
//            if(resultCache.buff!=null)
            resultCache._updatePending(resultCache.buff!=null ? resultCache.buff.toString() : "");
        }

        resultCache.buff = null;
    }

    @Override
    public void prepareResults(){
        if(!hasResult()){
            ResultCache resultCache = getResultCache();
            if(resultCache!=null){
                if(resultCache.prepareResult())
                    notifyObservers(null, null);
                addAllResults(resultCache);
            }else
                addResult(-1, resultType().defaultValue());
        }
    }
}
