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

import jlibs.xml.sax.sniff.engine.context.Context;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.Results;
import jlibs.xml.sax.sniff.model.UserResults;
import jlibs.xml.sax.sniff.model.computed.ComputedResults;
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

    private class ResultCache extends CachedResults{
        int pending = members.size();
        String memberResults[] = new String[members.size()];

        public boolean prepareResult(){
            if(!hasResult()){
                for(int i=0; i<memberResults.length; i++){
                    if(memberResults[i]==null){
                        ComputedResults member = (ComputedResults)members.get(i);
                        CachedResults memberResultCache = member.getResultCache();
                        if(memberResultCache==null)
                            memberResultCache = member.getResultCache();
                        memberResultCache.prepareResult();
                        memberResults[i] = memberResultCache.results.firstEntry().getValue();                        
                    }
                }
                addResult(-1, deriveResult(memberResults));
                return true;
            }
            return false;
        }
    }

    protected String guessResult(String memberResults[], int curMember, String curResult){
        return null;
    }
    
    protected abstract String deriveResult(String memberResults[]);

    @Override
    @NotNull
    protected CachedResults createResultCache(){
        return new ResultCache();
    }

    @Override
    public final void memberHit(UserResults member, Context context, Event event){
        ResultCache resultCache = getResultCache();
        if(!resultCache.hasResult()){
            CachedResults memberResultCache = ((ComputedResults)member).getResultCache();
            memberResultCache.prepareResult();
            String result = memberResultCache.results.firstEntry().getValue();
            int memberIndex = members.indexOf(member);
            resultCache.memberResults[memberIndex] = result;
            resultCache.pending--;
            if(debug)
                debugger.println("Member[%d] Pending=%d: %s ---> %s", memberIndex, resultCache.pending, this, result);
            if(resultCache.pending==0){
                resultCache.prepareResult();
                notifyObservers(context, event);
            }else{
                result = guessResult(resultCache.memberResults, memberIndex, result);
                if(result!=null){
                    resultCache.addResult(-1, result);
                    notifyObservers(context, event);
                }
            }
        }
    }

    @Override
    public final void prepareResults(){
        if(!hasResult()){
            ResultCache resultCache = getResultCache();
            if(resultCache!=null){
                for(int i=0; i<resultCache.memberResults.length; i++){
                    if(resultCache.memberResults[i]==null){
                        Results member = members.get(i);
                        ((ComputedResults)member).prepareResults();
                    }
                }
                resultCache.prepareResult();
                addAllResults(resultCache);
            }else{
                for(Results member: members())
                    ((ComputedResults)member).prepareResults();
                if(getResultCache()!=null)
                    prepareResults();
                else
                    addResult(-1, resultType.defaultValue().toString());
            }
        }
    }
}