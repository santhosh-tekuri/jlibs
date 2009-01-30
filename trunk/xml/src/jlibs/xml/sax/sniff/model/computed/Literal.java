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
import jlibs.xml.sax.sniff.model.Root;
import jlibs.xml.sax.sniff.model.UserResults;
import org.jetbrains.annotations.NotNull;

/**
 * @author Santhosh Kumar T
 */
public class Literal extends ComputedResults{
    String literal;

    public Literal(Root root, String literal){
        this(root, literal, ResultType.STRING);
    }

    public Literal(Root root, double literal){
        this(root, String.valueOf(literal), ResultType.NUMBER);
    }

    public Literal(Root root, boolean literal){
        this(root, String.valueOf(literal), ResultType.BOOLEAN);
    }

    private Literal(Root root, String literal, ResultType resultType){
        super(false);
        this.literal = literal;
        this.resultType = resultType;
        
        root.observers.add(this);
        this.root = root;
        hits.totalHits = root.hits.totalHits;
    }

    private ResultType resultType;

    @Override
    public ResultType resultType(){
        return resultType;
    }

    private class ResultCache extends CachedResults{
        @Override
        public boolean prepareResult(){
            if(!hasResult()){
                addResult(-1, literal);
                return true;
            }
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
        if(!usedAsMemberInFilteredSet()){
            ResultCache resultCache = getResultCache(member, context);
            if(resultCache.prepareResult())
                notifyObservers(context, event);
        }
    }

    @Override
    public void prepareResults(){
        if(!hasResult()){
            ResultCache resultCache = getResultCache();
            resultCache.prepareResult();
            addAllResults(resultCache);
            notifyObservers(null, null);
        }
    }
}
