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

package jlibs.xml.sax.sniff.model.computed.derived;

import jlibs.xml.sax.sniff.Context;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.Results;
import jlibs.xml.sax.sniff.model.UserResults;
import jlibs.xml.sax.sniff.model.computed.ComputedResults;
import jlibs.xml.sax.sniff.model.computed.StringizedNodeSet;
import org.jetbrains.annotations.NotNull;

/**
 * @author Santhosh Kumar T
 */
public abstract class DerivedResults extends ComputedResults{
    private final ResultType resultType;
    public DerivedResults(ResultType resultType, boolean variableMembers, ResultType... memberTypes){
        super(variableMembers, memberTypes);
        this.resultType = resultType;
    }

    @Override
    public final ResultType resultType(){
        return resultType;
    }

    private class ResultCache extends Results{
        int pending = members.size();
        String memberResults[] = new String[members.size()];

        public void prepareResults(){
            if(!hasResult())
                addResult(-1, deriveResult(memberResults));
        }
    }

    protected abstract String deriveResult(String memberResults[]);

    @Override
    @NotNull
    protected Results createResultCache(){
        return new ResultCache();
    }

    @Override
    public final void memberHit(UserResults member, Context context, Event event){
        ResultCache resultCache = getResultCache(member, context);
        if(!resultCache.hasResult()){
            ComputedResults _member = (ComputedResults)member;
            Results memberResultCache = _member.getResultCache();
            if(member instanceof StringizedNodeSet){
                StringizedNodeSet.ResultCache _memberResultCache = _member.getResultCache();
                _memberResultCache.prepareResult();
            }
            String result = memberResultCache.results.firstEntry().getValue();
            int memberIndex = members.indexOf(member);
            resultCache.memberResults[memberIndex] = result;
            resultCache.pending--;
            if(resultCache.pending==0){
                resultCache.prepareResults();
                notifyObservers(context, event);
            }
        }
    }

    @Override
    public final void prepareResults(){
        if(!hasResult()){
            for(Results member: members()){
                if(member instanceof ComputedResults)
                    ((ComputedResults)member).prepareResults();
            }
            ResultCache resultCache = getResultCache();
            if(resultCache!=null){
                resultCache.prepareResults();
                addAllResults(resultCache);
            }else
                addResult(-1, resultType.defaultValue());
        }
    }
}