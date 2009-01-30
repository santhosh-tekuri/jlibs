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
import jlibs.xml.sax.sniff.model.UserResults;
import jlibs.xml.sax.sniff.model.axis.Descendant;
import org.jaxen.saxpath.Axis;
import org.jetbrains.annotations.NotNull;

/**
 * @author Santhosh Kumar T
 */
public class SumNodeSet extends ComputedResults{
    private boolean descendants;
    private FilteredNodeSet filter;

    public SumNodeSet(){
        super(false, ResultType.NODESET);
    }

    @Override
    public void addMember(UserResults _member, FilteredNodeSet filter){
        this.filter = filter;
        Node member = (Node)_member;
        if(descendants=member.canBeContext()){
            if(filter!=null && !filter.contextSensitive){
                UserResults cleanupObserver = filter.members.get(0);
                cleanupObserver.cleanupObservers.add(this);
            }
            member = member.addChild(new Descendant(Axis.DESCENDANT));
        }
        super.addMember(member, null);

        if(filter!=null)
            filter.observers.add(this);
        else
            _member.cleanupObservers.add(this);
    }

    @Override
    public ResultType resultType(){
        return ResultType.NUMBER;
    }

    private class ResultCache extends CachedResults{
        double number;
        double pendingNumber;
        StringBuilder buff = new StringBuilder();
        boolean accept = filter==null;

        public void append(String str){
            if(buff==null)
                buff = new StringBuilder();
            buff.append(str);
        }
        
        public void updatePending(String str){
            if(str!=null){
                double d;
                try{
                    d = Double.parseDouble(str);
                }catch(NumberFormatException nfe){
                    d = Double.NaN;
                }
                pendingNumber += d;
            }
        }

        public void add(String str){
            if(str!=null){
                double d;
                try{
                    d = Double.parseDouble(str);
                }catch(NumberFormatException nfe){
                    d = Double.NaN;
                }
                number += d;
                accept = filter==null;
            }
        }
    }

    @NotNull
    @Override
    protected CachedResults createResultCache(){
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
                        resultCache.append(event.getResult());
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
                        resultCache.add(str);
                }
            }
        }
    }

    @Override
    public void endingContext(Context context){
        ResultCache resultCache = getResultCache();
        if(!resultCache.hasResult()){
            resultCache.updatePending(resultCache.buff.toString());
            resultCache.buff = null;
        }
    }

    @Override
    public void clearResults(UserResults member, Context context){
        if(member==filter){
            ResultCache resultCache = getResultCache();
            if(!resultCache.hasResult()){
                if(resultCache.accept){
                    resultCache.number += resultCache.pendingNumber;
                    resultCache.accept = filter==null;
                }
                resultCache.pendingNumber = 0;
            }
        }else
            super.clearResults(member, context);
    }

    @Override
    public void prepareResults(){
        if(!hasResult()){
            ResultCache resultCache = getResultCache();
            if(resultCache!=null){
                clearResults(null, null);
                addResult(-1, String.valueOf(resultCache.number));
                resultCache.buff = null;
                addAllResults(resultCache);
            }else
                addResult(-1, "0.0");
        }
    }
}
