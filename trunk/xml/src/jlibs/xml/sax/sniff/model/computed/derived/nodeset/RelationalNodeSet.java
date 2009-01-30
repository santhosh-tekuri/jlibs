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

package jlibs.xml.sax.sniff.model.computed.derived.nodeset;

import jlibs.xml.sax.sniff.Context;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.UserResults;
import jlibs.xml.sax.sniff.model.computed.CachedResults;
import jlibs.xml.sax.sniff.model.computed.ComputedResults;
import jlibs.xml.sax.sniff.model.computed.FilteredNodeSet;
import org.jetbrains.annotations.NotNull;

import java.util.TreeMap;

/**
 * @author Santhosh Kumar T
 */
public class RelationalNodeSet extends ComputedResults{
    public RelationalNodeSet(){
        super(false);
    }

    @Override
    public ResultType resultType(){
        return ResultType.BOOLEAN;
    }

    @Override
    public void addMember(UserResults _member, FilteredNodeSet filter){
        if(members.size()==2)
            throw new IllegalStateException("no more arguments can be added");
        
        UserResults member = castTo(_member, filter, ResultType.STRINGS);
        if(member!=_member)
            filter = null;
        _addMember(member, filter);
    }

    private class ResultCache extends CachedResults{
        public TreeMap<Integer, String> lhsResults;
        public TreeMap<Integer, String> rhsResults;

        @Override
        public boolean prepareResult(){
            if(!hasResult() && lhsResults!=null && rhsResults!=null){
                ResultType lhsType = members.get(0).resultType();
                if(lhsType==ResultType.STRINGS)
                    lhsType = ResultType.STRING;

                ResultType rhsType = members.get(1).resultType();
                if(rhsType==ResultType.STRINGS)
                    rhsType = ResultType.STRING;

                for(String lhs: lhsResults.values()){
                    for(String rhs: rhsResults.values()){
                        if(evaluateObjectObject(lhsType.convert(lhs), rhsType.convert(rhs))){
                            addResult(-1, "true");
                            return true;
                        }
                    }
                }

                addResult(-1, "false");
                return true;
            }else
                return false;
        }

        private boolean evaluateObjectObject( Object lhs, Object rhs){
          if(lhs instanceof Boolean || rhs instanceof Boolean){
              boolean b1 = ResultType.asBoolean(lhs);
              boolean b2 = ResultType.asBoolean(rhs);
              return evaluateObjects(b1, b2);
          }else if(lhs instanceof Double || rhs instanceof Double){
              double d1 = ResultType.asNumber(lhs);
              double d2 = ResultType.asNumber(rhs);
              return evaluateObjects(d1, d2);
          }else{
              String s1 = ResultType.asString(lhs);
              String s2 = ResultType.asString(rhs);
              return evaluateObjects(s1, s2);
          }
        }

        private boolean evaluateObjects(Object lhs, Object rhs){
            if(lhs instanceof Double){
                if(Double.isNaN((Double)lhs) || Double.isNaN((Double)rhs))
                    return false;
            }
            return lhs.equals( rhs );
        }
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
            CachedResults memberResultCache = ((ComputedResults)member).getResultCache();
            if(member==members.get(0)){
                resultCache.lhsResults = new TreeMap<Integer, String>();
                resultCache.lhsResults.putAll(memberResultCache.results);
            }else if(member==members.get(1)){
                resultCache.rhsResults = new TreeMap<Integer, String>();
                resultCache.rhsResults.putAll(memberResultCache.results);
            }
            if(resultCache.prepareResult())
                notifyObservers(context, event);
        }
    }
}
