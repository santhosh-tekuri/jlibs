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
import jlibs.xml.sax.sniff.model.Results;

import java.util.TreeMap;

/**
 * @author Santhosh Kumar T
 */
public class BooleanizedNodeSet extends ComputedResults{
    public BooleanizedNodeSet(Node member){
        if(member.resultType()!=ResultType.NODESET)
            throw new IllegalArgumentException("expected nodeset member");
        addMember(member);
    }

    @Override
    public ResultType resultType(){
        return ResultType.BOOLEAN;
    }

    private class ResultCache{
        TreeMap<Integer, String> results = new TreeMap<Integer, String>();
    }

    @Override
    protected ResultCache createResultCache(){
        return new ResultCache();
    }

    @Override
    public void prepareResults(){
        ResultCache resultCache = getResultCache();
        if(!hasResult()){
            if(resultCache!=null && resultCache.results.size()>0)
                addResult(-1, "true");
            else
                addResult(-1, "false");
        }
    }

    public void memberHit(Results member, Context context, Event event){
        ResultCache resultCache = getResultCache(member, context);
        if(resultCache.results.size()==0){
            resultCache.results.put(-1, "true");
            notifyObservers(context, event);
        }
    }
}
