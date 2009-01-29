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
import jlibs.xml.sax.sniff.model.Root;
import jlibs.xml.sax.sniff.model.UserResults;
import org.jetbrains.annotations.NotNull;

/**
 * @author Santhosh Kumar T
 */
public class StringLiteral extends ComputedResults{
    String literal;

    public StringLiteral(Root root, String literal){
        this.literal = literal;
        root.observers.add(this);
        this.root = root;
        hits.totalHits = root.hits.totalHits;
    }

    @Override
    public ResultType resultType(){
        return ResultType.STRING;
    }

    @NotNull
    @Override
    protected Results createResultCache(){
        return new Results();
    }

    @Override
    public void memberHit(UserResults member, Context context, Event event){
        Results resultCache = getResultCache(member, context);
        if(!resultCache.hasResult()){
            resultCache.addResult(-1, literal);
//            prepareResults();
            notifyObservers(context, event);
        }
    }

    @Override
    public void prepareResults(){
        if(!hasResult()){
            addAllResults(getResultCache());
            notifyObservers(null, null);
        }
    }
}
