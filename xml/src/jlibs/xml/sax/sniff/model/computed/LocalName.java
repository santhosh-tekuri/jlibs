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
import jlibs.xml.sax.sniff.events.Attribute;
import jlibs.xml.sax.sniff.events.Element;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.events.PI;
import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.Results;
import jlibs.xml.sax.sniff.model.UserResults;
import org.jetbrains.annotations.NotNull;

/**
 * @author Santhosh Kumar T
 */
public class LocalName extends ComputedResults{
    public LocalName(UserResults member){
        super(null, false, ResultType.NODESET);
        addMember(member);
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
            String name = null;
            switch(event.type()){
                case Event.ELEMENT:
                    name = ((Element)event).name;
                    break;
                case Event.ATTRIBUTE:
                    name = ((Attribute)event).name;
                    break;
                case Event.PI:
                    name = ((PI)event).target;
                    break;
            }

            if(name!=null)
                resultCache.addResult(-1, name);
        }
    }

    @Override
    public void prepareResults(){
        if(!hasResult()){
            Results resultCache = getResultCache();
            if(resultCache!=null && resultCache.hasResult())
                addAllResults(resultCache);
            else
                addResult(-1, "");
        }
    }
}
