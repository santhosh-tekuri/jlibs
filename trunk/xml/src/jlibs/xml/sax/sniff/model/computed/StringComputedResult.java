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
import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.Results;
import jlibs.xml.sax.sniff.model.UserResults;
import org.jetbrains.annotations.NotNull;

/**
 * @author Santhosh Kumar T
 */
public abstract class StringComputedResult extends ComputedResults{
    public StringComputedResult(FilteredNodeSet filter){
        super(filter, false, ResultType.STRING);
    }

    @Override
    public ResultType resultType(){
        return ResultType.STRING;
    }

    @Override
    @NotNull
    protected Results createResultCache(){
        return new Results();
    }

    @Override
    public void memberHit(UserResults member, Context context, Event event){
        Results resultCache = getResultCache(member, context);
        if(!resultCache.hasResult()){
            ComputedResults _member = (ComputedResults)member;
            Results memberResultCache = _member.getResultCache();
            if(member instanceof StringizedNodeSet){
                StringizedNodeSet.ResultCache _memberResultCache = _member.getResultCache();
                _memberResultCache.prepareResult();
            }
            String result = memberResultCache.results.firstEntry().getValue();
            resultCache.addResult(-1, transform(result));
            notifyObservers(context, event);
        }
    }

    @Override
    public void prepareResults(){
        if(!hasResult()){
            for(Results member: members()){
                if(member instanceof ComputedResults)
                    ((ComputedResults)member).prepareResults();
            }
            Results resultCache = getResultCache();
            if(resultCache!=null && resultCache.hasResult())
                addAllResults(resultCache);
            else{
                addResult(-1, resultType().defaultValue());
            }
        }
    }

    protected abstract String transform(String result);
}
