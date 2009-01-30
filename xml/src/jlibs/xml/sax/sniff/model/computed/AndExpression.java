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
import jlibs.xml.sax.sniff.model.UserResults;
import org.jetbrains.annotations.NotNull;

/**
 * @author Santhosh Kumar T
 */
public class AndExpression extends ComputedResults{
    public AndExpression(){
        super(false, ResultType.NODESET, ResultType.NODESET);
    }

    private class ResultCache extends CachedResults{
        Boolean lhsResult, rhsResult;
    }

    @NotNull
    @Override
    protected ResultCache createResultCache(){
        return new ResultCache();
    }
    
    @Override
    public void memberHit(UserResults member, Context context, Event event){
        ResultCache resultCache = getResultCache(member, context);
        if(!resultCache.hasResult()){
            if(member==members.get(0))
                resultCache.lhsResult = Boolean.TRUE;
            else if(member==members.get(1))
                resultCache.rhsResult = Boolean.TRUE;
            
            if(resultCache.lhsResult!=null && resultCache.rhsResult!=null){
                resultCache.addResult(-1, "true");
                notifyObservers(context, event);
            }
        }
    }
}
