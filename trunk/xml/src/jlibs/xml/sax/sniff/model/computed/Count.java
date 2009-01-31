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

import java.util.HashMap;

/**
 * @author Santhosh Kumar T
 */
public class Count extends ComputedResults{
    public Count(){
        super(false, ResultType.NODESET);
    }

    @Override
    public ResultType resultType(){
        return ResultType.NUMBER;
    }

    private class ResultCache extends CachedResults{
        HashMap<Integer, Integer> countsMap = new HashMap<Integer, Integer>();
        int count;

        @Override
        public boolean prepareResult(){
            if(!hasResult()){
                for(int c: countsMap.values())
                    count+=c;
                addResult(-1, String.valueOf((double)count));
                return true;
            }else
                return false;
        }
    }

    @NotNull
    @Override
    protected ResultCache createResultCache(){
        return new ResultCache();
    }

    @Override
    public void memberHit(UserResults member, Context context, Event event){
        ResultCache resultCache = getResultCache();
        if(member instanceof FilteredNodeSet){
            FilteredNodeSet filter = (FilteredNodeSet)member;
            CachedResults cache = filter.getResultCache();
            resultCache.countsMap.put(System.identityHashCode(cache), cache.hasResult() ? cache.results.size() : 0);
        }else
            resultCache.count++;
    }

    @Override
    public void prepareResults(){
        ResultCache resultCache = getResultCache();
        if(!hasResult()){
            if(resultCache!=null){
                if(resultCache.prepareResult())
                    notifyObservers(null, null);
                addAllResults(resultCache);
            }else
                addResult(-1, "0.0");
        }
    }
}
