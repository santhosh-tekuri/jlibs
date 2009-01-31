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

import jlibs.xml.sax.sniff.model.ResultType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * @author Santhosh Kumar T
 */
public class StringsNodeSet extends DerivedNodeSetResults{
    @Override
    public ResultType resultType(){
        return ResultType.STRINGS;
    }

    private class StringsResultCache extends ResultCache{
        private ArrayList<String> list = new ArrayList<String>();
        private ArrayList<String> pendingList = new ArrayList<String>();

        @Override
        public void updatePending(String str){
            pendingList.add(str);
        }

        @Override
        public void promotePending(){
            list.addAll(pendingList);
        }

        @Override
        public void resetPending(){
            pendingList.clear();
        }

        @Override
        public void promote(String str){
            list.add(str);
        }

        @Override
        public void populateResults(){
            int i=1;
            for(String str: list)
                addResult(i++, str);
            if(results==null)
                results = new TreeMap<Integer, String>();            
        }
    }

    @NotNull
    @Override
    protected CachedResults createResultCache(){
        return new StringsResultCache();
    }


    /*-------------------------------------------------[ ToString ]---------------------------------------------------*/

    @Override
    public String getName(){
        return "strings";
    }
}