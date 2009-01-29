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
import jlibs.xml.sax.sniff.events.PI;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.Results;
import jlibs.xml.sax.sniff.model.UserResults;
import jlibs.xml.sax.sniff.model.axis.Descendant;
import org.jaxen.saxpath.Axis;

/**
 * @author Santhosh Kumar T
 */
public class StringizedNodeSet extends ComputedResults{
    private boolean descendants;
    private boolean hasFilter;

    public StringizedNodeSet(Node member, FilteredNodeSet filteredNodeSet){
        if(descendants=member.canBeContext()){
            member.cleanupObservers.add(this);
            member = member.addChild(new Descendant(Axis.DESCENDANT));
        }
        addMember(member, ResultType.NODESET);

        if(filteredNodeSet!=null){
            hasFilter = true;
            filteredNodeSet.observers.add(this);
        }
    }

    @Override
    public ResultType resultType(){
        return ResultType.STRING;
    }

    private class ResultCache extends Results{
        StringBuilder buff = new StringBuilder();
        boolean accept = !hasFilter;
    }

    @Override
    protected Results createResultCache(){
        return new ResultCache();
    }

    @Override
    public void memberHit(UserResults member, Context context, Event event){
        ResultCache resultCache = getResultCache(member, context);
        if(!resultCache.hasResult()){
            if(member instanceof FilteredNodeSet){
                resultCache.accept = true;
            }else{
                String str = null;
                if(descendants){
                    if(event.type()==Event.TEXT)
                        resultCache.buff.append(event.getResult());
                }else{
                    switch(event.type()){
                        case Event.TEXT:
                        case Event.COMMENT:
                        case Event.ATTRIBUTE:
                            str = event.getResult();
                            break;
                        case Event.PI:
                            str = ((PI)event).data;
                            break;
                    }
                    if(str!=null)
                        resultCache.addResult(-1, str);
                }
            }
        }
    }

    @Override
    public void endingContext(Context context){
        ResultCache resultCache = getResultCache();
        if(!resultCache.hasResult()){
            if(resultCache.accept){
                resultCache.addResult(-1, resultCache.buff.toString());
                resultCache.buff = null;
            }else
                resultCache.buff.setLength(0);
        }
    }

    @Override
    public void prepareResults(){
        if(!hasResult()){
            ResultCache resultCache = getResultCache();
            if(resultCache!=null && resultCache.accept){
                endingContext(null);
                addAllResults(resultCache);
            }else
                addResult(-1, "");
        }
    }
}
