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
import jlibs.xml.sax.sniff.model.computed.CachedResults;
import org.jetbrains.annotations.NotNull;

/**
 * @author Santhosh Kumar T
 */
public class StringizedNodeSet extends DerivedNodeSetResults{

    @Override
    public ResultType resultType(){
        return ResultType.STRING;
    }

    private class StringResultCache extends ResultCache{
        private String str;
        private String pendingStr;

        @Override
        public void updatePending(String str){
            if(pendingStr==null)
                pendingStr = str;
        }

        @Override
        public void promotePending(){
            promote(pendingStr);
        }

        @Override
        public void resetPending(){
            pendingStr = null;
        }

        @Override
        public void promote(String str){
            if(this.str==null){
                this.str = str;
                if(prepareResult())
                    notifyObservers(null, null);
            }
        }

        @Override
        public void populateResults(){
            if(str!=null)
                addResult(-1, str);
        }
    }

    @NotNull
    @Override
    protected CachedResults createResultCache(){
        return new StringResultCache();
    }
}