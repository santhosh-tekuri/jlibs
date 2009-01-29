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

import java.util.TreeMap;

/**
 * @author Santhosh Kumar T
 */
public class Count extends ComputedResults{
    public Count(Results member){
        addMember(member, ResultType.NODESET);
    }

    @Override
    public ResultType resultType(){
        return ResultType.NUMBER;
    }

    private class ResultCache{
        TreeMap<Integer, String> results = new TreeMap<Integer, String>();
        int count;
    }

    @Override
    protected ResultCache createResultCache(){
        return new ResultCache();
    }

    @Override
    public void memberHit(Results member, Context context, Event event){
        ResultCache resultCache = getResultCache(member, context);
        if(member instanceof FilteredNodeSet)
            resultCache.count = member.hasResult() ? member.results.size() : 0;
        else
            resultCache.count++;
    }

    @Override
    public void prepareResults(){
        ResultCache resultCache = getResultCache();
        if(!hasResult()){
            if(resultCache!=null)
                addResult(-1, String.valueOf((double)resultCache.count));
            else
                addResult(-1, "0.0");
        }
    }
}
