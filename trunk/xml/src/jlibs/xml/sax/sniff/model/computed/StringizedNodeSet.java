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
import org.jetbrains.annotations.NotNull;

/**
 * @author Santhosh Kumar T
 */
public class StringizedNodeSet extends ComputedResults{
    private boolean descendants;
    private boolean hasFilter;

    public StringizedNodeSet(Node member, FilteredNodeSet filteredNodeSet){
        super(null, false, ResultType.NODESET);
        if(descendants=member.canBeContext()){
            if(filteredNodeSet==null || filteredNodeSet.members.get(1) instanceof FilteredNodeSet)
                member.cleanupObservers.add(this);
            member = member.addChild(new Descendant(Axis.DESCENDANT));
        }
        addMember(member);

        if(filteredNodeSet!=null){
            hasFilter = true;
            filteredNodeSet.observers.add(this);
        }
    }

    @Override
    public ResultType resultType(){
        return ResultType.STRING;
    }

    public class ResultCache extends Results{
        StringBuilder buff = new StringBuilder();
        boolean accept = !hasFilter;
        boolean contextEnded = false;

        public boolean prepareResult(){
            if(!hasResult()){
                if(accept){
                    addResult(-1, buff.toString());
                    buff = null;
                    return true;
                }else{
                    buff.setLength(0);
                    contextEnded = false;
                }
            }
            return false;
        }
    }

    @NotNull
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
            }else if(!resultCache.contextEnded){
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
                    if(str!=null){
                        resultCache.addResult(-1, str);
                        notifyObservers(null, null);
                    }
                }
            }
        }
    }

    @Override
    public void endingContext(Context context){
        ResultCache resultCache = getResultCache();
        if(!resultCache.hasResult())
            resultCache.contextEnded = true;
    }

    @Override
    public void prepareResults(){
        if(!hasResult()){
            ResultCache resultCache = getResultCache();
            if(resultCache!=null && resultCache.accept){
                if(resultCache.prepareResult())
                    notifyObservers(null, null);
                addAllResults(resultCache);
            }else
                addResult(-1, "");
        }
    }

    @Override
    public void clearResults(UserResults member, Context context){
        ResultCache resultCache = getResultCache();
        if(resultCache.prepareResult())
            notifyObservers(context, null);
    }
}
