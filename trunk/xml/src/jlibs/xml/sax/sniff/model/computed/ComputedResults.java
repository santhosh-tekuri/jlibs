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
import jlibs.xml.sax.sniff.model.Results;
import jlibs.xml.sax.sniff.model.UserResults;
import jlibs.xml.sax.sniff.model.computed.derived.ToNumber;
import jlibs.xml.sax.sniff.model.computed.derived.nodeset.StringizedNodeSet;
import jlibs.xml.sax.sniff.model.computed.derived.nodeset.StringsNodeSet;
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

    protected UserResults castTo(UserResults member, FilteredNodeSet filter, ResultType toType){
        switch(member.resultType()){
            case NODESET:
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
                    case STRINGS:
                        StringsNodeSet strings = new StringsNodeSet();
                        strings.addMember(member, filter);
                        return strings;
                }
                break;
            default:
                if(toType==ResultType.STRINGS)
                    return member;
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

    public UserResults addMember(UserResults member, FilteredNodeSet filter){
        ResultType expected = getMemberType(members.size());

        if(member.resultType()!=expected){
            member = castTo(member, filter, expected);
            filter = null;
        }
        return _addMember(member, filter);
    }
    
    protected ContextSensitiveFilteredNodeSet contextSensitiveFilterMember;
    protected UserResults _addMember(UserResults member, FilteredNodeSet filter){
        if(filter!=null)
            member = filter;

        root = ((Node)member).root;
        hits.totalHits = member.hits.totalHits;
        members.add(member);
        member.addObserver(this);

        if(member instanceof ContextSensitiveFilteredNodeSet)
            contextSensitiveFilterMember = (ContextSensitiveFilteredNodeSet)member;
        else if(contextSensitiveFilterMember==null && member instanceof ComputedResults)
            contextSensitiveFilterMember = ((ComputedResults)member).contextSensitiveFilterMember;

        return member;
    }

    public abstract void memberHit(UserResults member, Context context, Event event);

    @Override
    public void notifyObservers(Context context, Event event){
        super.notifyObservers(context, event);
    }

    @NotNull
    protected abstract CachedResults createResultCache();

    protected CachedResults resultCache;

    public class CachedResults extends Results{
    
        @Override
        public void addResult(int docOrder, String result){
            super.addResult(docOrder, result);
            if(debug)
                debugger.println("CacheHit %d: %s ---> %s", results.size(), ComputedResults.this, result);
            if(userGiven)
                ComputedResults.this.addResult(docOrder, result);
        }

        public boolean prepareResult(){
            return false;
        }

        public boolean asBoolean(ResultType resultType){
            return resultType.asBoolean(results);
        }

        public String asString(ResultType resultType){
            return resultType.asString(results);
        }

        public double asNumber(ResultType resultType){
            return resultType.asNumber(results);
        }
    }

    @SuppressWarnings({"unchecked"})
    public <T extends CachedResults> T getResultCache(){
        if(resultCache==null)
            resultCache = createResultCache();
        return (T)resultCache;
    }

    public FilteredNodeSet getEnclosingFilteredSet(){
        ComputedResults node = this;
        while(node.observers.size()>0){
            node = node.observers.get(0);
            if(node instanceof FilteredNodeSet)
                return (FilteredNodeSet)node;
        }
        return null;
    }

    @Override
    public void endingContext(Context context){}

    /*-------------------------------------------------[ ToString ]---------------------------------------------------*/
    
    public String getName(){
        String name = getClass().getSimpleName();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    @Override
    public String toString(){
        StringBuilder buff = new StringBuilder();
        if(userGiven)
            buff.append("UserGiven");
        for(UserResults member: members){
            if(buff.length()>0)
                buff.append(", ");
            buff.append(member);
        }
        return getName()+'('+buff+')';
    }
}
