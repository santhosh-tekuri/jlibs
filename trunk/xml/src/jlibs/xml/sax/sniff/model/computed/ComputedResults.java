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
import jlibs.xml.sax.sniff.model.computed.derived.ToNumber;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class ComputedResults extends Node{
    protected final ResultType memberTypes[];
    protected final boolean variableMembers;
    
    public ComputedResults(boolean variableMembers, ResultType... memberTypes){
        this.variableMembers = variableMembers;
        this.memberTypes = memberTypes;
        members = new ArrayList<UserResults>(variableMembers ? 5 : memberTypes.length);
    }

    @Override
    public boolean equivalent(Node node){
        return false;
    }
    
    public List<UserResults> members;

    public Iterable<UserResults> members(){
        return members;
    }

    private UserResults castTo(UserResults member, FilteredNodeSet filter, ResultType toType){
        if(member.resultType()==ResultType.NODESET){
            switch(toType){
                case STRING:
                    StringizedNodeSet stringizedNodeSet = new StringizedNodeSet();
                    stringizedNodeSet.addMember(member, filter);
                    return stringizedNodeSet;
                case NUMBER:
                    ToNumber toNumber = new ToNumber();
                    toNumber.addMember(castTo(member, filter, ResultType.STRING), null);
                    return toNumber;
                case BOOLEAN:
                    BooleanizedNodeSet bool = new BooleanizedNodeSet();
                    bool.addMember(member, filter);
                    return bool;
            }
        }
        throw new IllegalArgumentException(member.resultType()+" can't be casted to "+toType);
    }

    public ResultType getMemberType(int index){
        if(memberTypes.length>index)
            return memberTypes[index];
        else if(variableMembers)
            return memberTypes[memberTypes.length-1];
        else
            throw new IllegalStateException("no more arguments can be added");
    }

    public void addMember(UserResults member, FilteredNodeSet filter){
        ResultType expected = getMemberType(members.size());

        if(member.resultType()!=expected){
            member = castTo(member, filter, expected);
            filter = null;
        }

        if(filter!=null)
            member = filter;

        root = ((Node)member).root;
        hits.totalHits = member.hits.totalHits;
        members.add(member);
        member.observers.add(this);
    }

    public abstract void memberHit(UserResults member, Context context, Event event);

    @Override
    public void notifyObservers(Context context, Event event){
        super.notifyObservers(context, event);
        if(userGiven)
            hits.hit();
    }

    public String getName(){
        return getClass().getSimpleName();
    }

    @NotNull
    protected abstract CachedResults createResultCache();

    private CachedResults resultCache;
    @SuppressWarnings({"unchecked"})
    public <T extends CachedResults> T getResultCache(UserResults member, Context context){
        if(resultCache!=null)
            return (T)resultCache;

        ComputedResults node = this;
        while(node.observers.size()>0){
            node = node.observers.get(0);
            if(node instanceof FilteredNodeSet){
                FilteredNodeSet filteredNodeSet = (FilteredNodeSet)node;
                if(filteredNodeSet.contextSensitive){
                    return (T)filteredNodeSet.getResultCache(member, context).getResultCache(this);
                }
            }
        }

        resultCache = createResultCache();
        return (T)resultCache;
    }
    
    @SuppressWarnings({"unchecked"})
    public <T extends CachedResults> T getResultCache(){
        return (T)resultCache;
    }

    protected void clearResults(Context context){
        if(resultCache!=null)
            resultCache=null;
        
        for(UserResults observer: members()){
            if(observer instanceof ComputedResults)
                ((ComputedResults)observer).clearResults(context);
        }
        for(ComputedResults observer: observers())
            observer.clearResults(this, context);
    }

    public void clearResults(UserResults member, Context context){}

    @Override
    public void endingContext(Context context){}

    @Override
    public String toString(){
        StringBuilder buff = new StringBuilder();
        if(userGiven)
            buff.append("UserGiven");
        for(UserResults member: members){
            if(buff.length()>0)
                buff.append(", ");
            buff.append('(').append(member).append(')');
        }
        return getName()+'{'+buff+'}';
    }
}
